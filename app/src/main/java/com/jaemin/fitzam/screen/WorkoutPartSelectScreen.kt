package com.jaemin.fitzam.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.jaemin.fitzam.R
import com.jaemin.fitzam.ui.common.FitzamTopAppBar
import com.jaemin.fitzam.ui.common.TopAppBarItem
import com.jaemin.fitzam.ui.theme.FitzamTheme

@Composable
fun WorkoutPartSelectScreen(
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "운동 부위 선택",
                navigation = TopAppBarItem(
                    icon = ImageVector.vectorResource(R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                )
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {

        }
    }
}

@Preview
@Composable
fun WorkoutPartSelectScreenPreview() {
    FitzamTheme {
        WorkoutPartSelectScreen(
            onBackClick = {},
        )
    }
}