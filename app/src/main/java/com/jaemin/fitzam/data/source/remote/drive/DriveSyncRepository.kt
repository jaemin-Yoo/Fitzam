package com.jaemin.fitzam.data.source.remote.drive

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.FileOutputStream
import java.io.File as JavaFile

@Singleton
class DriveSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driveServiceFactory: DriveServiceFactory,
) {
    suspend fun uploadDbBackup(session: DriveAuthSession): Result<Unit> = runCatching {
        val dbPath = getLocalDatabasePath()
        checkpointWalIfPossible(dbPath)
        validateLocalDatabase(dbPath)

        val sourceFile = JavaFile(dbPath)
        if (!sourceFile.exists()) {
            throw IllegalStateException("로컬 DB 파일이 없습니다.")
        }
        val walFile = JavaFile("$dbPath-wal")
        val shmFile = JavaFile("$dbPath-shm")
        Log.i(TAG, "Drive upload: dbPath=$dbPath size=${sourceFile.length()} bytes")
        Log.i(
            TAG,
            "Drive upload: walSize=${walFile.length()} bytes shmSize=${shmFile.length()} bytes",
        )

        val tempFile = JavaFile(context.cacheDir, "fitzam_backup.db")
        sourceFile.copyTo(tempFile, overwrite = true)
        Log.i(TAG, "Drive upload: temp backup created size=${tempFile.length()} bytes")

        val drive = driveServiceFactory.createDriveService(session)
        val fileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name='fitzam.db' and trashed=false")
            .setFields("files(id, name)")
            .execute()

        val existingFileId = fileList.files?.firstOrNull()?.id
        val mediaContent = FileContent("application/octet-stream", tempFile)

        if (existingFileId != null) {
            val metadata = File().apply {
                name = "fitzam.db"
            }
            drive.files()
                .update(existingFileId, metadata, mediaContent)
                .setFields("id")
                .execute()
        } else {
            val metadata = File().apply {
                name = "fitzam.db"
                parents = listOf("appDataFolder")
            }
            drive.files()
                .create(metadata, mediaContent)
                .setFields("id")
                .execute()
        }
    }

    suspend fun downloadDbBackup(session: DriveAuthSession): Result<JavaFile?> = runCatching {
        val drive = driveServiceFactory.createDriveService(session)
        val fileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name='fitzam.db' and trashed=false")
            .setFields("files(id, name)")
            .execute()

        val fileId = fileList.files?.firstOrNull()?.id ?: return@runCatching null
        val tempFile = JavaFile(context.cacheDir, "fitzam_restore.db")
        FileOutputStream(tempFile).use { output ->
            drive.files().get(fileId).executeMediaAndDownloadTo(output)
        }
        tempFile
    }

    fun mergeBackupIntoLocal(backupFile: JavaFile): Result<Unit> = runCatching {
        if (!backupFile.exists()) {
            throw IllegalStateException("백업 파일이 없습니다.")
        }
        val dbPath = getLocalDatabasePath()
        checkpointWalIfPossible(dbPath)

        Log.i(TAG, "Drive restore: start merge, backup=${backupFile.absolutePath}")
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        try {
            db.execSQL("ATTACH DATABASE ? AS backup", arrayOf(backupFile.absolutePath))
            Log.i(TAG, "Drive restore: backup attached")
            db.beginTransaction()
            try {
                mergeTable(db, "exercise_category")
                mergeTable(db, "exercise")
                mergeTable(db, "workout")
                mergeTable(db, "workout_category")
                mergeTable(db, "workout_exercise")
                mergeTable(db, "workout_set")
                mergeTable(db, "favorite_exercise")
                mergeTable(db, "image_url_cache")

                db.setTransactionSuccessful()
                Log.i(TAG, "Drive restore: merge committed")
            } finally {
                db.endTransaction()
            }
        } finally {
            runCatching { db.execSQL("DETACH DATABASE backup") }
            db.close()
            Log.i(TAG, "Drive restore: merge finished")
            backupFile.delete()
        }
    }

    fun getLocalDatabasePath(): String =
        context.getDatabasePath("fitzam.db").absolutePath

    private fun checkpointWalIfPossible(dbPath: String) {
        val db = runCatching {
            SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        }.getOrNull() ?: return

        runCatching {
            val cursor = db.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null)
            cursor.use { cur ->
                if (cur.moveToFirst()) {
                    val busy = cur.getInt(0)
                    val log = cur.getInt(1)
                    val checkpointed = cur.getInt(2)
                    Log.i(
                        TAG,
                        "Drive upload: wal_checkpoint(TRUNCATE) busy=$busy log=$log checkpointed=$checkpointed",
                    )
                }
            }
        }
        db.close()
    }

    private fun validateLocalDatabase(dbPath: String) {
        val db = runCatching {
            SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        }.getOrNull() ?: throw IllegalStateException("로컬 DB를 열 수 없습니다.")

        try {
            val tables = listTables(db)
            Log.i(TAG, "Drive upload: local tables=${tables.joinToString(",")}")
            val requiredTables = listOf(
                "exercise_category",
                "exercise",
                "workout",
                "workout_category",
                "workout_exercise",
                "workout_set",
                "favorite_exercise",
                "image_url_cache",
            )
            val missing = requiredTables.filterNot { tables.contains(it) }
            if (missing.isNotEmpty()) {
                throw IllegalStateException("로컬 DB 테이블이 없습니다: ${missing.joinToString(", ")}")
            }
        } finally {
            db.close()
        }
    }

    private fun listTables(db: SQLiteDatabase): Set<String> {
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'android_%'",
            null,
        )
        return cursor.use { cur ->
            val names = mutableSetOf<String>()
            while (cur.moveToNext()) {
                names.add(cur.getString(0))
            }
            names
        }
    }

    private fun mergeTable(
        db: SQLiteDatabase,
        tableName: String,
    ) {
        if (!hasTable(db, "backup", tableName)) {
            Log.w(TAG, "Drive restore: missing table in backup: $tableName")
            return
        }
        val before = queryCount(db, tableName)
        db.execSQL("INSERT OR IGNORE INTO $tableName SELECT * FROM backup.$tableName")
        val after = queryCount(db, tableName)
        Log.i(TAG, "Drive restore: $tableName merged, inserted=${after - before}")
    }

    private fun hasTable(
        db: SQLiteDatabase,
        schemaName: String,
        tableName: String,
    ): Boolean {
        val cursor = db.rawQuery(
            "SELECT 1 FROM $schemaName.sqlite_master WHERE type='table' AND name=? LIMIT 1",
            arrayOf(tableName),
        )
        return cursor.use { it.moveToFirst() }
    }

    private fun queryCount(
        db: SQLiteDatabase,
        tableName: String,
    ): Long {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $tableName", null)
        return cursor.use { cur ->
            if (cur.moveToFirst()) cur.getLong(0) else 0L
        }
    }
}

private const val TAG = "DriveSyncRepository"
