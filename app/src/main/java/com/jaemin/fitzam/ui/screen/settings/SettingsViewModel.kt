package com.jaemin.fitzam.ui.screen.settings

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun restoreSignInIfPossible(activity: Activity) {
        viewModelScope.launch {
            val session = driveAuthManager.restoreAuthorization(activity)
            updateSignedInSession(session)
        }
    }

    fun onSignInClick(activity: Activity) {
        if (_uiState.value.isSigningIn) {
            return
        }

        _uiState.value = _uiState.value.copy(isSigningIn = true, errorMessage = null)

        viewModelScope.launch {
            val result = driveAuthManager.authorizeDrive(activity)
            result
                .onSuccess { outcome ->
                    when (outcome) {
                        is DriveAuthorizationOutcome.Authorized -> {
                            updateSignedInSession(outcome.session)
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
                .onSuccess { session -> updateSignedInSession(session) }
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

    private fun updateSignedInSession(session: DriveAuthSession?) {
        if (session != null) {
            Log.i(TAG, "Google account connected: email=${session.email}")
        } else {
            Log.i(TAG, "No Google account connected")
        }
        _uiState.value = _uiState.value.copy(
            isSignedIn = session != null,
            accountEmail = session?.email,
            isSigningIn = false,
            pendingIntent = null,
            errorMessage = null,
        )
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
)

