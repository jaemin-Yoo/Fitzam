package com.jaemin.fitzam.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jaemin.fitzam.screen.HomeScreen
import com.jaemin.fitzam.screen.WorkoutPartSelectScreen
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Home : NavKey
    @Serializable data object WorkoutPartSelect : NavKey
}

@Composable
fun FitzamApp() {
    val backStack = rememberNavBackStack(Screen.Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Screen.Home> {
                HomeScreen()
            }
            entry<Screen.WorkoutPartSelect> {
                WorkoutPartSelectScreen()
            }
        }
    )
}