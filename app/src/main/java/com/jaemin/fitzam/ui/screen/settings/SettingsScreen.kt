package com.jaemin.fitzam.ui.screen.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.BuildConfig
import com.jaemin.fitzam.R
import com.jaemin.fitzam.ui.common.DZamButton
import com.jaemin.fitzam.ui.common.FitzamTopAppBar
import com.jaemin.fitzam.ui.common.TopAppBarItem

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onTermsClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onAuthorizationResult(result.data, result.resultCode)
    }

    LaunchedEffect(activity) {
        if (activity != null) {
            viewModel.restoreSignInIfPossible(activity)
        }
    }

    LaunchedEffect(uiState.pendingIntent) {
        val pendingIntent = uiState.pendingIntent
        if (pendingIntent != null) {
            val request = IntentSenderRequest.Builder(pendingIntent).build()
            authorizationLauncher.launch(request)
            viewModel.onPendingIntentLaunched()
        }
    }

    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "설정",
                navigation = TopAppBarItem(
                    icon = ImageVector.vectorResource(R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp,
                )
                .verticalScroll(rememberScrollState()),
        ) {
            SectionTitle(text = "동기화")
            Spacer(Modifier.height(8.dp))

            DZamButton(
                text = if (uiState.isSigningIn) "연결 중..." else "계정 연결",
                onClick = {
                    if (activity != null) {
                        viewModel.onSignInClick(activity)
                    }
                },
                enabled = activity != null && !uiState.isSigningIn,
                modifier = Modifier.fillMaxWidth(),
            )
            if (uiState.accountEmail != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "연결된 계정: ${uiState.accountEmail}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(24.dp))

            SectionTitle(text = "정보")
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                SettingRow(
                    title = "앱 버전",
                    trailingText = "v ${BuildConfig.VERSION_NAME}",
                )
                SettingRow(
                    title = "약관 및 정책",
                    trailingIcon = R.drawable.ic_right_arrow,
                    onClick = onTermsClick,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun SettingRow(
    title: String,
    trailingText: String? = null,
    trailingIcon: Int? = null,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = title)
            when {
                trailingText != null -> {
                    Text(
                        text = trailingText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                trailingIcon != null -> {
                    Icon(
                        imageVector = ImageVector.vectorResource(trailingIcon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) {
            return current
        }
        current = current.baseContext
    }
    return null
}

