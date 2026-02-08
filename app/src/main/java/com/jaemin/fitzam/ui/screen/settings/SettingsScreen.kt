package com.jaemin.fitzam.ui.screen.settings

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
    var isAutoSyncEnabled by rememberSaveable { mutableStateOf(true) }
    var isWifiOnlyEnabled by rememberSaveable { mutableStateOf(true) }
    val activity = LocalContext.current.findActivity()
    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onAuthorizationResult(result.data, result.resultCode)
    }
    val accountPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (activity == null) {
            return@rememberLauncherForActivityResult
        }
        if (result.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (!accountName.isNullOrBlank()) {
            viewModel.onSignInClick(activity, accountName)
        }
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

            val accountEmail = uiState.accountEmail
            if (accountEmail == null) {
                DZamButton(
                    text = if (uiState.isSigningIn) "연결 중..." else "계정 연결",
                    onClick = {
                        val currentActivity = activity ?: return@DZamButton
                        val intent = AccountManager.newChooseAccountIntent(
                            null,
                            null,
                            arrayOf("com.google"),
                            false,
                            null,
                            null,
                            null,
                            null,
                        )
                        accountPickerLauncher.launch(intent)
                    },
                    enabled = activity != null && !uiState.isSigningIn,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                SyncConnectedCard(
                    accountEmail = accountEmail,
                    isAutoSyncEnabled = isAutoSyncEnabled,
                    onAutoSyncChange = { isAutoSyncEnabled = it },
                    isWifiOnlyEnabled = isWifiOnlyEnabled,
                    onWifiOnlyChange = { isWifiOnlyEnabled = it },
                    onSyncNowClick = {},
                    lastSyncText = "마지막 동기화: -",
                    onAccountClick = { viewModel.onSignOutClick() },
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
private fun SyncConnectedCard(
    accountEmail: String,
    isAutoSyncEnabled: Boolean,
    onAutoSyncChange: (Boolean) -> Unit,
    isWifiOnlyEnabled: Boolean,
    onWifiOnlyChange: (Boolean) -> Unit,
    onSyncNowClick: () -> Unit,
    lastSyncText: String,
    onAccountClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            SettingRow(
                title = "연결된 계정",
                trailingText = accountEmail,
                onClick = onAccountClick,
            )
            SettingRow(
                title = "자동 동기화",
                trailingContent = {
                    Switch(
                        checked = isAutoSyncEnabled,
                        onCheckedChange = onAutoSyncChange,
                    )
                },
            )
            SettingRow(
                title = "Wi-Fi에서만 동기화",
                trailingContent = {
                    Switch(
                        checked = isWifiOnlyEnabled,
                        onCheckedChange = onWifiOnlyChange,
                    )
                },
            )
            Spacer(Modifier.height(12.dp))

            DZamButton(
                text = "지금 동기화 하기",
                onClick = onSyncNowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                ,
            )
            Spacer(Modifier.height(4.dp))

            Text(
                text = lastSyncText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    trailingText: String? = null,
    trailingIcon: Int? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailingTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val rowModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        Modifier
            .fillMaxWidth()
    }
    Row(
        modifier = rowModifier
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title)
        when {
            trailingContent != null -> trailingContent()
            trailingText != null -> {
                Text(
                    text = trailingText,
                    color = trailingTextColor,
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







