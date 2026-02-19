package com.jaemin.fitzam.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jaemin.fitzam.data.repository.DriveSyncRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DriveSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val driveSyncRepository: DriveSyncRepository,
) : CoroutineWorker(appContext, params) {

    constructor(appContext: Context, params: WorkerParameters) : this(
        appContext,
        params,
        EntryPointAccessors.fromApplication(
            appContext,
            DriveSyncWorkerEntryPoint::class.java,
        ).driveSyncRepository(),
    )

    override suspend fun doWork(): Result {
        val result = driveSyncRepository.syncNow()
        val error = result.exceptionOrNull()
        return when {
            result.isSuccess -> Result.success()
            error is IllegalStateException -> Result.failure()
            else -> Result.retry()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DriveSyncWorkerEntryPoint {
    fun driveSyncRepository(): DriveSyncRepository
}
