package com.jaemin.fitzam.ui.screen.settings

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.BuildConfig
import com.jaemin.fitzam.R
import com.jaemin.fitzam.data.sync.NetworkStatus
import com.jaemin.fitzam.ui.dzam.DZamAlertDialog
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onTermsClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCellularConfirm by rememberSaveable { mutableStateOf(false) }
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }
    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onAuthorizationResult(result.data, result.resultCode)
    }
    val accountPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (!accountName.isNullOrBlank()) {
            viewModel.onSignInClick(accountName)
        } else {
            Toast.makeText(context, "계정 항목을 선택해야 연결할 수 있어요.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.restoreSignInIfPossible()
    }

    LaunchedEffect(uiState.pendingIntent) {
        val pendingIntent = uiState.pendingIntent
        if (pendingIntent != null) {
            val request = IntentSenderRequest.Builder(pendingIntent).build()
            authorizationLauncher.launch(request)
            viewModel.onPendingIntentLaunched()
        }
    }

    LaunchedEffect(uiState.hasSyncError) {
        if (uiState.hasSyncError) {
            Toast.makeText(context, "동기화에 실패했어요.", Toast.LENGTH_SHORT).show()
            viewModel.onSyncErrorShown()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onAuthErrorShown()
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
            SyncSection(
                uiState = uiState,
                context = context,
                accountPickerLauncher = accountPickerLauncher,
                viewModel = viewModel,
                onShowCellularConfirm = { showCellularConfirm = true },
                onShowLogoutConfirm = { showLogoutConfirm = true },
            )
            Spacer(Modifier.height(24.dp))

            InfoSection(
                onTermsClick = onTermsClick,
            )
        }
    }

    if (showCellularConfirm) {
        DZamAlertDialog(
            title = "모바일 데이터 사용",
            text = "Wi-Fi가 아닙니다. 그래도 동기화를 진행할까요?",
            onConfirm = {
                showCellularConfirm = false
                viewModel.onSyncNowClick()
            },
            onCancel = { showCellularConfirm = false },
            confirmText = "진행",
            cancelText = "취소",
        )
    }

    if (showLogoutConfirm) {
        DZamAlertDialog(
            title = "로그아웃",
            text = "로그아웃 하시겠습니까?",
            onConfirm = {
                showLogoutConfirm = false
                viewModel.onSignOutClick()
            },
            onCancel = { showLogoutConfirm = false },
            confirmText = "확인",
            cancelText = "취소",
        )
    }
}

@Composable
private fun SyncSection(
    uiState: SettingsUiState,
    context: Context,
    accountPickerLauncher: ActivityResultLauncher<Intent>,
    viewModel: SettingsViewModel,
    onShowCellularConfirm: () -> Unit,
    onShowLogoutConfirm: () -> Unit,
) {
    SectionTitle(text = "동기화")
    Spacer(Modifier.height(8.dp))

    val accountEmail = uiState.accountEmail
    if (accountEmail == null) {
        AccountConnectButton(
            isSigningIn = uiState.isSigningIn,
            isCheckingAccount = uiState.isCheckingAccount,
            accountPickerLauncher = accountPickerLauncher,
        )
    } else {
        val lastSyncText = formatLastSyncText(uiState.lastSyncEpochMillis)
        SyncConnectedCard(
            accountEmail = accountEmail,
            isAutoSyncEnabled = uiState.isAutoSyncEnabled,
            onAutoSyncChange = viewModel::onAutoSyncChange,
            isWifiOnlyEnabled = uiState.isWifiOnlyEnabled,
            onWifiOnlyChange = viewModel::onWifiOnlyChange,
            onSyncNowClick = {
                val wifiOnly = uiState.isWifiOnlyEnabled
                val isUnmetered = NetworkStatus.isUnmetered(context)
                if (wifiOnly && !isUnmetered) {
                    onShowCellularConfirm()
                } else {
                    viewModel.onSyncNowClick()
                }
            },
            isSyncing = uiState.isSyncing,
            lastSyncText = lastSyncText,
            onAccountClick = onShowLogoutConfirm,
        )
    }
}

@Composable
private fun AccountConnectButton(
    isSigningIn: Boolean,
    isCheckingAccount: Boolean,
    accountPickerLauncher: ActivityResultLauncher<Intent>,
) {
    val buttonText = when {
        isCheckingAccount -> "계정 확인 중..."
        isSigningIn -> "연결 중..."
        else -> "계정 연결"
    }

    DZamButton(
        text = buttonText,
        onClick = {
            val intent = AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null,
            )
            accountPickerLauncher.launch(intent)
        },
        enabled = !isSigningIn && !isCheckingAccount,
        modifier = Modifier.fillMaxWidth(),
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
    isSyncing: Boolean,
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
                text = if (isSyncing) "동기화 중..." else "지금 동기화 하기",
                onClick = onSyncNowClick,
                enabled = !isSyncing,
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
private fun InfoSection(
    onTermsClick: () -> Unit,
) {
    val context = LocalContext.current
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
            onClick = {
                Toast.makeText(context, "준비 중", Toast.LENGTH_SHORT).show()
                onTermsClick()
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
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

private fun formatLastSyncText(lastSyncEpochMillis: Long?): String {
    if (lastSyncEpochMillis == null) {
        return "마지막 동기화: -"
    }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val dateTime = Instant.ofEpochMilli(lastSyncEpochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return "마지막 동기화: ${dateTime.format(formatter)}"
}
