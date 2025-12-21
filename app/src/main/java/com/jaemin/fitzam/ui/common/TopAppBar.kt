package com.jaemin.fitzam.ui.common


import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

data class TopAppBarItem(
    val icon: ImageVector,
    val contentDescription: String?,
    val onClick: () -> Unit,
)

/**
 * Fitzam 상단바. 제목이 가운데에 배치되어 있는 형태.
 * Material 3 [CenterAlignedTopAppBar] 래핑
 *
 * @param title 상단바 제목
 * @param modifier 상단바 modifier
 * @param navigation 상단바 시작 부분 아이콘, 설명, 클릭 시 호출되는 콜백 아이템
 * @param actions 상단바 끝 부분 아이콘, 설명, 클릭 시 호출되는 콜백 아이템 리스트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitzamCenterAlignedTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigation: TopAppBarItem? = null,
    actions: List<TopAppBarItem> = emptyList(),
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        navigationIcon = {
            if (navigation != null) {
                IconButton(onClick = navigation.onClick) {
                    Icon(
                        imageVector = navigation.icon,
                        contentDescription = navigation.contentDescription,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier,
    )
}

/**
 * Fitzam 상단바. 왼쪽에 로고 이미지가 보이는 형태.
 * Material 3 [TopAppBar] 래핑
 *
 * @param logoRes 로고 이미지 리소스 (ex: R.drawable.logo)
 * @param modifier 상단바 modifier
 * @param actions 상단바 끝 부분 아이콘, 설명, 클릭 시 호출되는 콜백 아이템 리스트
 * @param onLogoClick 로고 이미지 클릭 시 호출되는 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitzamBrandTopAppBar(
    @DrawableRes logoRes: Int,
    modifier: Modifier = Modifier,
    actions: List<TopAppBarItem> = emptyList(),
    onLogoClick: () -> Unit = {},
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onLogoClick) {
                Image(
                    painter = painterResource(logoRes),
                    contentDescription = "로고 이미지",
                )
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier.padding(horizontal = 16.dp),
    )
}