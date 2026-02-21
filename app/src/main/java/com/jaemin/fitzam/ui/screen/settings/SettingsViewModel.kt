package com.jaemin.fitzam.ui.screen.settings

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.AuthRepository
import com.jaemin.fitzam.data.repository.DriveAuthorizationOutcome
import com.jaemin.fitzam.data.repository.DriveSyncRepository
import com.jaemin.fitzam.data.repository.SettingsRepository
import com.jaemin.fitzam.data.source.remote.drive.DriveAuthSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val driveSyncRepository: DriveSyncRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            isSignedOutByUser = authRepository.isUserSignedOut(),
        )

        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    isAutoSyncEnabled = settings.isAutoSyncEnabled,
                    isWifiOnlyEnabled = settings.isWifiOnlyEnabled,
                    lastSyncEpochMillis = settings.lastSyncEpochMillis,
                    hasSyncError = settings.lastSyncErrorMessage != null,
                )
            }
        }
    }

    fun restoreSignInIfPossible() {
        if (authRepository.isUserSignedOut()) {
            return
        }
        _uiState.value = _uiState.value.copy(isCheckingAccount = true)
        viewModelScope.launch {
            val session = authRepository.restoreAuthorization()
            handleSignedInSession(session, shouldRestoreFromDrive = false)
            _uiState.value = _uiState.value.copy(isCheckingAccount = false)
        }
    }

    fun onSignInClick(accountName: String? = null) {
        if (_uiState.value.isSigningIn) {
            return
        }

        _uiState.value = _uiState.value.copy(
            isSigningIn = true,
            errorMessage = null,
            isSignedOutByUser = false,
        )
        authRepository.setUserSignedOut(false)

        viewModelScope.launch {
            val result = authRepository.authorizeDrive(accountName = accountName)
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
            val result = authRepository.handleAuthorizationResult(data, resultCode)
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
        authRepository.setUserSignedOut(true)
        settingsRepository.cancelAutoSync()
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
        settingsRepository.setAutoSyncEnabled(enabled)
    }

    fun onWifiOnlyChange(enabled: Boolean) {
        settingsRepository.setWifiOnlyEnabled(enabled)
    }

    fun onSyncNowClick() {
        if (_uiState.value.isSyncing) {
            return
        }
        settingsRepository.setLastSyncErrorMessage(null)
        _uiState.value = _uiState.value.copy(
            isSyncing = true,
            hasSyncError = false,
        )
        viewModelScope.launch {
            val result = driveSyncRepository.syncNow()
            result
                .onSuccess { timestamp ->
                    Log.i(TAG, "Sync completed successfully at $timestamp")
                }
                .onFailure { error ->
                    Log.e(TAG, "Sync failed", error)
                }
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                lastSyncEpochMillis = result.getOrNull() ?: _uiState.value.lastSyncEpochMillis,
                hasSyncError = result.isFailure,
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
            authRepository.setUserSignedOut(false)
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
            hasSyncError = false,
        )
        viewModelScope.launch {
            val result = driveSyncRepository.restoreFromDriveAndMerge()
            result
                .onSuccess {
                    Log.i(TAG, "Restore from Drive completed successfully")
                }
                .onFailure { error ->
                    Log.e(TAG, "Restore from Drive failed", error)
                }
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                hasSyncError = result.isFailure,
            )
        }
    }

    fun onSyncErrorShown() {
        _uiState.value = _uiState.value.copy(hasSyncError = false)
    }

    private companion object {
        const val TAG = "SettingsViewModel"
    }
}

data class SettingsUiState(
    val isSignedIn: Boolean = false,
    val accountEmail: String? = null,
    val isSigningIn: Boolean = false,
    val isCheckingAccount: Boolean = false,
    val pendingIntent: PendingIntent? = null,
    val errorMessage: String? = null,
    val isSignedOutByUser: Boolean = false,
    val isAutoSyncEnabled: Boolean = true,
    val isWifiOnlyEnabled: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncEpochMillis: Long? = null,
    val hasSyncError: Boolean = false,
)
