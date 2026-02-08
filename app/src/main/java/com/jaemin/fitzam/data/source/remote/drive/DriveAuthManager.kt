package com.jaemin.fitzam.data.source.remote.drive

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val authorizationClient = Identity.getAuthorizationClient(context)
    private val requestedScopes = listOf(
        Scope(DriveScopes.DRIVE_APPDATA),
        Scope("openid"),
        Scope("email"),
        Scope("profile"),
    )

    suspend fun authorizeDrive(activity: Activity): Result<DriveAuthorizationOutcome> = runCatching {
        authorizeInternal()
    }

    suspend fun restoreAuthorization(activity: Activity): DriveAuthSession? {
        return when (val outcome = authorizeInternal(allowResolution = false)) {
            is DriveAuthorizationOutcome.Authorized -> outcome.session
            is DriveAuthorizationOutcome.Resolution -> null
        }
    }

    suspend fun handleAuthorizationResult(
        data: Intent?,
        resultCode: Int,
    ): Result<DriveAuthSession> = runCatching {
        if (resultCode != Activity.RESULT_OK) {
            throw IllegalStateException("사용자가 인증을 취소했습니다.")
        }
        val intent = data ?: throw IllegalArgumentException("인증 결과가 없습니다.")
        val result = authorizationClient.getAuthorizationResultFromIntent(intent)
        createSessionFromResult(result)
    }

    private suspend fun authorizeInternal(
        allowResolution: Boolean = true,
    ): DriveAuthorizationOutcome {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .build()

        val result = awaitAuthorizationResult(request)
        if (result.hasResolution()) {
            if (!allowResolution) {
                return DriveAuthorizationOutcome.Resolution(null)
            }
            val pendingIntent = result.pendingIntent
                ?: throw IllegalStateException("권한 요청 인텐트를 만들 수 없습니다.")
            return DriveAuthorizationOutcome.Resolution(pendingIntent)
        }

        return DriveAuthorizationOutcome.Authorized(createSessionFromResult(result))
    }

    private suspend fun awaitAuthorizationResult(
        request: AuthorizationRequest,
    ): AuthorizationResult = withContext(Dispatchers.IO) {
        Tasks.await(authorizationClient.authorize(request))
    }

    private suspend fun createSessionFromResult(result: AuthorizationResult): DriveAuthSession {
        val accessToken = result.accessToken
            ?: throw IllegalStateException("액세스 토큰이 없습니다.")
        val email = fetchUserEmail(accessToken)
        val grantedScopes = result.grantedScopes
            ?.let { scopes ->
                val mapped = mutableListOf<String>()
                for (scope in scopes) {
                    val scopeUri = if (scope is Scope) {
                        scope.getScopeUri()
                    } else {
                        scope.toString()
                    }
                    mapped.add(scopeUri)
                }
                mapped.ifEmpty { null }
            }
            ?: requestedScopes.map { it.scopeUri }

        return DriveAuthSession(
            email = email,
            accessToken = accessToken,
            grantedScopes = grantedScopes,
        )
    }

    private suspend fun fetchUserEmail(accessToken: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://www.googleapis.com/oauth2/v1/userinfo?alt=json")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")

        try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream.bufferedReader().use { it.readText() }
            if (responseCode !in 200..299) {
                throw IllegalStateException("사용자 정보 조회 실패: HTTP $responseCode")
            }
            val json = JSONObject(body)
            json.optString("email").takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("이메일 정보가 없습니다.")
        } finally {
            connection.disconnect()
        }
    }
}

sealed class DriveAuthorizationOutcome {
    data class Authorized(val session: DriveAuthSession) : DriveAuthorizationOutcome()
    data class Resolution(val pendingIntent: PendingIntent?) : DriveAuthorizationOutcome()
}

