package com.jaemin.fitzam.data.source.remote.drive

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File as JavaFile

@Singleton
class DriveSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driveServiceFactory: DriveServiceFactory,
) {
    suspend fun uploadDbBackup(session: DriveAuthSession): Result<Unit> = runCatching {
        val dbPath = getLocalDatabasePath()
        checkpointWalIfPossible(dbPath)

        val sourceFile = JavaFile(dbPath)
        if (!sourceFile.exists()) {
            throw IllegalStateException("로컬 DB 파일이 없습니다.")
        }

        val tempFile = JavaFile(context.cacheDir, "fitzam_backup.db")
        sourceFile.copyTo(tempFile, overwrite = true)

        val drive = driveServiceFactory.createDriveService(session)
        val fileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name='fitzam.db' and trashed=false")
            .setFields("files(id, name)")
            .execute()

        val existingFileId = fileList.files?.firstOrNull()?.id
        val metadata = File().apply {
            name = "fitzam.db"
            parents = listOf("appDataFolder")
        }
        val mediaContent = FileContent("application/octet-stream", tempFile)

        if (existingFileId != null) {
            drive.files()
                .update(existingFileId, metadata, mediaContent)
                .setFields("id")
                .execute()
        } else {
            drive.files()
                .create(metadata, mediaContent)
                .setFields("id")
                .execute()
        }
    }

    suspend fun downloadDbBackup(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not implemented yet"))

    fun getLocalDatabasePath(): String =
        context.getDatabasePath("fitzam.db").absolutePath

    private fun checkpointWalIfPossible(dbPath: String) {
        val db = runCatching {
            SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        }.getOrNull() ?: return

        runCatching {
            val cursor = db.rawQuery("PRAGMA wal_checkpoint(PASSIVE)", null)
            cursor.close()
        }
        db.close()
    }
}
