package com.jaemin.fitzam.data.sync

data class SyncSettings(
    val isAutoSyncEnabled: Boolean,
    val isWifiOnlyEnabled: Boolean,
    val lastSyncEpochMillis: Long?,
    val lastSyncErrorMessage: String?,
)
