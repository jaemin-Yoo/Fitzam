package com.jaemin.fitzam.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jaemin.fitzam.worker.DriveSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodic(autoEnabled: Boolean, wifiOnly: Boolean) {
        if (!autoEnabled) {
            cancelPeriodic()
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .build()

        val request = PeriodicWorkRequestBuilder<DriveSyncWorker>(
            AUTO_SYNC_INTERVAL_HOURS,
            TimeUnit.HOURS,
        )
            .setInitialDelay(calculateInitialDelayMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancelPeriodic() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun calculateInitialDelayMillis(): Long {
        val now = ZonedDateTime.now()
        val nextMidnight = now.toLocalDate()
            .plusDays(1)
            .atStartOfDay(now.zone)
        return Duration.between(now, nextMidnight).toMillis()
    }
}

private const val WORK_NAME = "drive_auto_sync"
private const val AUTO_SYNC_INTERVAL_HOURS = 24L

