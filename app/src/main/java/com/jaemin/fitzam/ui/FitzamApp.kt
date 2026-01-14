package com.jaemin.fitzam.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jaemin.fitzam.ui.screen.home.HomeScreen
import com.jaemin.fitzam.ui.screen.exercisecategoryselect.ExerciseCategorySelectScreen
import java.time.LocalDate
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Home : NavKey
    @Serializable data class ExerciseCategorySelect(val selectedDate: String) : NavKey
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
                    onAddOrEditWorkout = { selectedDate ->
                        backStack.add(Screen.ExerciseCategorySelect(selectedDate = selectedDate.toString()))
                    }
                )
            }
            entry<Screen.ExerciseCategorySelect> { screen ->
                ExerciseCategorySelectScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    onBackClick = { backStack.removeLastOrNull() },
                    onCompleteClick = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
