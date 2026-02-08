package com.jaemin.fitzam.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jaemin.fitzam.data.sync.SyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DriveSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: SyncManager,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val result = syncManager.syncNow()
        val error = result.exceptionOrNull()
        return when {
            result.isSuccess -> Result.success()
            error is IllegalStateException -> Result.failure()
            else -> Result.retry()
        }
    }
}
