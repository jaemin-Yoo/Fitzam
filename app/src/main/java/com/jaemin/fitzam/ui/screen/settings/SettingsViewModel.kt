package com.jaemin.fitzam.ui.screen.settings

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.jaemin.fitzam.data.source.remote.drive.DriveAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val driveAuthManager: DriveAuthManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        updateSignedInAccount(driveAuthManager.getLastSignedInAccount())
    }

    fun requestSignInIntent(): Intent = driveAuthManager.getSignInIntent()

    fun onSignInResult(data: Intent?, resultCode: Int) {
        if (resultCode != Activity.RESULT_OK) {
            Log.i(TAG, "Google sign-in returned resultCode=$resultCode")
        }

        val result = driveAuthManager.handleSignInResult(data)
        result
            .onSuccess { account -> updateSignedInAccount(account) }
            .onFailure { error ->
                if (error is ApiException) {
                    Log.w(TAG, "Google sign-in failed: statusCode=${error.statusCode}", error)
                } else {
                    Log.w(TAG, "Google sign-in failed", error)
                }
                _uiState.value = _uiState.value.copy(errorMessage = error.message)
            }
    }

    private fun updateSignedInAccount(account: GoogleSignInAccount?) {
        if (account != null) {
            Log.i(TAG, "Google account connected: email=${account.email}, id=${account.id}")
        } else {
            Log.i(TAG, "No Google account connected")
        }
        _uiState.value = _uiState.value.copy(
            isSignedIn = account != null,
            accountEmail = account?.email,
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
    val errorMessage: String? = null,
)
