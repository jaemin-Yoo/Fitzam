package com.jaemin.fitzam.ui.screen.settings

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.sync.SyncManager
import com.jaemin.fitzam.data.sync.SyncScheduler
import com.jaemin.fitzam.data.sync.SyncSettingsRepository
import com.jaemin.fitzam.data.source.remote.drive.DriveAuthManager
import com.jaemin.fitzam.data.source.remote.drive.DriveAuthSession
import com.jaemin.fitzam.data.source.remote.drive.DriveAuthorizationOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val driveAuthManager: DriveAuthManager,
    private val syncSettingsRepository: SyncSettingsRepository,
    private val syncScheduler: SyncScheduler,
    private val syncManager: SyncManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            isSignedOutByUser = driveAuthManager.isUserSignedOut(),
        )

        viewModelScope.launch {
            syncSettingsRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    isAutoSyncEnabled = settings.isAutoSyncEnabled,
                    isWifiOnlyEnabled = settings.isWifiOnlyEnabled,
                    lastSyncEpochMillis = settings.lastSyncEpochMillis,
                    syncErrorMessage = settings.lastSyncErrorMessage,
                )
                syncScheduler.schedulePeriodic(
                    autoEnabled = settings.isAutoSyncEnabled,
                    wifiOnly = settings.isWifiOnlyEnabled,
                )
            }
        }
    }

    fun restoreSignInIfPossible(activity: Activity) {
        if (driveAuthManager.isUserSignedOut()) {
            return
        }
        viewModelScope.launch {
            val session = driveAuthManager.restoreAuthorization(activity)
            handleSignedInSession(session, shouldRestoreFromDrive = false)
        }
    }

    fun onSignInClick(
        activity: Activity,
        accountName: String? = null,
    ) {
        if (_uiState.value.isSigningIn) {
            return
        }

        _uiState.value = _uiState.value.copy(
            isSigningIn = true,
            errorMessage = null,
            isSignedOutByUser = false,
        )
        driveAuthManager.setUserSignedOut(false)

        viewModelScope.launch {
            val result = driveAuthManager.authorizeDrive(
                activity = activity,
                accountName = accountName,
            )
            result
                .onSuccess { outcome ->
                    when (outcome) {
                        is DriveAuthorizationOutcome.Authorized -> {
                            handleSignedInSession(outcome.session, shouldRestoreFromDrive = true)
                            _uiState.value = _uiState.value.copy(isSigningIn = false)
                        }
                        is DriveAuthorizationOutcome.Resolution -> {
                            _uiState.value = _uiState.value.copy(pendingIntent = outcome.pendingIntent)
                        }
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "Google sign-in failed", error)
                    _uiState.value = _uiState.value.copy(
                        isSigningIn = false,
                        errorMessage = error.message,
                    )
                }
        }
    }

    fun onAuthorizationResult(data: Intent?, resultCode: Int) {
        viewModelScope.launch {
            val result = driveAuthManager.handleAuthorizationResult(data, resultCode)
            result
                .onSuccess { session ->
                    handleSignedInSession(session, shouldRestoreFromDrive = true)
                }
                .onFailure { error ->
                    Log.w(TAG, "Google sign-in failed", error)
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
            _uiState.value = _uiState.value.copy(isSigningIn = false)
        }
    }

    fun onPendingIntentLaunched() {
            if (_uiState.value.pendingIntent != null) {
                _uiState.value = _uiState.value.copy(pendingIntent = null)
            }
    }

    fun onSignOutClick() {
        driveAuthManager.setUserSignedOut(true)
        syncScheduler.cancelPeriodic()
        _uiState.value = _uiState.value.copy(
            isSignedIn = false,
            accountEmail = null,
            isSigningIn = false,
            pendingIntent = null,
            errorMessage = null,
            isSignedOutByUser = true,
        )
    }

    fun onAutoSyncChange(enabled: Boolean) {
        syncSettingsRepository.setAutoSyncEnabled(enabled)
        syncScheduler.schedulePeriodic(
            autoEnabled = enabled,
            wifiOnly = _uiState.value.isWifiOnlyEnabled,
        )
    }

    fun onWifiOnlyChange(enabled: Boolean) {
        syncSettingsRepository.setWifiOnlyEnabled(enabled)
        syncScheduler.schedulePeriodic(
            autoEnabled = _uiState.value.isAutoSyncEnabled,
            wifiOnly = enabled,
        )
    }

    fun onSyncNowClick() {
        if (_uiState.value.isSyncing) {
            return
        }
        syncSettingsRepository.setLastSyncErrorMessage(null)
        _uiState.value = _uiState.value.copy(
            isSyncing = true,
            syncErrorMessage = null,
        )
        viewModelScope.launch {
            val result = syncManager.syncNow()
            val error = result.exceptionOrNull()
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                lastSyncEpochMillis = result.getOrNull() ?: _uiState.value.lastSyncEpochMillis,
                syncErrorMessage = if (result.isSuccess) {
                    null
                } else {
                    error?.message?.takeIf { it.isNotBlank() }
                        ?: error?.javaClass?.simpleName
                        ?: "알 수 없는 오류"
                },
            )
        }
    }

    private fun updateSignedInSession(session: DriveAuthSession?) {
        if (session != null) {
            Log.i(TAG, "Google account connected: email=${session.email}")
        } else {
            Log.i(TAG, "No Google account connected")
        }
        val signedOutByUser = if (session != null) {
            false
        } else {
            _uiState.value.isSignedOutByUser
        }
        if (session != null) {
            driveAuthManager.setUserSignedOut(false)
        }
        _uiState.value = _uiState.value.copy(
            isSignedIn = session != null,
            accountEmail = session?.email,
            isSigningIn = false,
            pendingIntent = null,
            errorMessage = null,
            isSignedOutByUser = signedOutByUser,
        )
    }

    private fun handleSignedInSession(
        session: DriveAuthSession?,
        shouldRestoreFromDrive: Boolean,
    ) {
        updateSignedInSession(session)
        if (session != null && shouldRestoreFromDrive) {
            restoreFromDrive()
        }
    }

    private fun restoreFromDrive() {
        if (_uiState.value.isSyncing) {
            return
        }
        _uiState.value = _uiState.value.copy(
            isSyncing = true,
            syncErrorMessage = null,
        )
        viewModelScope.launch {
            val result = syncManager.restoreFromDriveAndMerge()
            val error = result.exceptionOrNull()
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                syncErrorMessage = if (result.isSuccess) {
                    null
                } else {
                    error?.message?.takeIf { it.isNotBlank() }
                        ?: error?.javaClass?.simpleName
                        ?: "알 수 없는 오류"
                },
            )
        }
    }

    private companion object {
        const val TAG = "SettingsViewModel"
    }
}

data class SettingsUiState(
    val isSignedIn: Boolean = false,
    val accountEmail: String? = null,
    val isSigningIn: Boolean = false,
    val pendingIntent: PendingIntent? = null,
    val errorMessage: String? = null,
    val isSignedOutByUser: Boolean = false,
    val isAutoSyncEnabled: Boolean = true,
    val isWifiOnlyEnabled: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncEpochMillis: Long? = null,
    val syncErrorMessage: String? = null,
)



