package com.jaemin.fitzam.data.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import com.jaemin.fitzam.data.source.remote.drive.DriveAuthSession
import com.jaemin.fitzam.data.source.remote.drive.DriveServiceFactory
import com.jaemin.fitzam.data.source.local.DatabaseConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.File as JavaFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driveServiceFactory: DriveServiceFactory,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun syncNow(): Result<Long> {
        if (authRepository.isUserSignedOut()) {
            return Result.failure(IllegalStateException("계정 연결이 필요합니다."))
        }
        val session = authRepository.tryRestoreAuthorization()
            ?: return Result.failure(IllegalStateException("계정 연결이 필요합니다."))

        val result = withContext(Dispatchers.IO) {
            uploadDbBackup(session)
        }
        return result
            .map {
                val syncedAt = System.currentTimeMillis()
                settingsRepository.setLastSyncEpochMillis(syncedAt)
                settingsRepository.setLastSyncErrorMessage(null)
                syncedAt
            }
            .onFailure { error ->
                Log.e(TAG, "Drive sync failed", error)
                val message = error.message?.takeIf { it.isNotBlank() }
                    ?: error.javaClass.simpleName
                settingsRepository.setLastSyncErrorMessage(message)
            }
    }

    suspend fun restoreFromDriveAndMerge(): Result<Unit> {
        if (authRepository.isUserSignedOut()) {
            return Result.failure(IllegalStateException("계정 연결이 필요합니다."))
        }
        val session = authRepository.tryRestoreAuthorization()
            ?: return Result.failure(IllegalStateException("계정 연결이 필요합니다."))

        settingsRepository.setLastSyncErrorMessage(null)
        val result = withContext(Dispatchers.IO) {
            downloadDbBackup(session)
                .mapCatching { backupFile ->
                    if (backupFile == null) {
                        return@mapCatching
                    }
                    mergeBackupIntoLocal(backupFile).getOrThrow()
                }
        }

        return result.onFailure { error ->
            Log.e(TAG, "Drive restore failed", error)
            val message = error.message?.takeIf { it.isNotBlank() }
                ?: error.javaClass.simpleName
            settingsRepository.setLastSyncErrorMessage(message)
        }
    }

    suspend fun uploadDbBackup(session: DriveAuthSession): Result<Unit> = runCatching {
        val dbPath = getLocalDatabasePath()
        checkpointWalIfPossible(dbPath)
        validateLocalDatabase(dbPath)

        val sourceFile = JavaFile(dbPath)
        if (!sourceFile.exists()) {
            throw IllegalStateException("로컬 DB 파일이 없습니다.")
        }
        val walFile = JavaFile("-wal")
        val shmFile = JavaFile("-shm")
        Log.i(TAG, "Drive upload: dbPath= size= bytes")
        Log.i(
            TAG,
            "Drive upload: walSize= bytes shmSize= bytes",
        )

        val tempFile = JavaFile(context.cacheDir, DatabaseConfig.tempBackupFileName())
        sourceFile.copyTo(tempFile, overwrite = true)
        Log.i(TAG, "Drive upload: temp backup created size= bytes")

        val drive = driveServiceFactory.createDriveService(session)
        val driveFileName = DatabaseConfig.driveDbFileName()
        val fileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name='$driveFileName' and trashed=false")
            .setFields("files(id, name)")
            .execute()

        val existingFileId = fileList.files?.firstOrNull()?.id
        val mediaContent = FileContent("application/octet-stream", tempFile)

        if (existingFileId != null) {
            val metadata = File().apply { name = driveFileName }
            drive.files()
                .update(existingFileId, metadata, mediaContent)
                .setFields("id")
                .execute()
        } else {
            val metadata = File().apply {
                name = driveFileName
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
        val driveFileName = DatabaseConfig.driveDbFileName()
        val fileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name='$driveFileName' and trashed=false")
            .setFields("files(id, name)")
            .execute()

        val fileId = fileList.files?.firstOrNull()?.id ?: return@runCatching null
        val tempFile = JavaFile(context.cacheDir, DatabaseConfig.tempRestoreFileName())
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

        Log.i(TAG, "Drive restore: start merge, backup=")
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
        context.getDatabasePath(DatabaseConfig.localDbFileName()).absolutePath

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
                        "Drive upload: wal_checkpoint(TRUNCATE) busy= log= checkpointed=",
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
            Log.i(TAG, "Drive upload: local tables=")
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
                throw IllegalStateException("로컬 DB 테이블이 없습니다: ")
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
            Log.w(TAG, "Drive restore: missing table in backup: ")
            return
        }
        val before = queryCount(db, tableName)
        db.execSQL("INSERT OR IGNORE INTO  SELECT * FROM backup.")
        val after = queryCount(db, tableName)
        Log.i(TAG, "Drive restore:  merged, inserted=")
    }

    private fun hasTable(
        db: SQLiteDatabase,
        schemaName: String,
        tableName: String,
    ): Boolean {
        val cursor = db.rawQuery(
            "SELECT 1 FROM .sqlite_master WHERE type='table' AND name=? LIMIT 1",
            arrayOf(tableName),
        )
        return cursor.use { it.moveToFirst() }
    }

    private fun queryCount(
        db: SQLiteDatabase,
        tableName: String,
    ): Long {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ", null)
        return cursor.use { cur ->
            if (cur.moveToFirst()) cur.getLong(0) else 0L
        }
    }
}

private const val TAG = "DriveSyncRepository"
