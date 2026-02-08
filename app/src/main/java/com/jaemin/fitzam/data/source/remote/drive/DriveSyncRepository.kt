package com.jaemin.fitzam.data.source.remote.drive

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: DriveAuthManager,
    private val driveServiceFactory: DriveServiceFactory,
) {
    suspend fun uploadDbBackup(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not implemented yet"))

    suspend fun downloadDbBackup(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not implemented yet"))

    fun getLocalDatabasePath(): String =
        context.getDatabasePath("fitzam.db").absolutePath
}
