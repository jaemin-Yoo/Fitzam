package com.jaemin.fitzam.ui

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jaemin.fitzam.ui.screen.HomeScreen
import com.jaemin.fitzam.ui.screen.WorkoutPartSelectScreen
import java.time.LocalDate
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Home : NavKey
    @Serializable data class WorkoutPartSelect(val selectedDate: String) : NavKey
}

@Composable
fun FitzamApp() {
    val backStack = rememberNavBackStack(Screen.Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Screen.Home> {
                HomeScreen(
                    onAddWorkout = { selectedDate ->
                        backStack.add(Screen.WorkoutPartSelect(selectedDate = selectedDate.toString()))
                    }
                )
            }
            entry<Screen.WorkoutPartSelect> { screen ->
                WorkoutPartSelectScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    onBackClick = { backStack.removeLastOrNull() },
                    onPartClick = {},
                    onCompleteClick = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}