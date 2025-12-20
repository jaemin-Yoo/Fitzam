package com.jaemin.fitzam.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.ui.common.FitzamCalendar
import com.jaemin.fitzam.ui.theme.FitzamTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Column {
        FitzamCalendar(
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    FitzamTheme {
        HomeScreen()
    }
}