package com.jaemin.fitzam.ui.dzam

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.R
import com.jaemin.fitzam.ui.theme.FitzamTheme

/**
 * Fitzam 상단바.
 * 제목이 있으면 중앙 정렬 타이틀 형태, 없으면 네비게이션/액션 전용 형태로 동작한다.
 *
 * @param title 상단바 제목. null 이거나 빈 문자열이면 타이틀 없이 렌더링
 * @param modifier 상단바 modifier
 * @param navigation 상단바 시작 부분 아이템
 * @param actions 상단바 끝 부분 아이템 리스트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitzamTopAppBar(
    title: String? = null,
    modifier: Modifier = Modifier,
    navigation: TopAppBarItem? = null,
    actions: List<TopAppBarItem> = emptyList(),
    actionContent: (@Composable () -> Unit)? = null,
) {
    val hasTitle = !title.isNullOrBlank()

    if (hasTitle) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            navigationIcon = {
                if (navigation != null) {
                    TopAppBarActionItem(
                        item = navigation,
                        defaultTint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
            actions = {
                actionContent?.invoke()
                actions.forEach { action ->
                    TopAppBarActionItem(
                        item = action,
                        defaultTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = modifier,
        )
    } else {
        TopAppBar(
            title = {},
            navigationIcon = {
                if (navigation != null) {
                    TopAppBarActionItem(
                        item = navigation,
                        defaultTint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
            actions = {
                actionContent?.invoke()
                actions.forEach { action ->
                    TopAppBarActionItem(
                        item = action,
                        defaultTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = modifier.padding(horizontal = 16.dp),
        )
    }
}

sealed interface IconSource {
    data class Vector(val imageVector: ImageVector) : IconSource
    data class Drawable(@DrawableRes val resId: Int) : IconSource
}

data class TopAppBarItem(
    val icon: IconSource? = null,
    val contentDescription: String? = null,
    val label: String? = null,
    val iconTint: Color? = null,
    val onClick: () -> Unit,
)

@Composable
private fun TopAppBarActionItem(
    item: TopAppBarItem,
    defaultTint: Color,
) {
    val resolvedTint = item.iconTint ?: defaultTint

    when (val icon = item.icon) {
        is IconSource.Vector -> {
            IconButton(onClick = item.onClick) {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = item.contentDescription,
                    tint = resolvedTint,
                )
            }
        }

        is IconSource.Drawable -> {
            IconButton(onClick = item.onClick) {
                Icon(
                    painter = painterResource(id = icon.resId),
                    contentDescription = item.contentDescription,
                    tint = resolvedTint,
                )
            }
        }

        null -> {
            if (!item.label.isNullOrBlank()) {
                TextButton(onClick = item.onClick) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = resolvedTint,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FitzamTopAppBarTitlePreview() {
    FitzamTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            FitzamTopAppBar(
                title = "홈",
                modifier = Modifier.fillMaxWidth(),
                navigation = TopAppBarItem(
                    icon = IconSource.Vector(ImageVector.vectorResource(id = R.drawable.ic_back)),
                    contentDescription = "뒤로가기",
                    onClick = {},
                ),
                actions = listOf(
                    TopAppBarItem(
                        icon = IconSource.Vector(ImageVector.vectorResource(id = R.drawable.ic_settings)),
                        contentDescription = "설정",
                        onClick = {},
                    ),
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FitzamTopAppBarBrandPreview() {
    FitzamTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            FitzamTopAppBar(
                title = null,
                modifier = Modifier.fillMaxWidth(),
                navigation = TopAppBarItem(
                    icon = IconSource.Drawable(R.drawable.fitzam_logo),
                    contentDescription = "로고 이미지",
                    iconTint = Color.Unspecified,
                    onClick = {},
                ),
                actions = listOf(
                    TopAppBarItem(
                        icon = IconSource.Vector(ImageVector.vectorResource(id = R.drawable.ic_settings)),
                        contentDescription = "추가",
                        onClick = {},
                    ),
                ),
            )
        }
    }
}
