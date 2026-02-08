package com.jaemin.fitzam.data.sync

import com.jaemin.fitzam.data.source.remote.drive.DriveAuthManager
import com.jaemin.fitzam.data.source.remote.drive.DriveSyncRepository
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val authManager: DriveAuthManager,
    private val driveSyncRepository: DriveSyncRepository,
    private val settingsRepository: SyncSettingsRepository,
) {
    suspend fun syncNow(): Result<Long> {
        if (authManager.isUserSignedOut()) {
            return Result.failure(IllegalStateException("계정 연결이 필요합니다."))
        }
        val session = authManager.tryRestoreAuthorization()
            ?: return Result.failure(IllegalStateException("계정 연결이 필요합니다."))

        val result = withContext(Dispatchers.IO) {
            driveSyncRepository.uploadDbBackup(session)
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
        if (authManager.isUserSignedOut()) {
            return Result.failure(IllegalStateException("계정 연결이 필요합니다."))
        }
        val session = authManager.tryRestoreAuthorization()
            ?: return Result.failure(IllegalStateException("계정 연결이 필요합니다."))

        settingsRepository.setLastSyncErrorMessage(null)
        val result = withContext(Dispatchers.IO) {
            driveSyncRepository.downloadDbBackup(session)
                .mapCatching { backupFile ->
                    if (backupFile == null) {
                        return@mapCatching
                    }
                    driveSyncRepository.mergeBackupIntoLocal(backupFile).getOrThrow()
                }
        }

        return result.onFailure { error ->
            Log.e(TAG, "Drive restore failed", error)
            val message = error.message?.takeIf { it.isNotBlank() }
                ?: error.javaClass.simpleName
            settingsRepository.setLastSyncErrorMessage(message)
        }
    }
}

private const val TAG = "SyncManager"
