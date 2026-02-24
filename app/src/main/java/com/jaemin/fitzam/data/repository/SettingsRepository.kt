package com.jaemin.fitzam.data.repository

import android.content.Context
import com.jaemin.fitzam.data.sync.SyncScheduler
import com.jaemin.fitzam.data.sync.SyncSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncScheduler: SyncScheduler,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(loadSettings())
    val settings = _settings.asStateFlow()

    init {
        syncScheduler.schedulePeriodic(
            autoEnabled = _settings.value.isAutoSyncEnabled,
            wifiOnly = _settings.value.isWifiOnlyEnabled,
            forceReschedule = false,
        )
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
        _settings.value = loadSettings()

        syncScheduler.schedulePeriodic(
            autoEnabled = enabled,
            wifiOnly = _settings.value.isWifiOnlyEnabled,
            forceReschedule = true,
        )
    }

    fun setWifiOnlyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
        _settings.value = loadSettings()

        syncScheduler.schedulePeriodic(
            autoEnabled = _settings.value.isAutoSyncEnabled,
            wifiOnly = enabled,
            forceReschedule = true,
        )
    }

    fun setLastSyncEpochMillis(epochMillis: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, epochMillis).apply()
        _settings.value = loadSettings()
    }

    fun setLastSyncErrorMessage(message: String?) {
        val editor = prefs.edit()
        if (message.isNullOrBlank()) {
            editor.remove(KEY_LAST_SYNC_ERROR)
        } else {
            editor.putString(KEY_LAST_SYNC_ERROR, message)
        }
        editor.apply()
        _settings.value = loadSettings()
    }

    fun cancelAutoSync() {
        syncScheduler.cancelPeriodic()
    }

    private fun loadSettings(): SyncSettings {
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L).takeIf { it > 0L }
        return SyncSettings(
            isAutoSyncEnabled = prefs.getBoolean(KEY_AUTO_SYNC, true),
            isWifiOnlyEnabled = prefs.getBoolean(KEY_WIFI_ONLY, true),
            lastSyncEpochMillis = lastSync,
            lastSyncErrorMessage = prefs.getString(KEY_LAST_SYNC_ERROR, null),
        )
    }
}

private const val PREFS_NAME = "sync_settings"
private const val KEY_AUTO_SYNC = "auto_sync_enabled"
private const val KEY_WIFI_ONLY = "wifi_only_enabled"
private const val KEY_LAST_SYNC = "last_sync_epoch"
private const val KEY_LAST_SYNC_ERROR = "last_sync_error"
