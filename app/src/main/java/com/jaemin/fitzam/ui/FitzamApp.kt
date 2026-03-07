package com.jaemin.fitzam.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jaemin.fitzam.ui.screen.exercisecategoryselect.ExerciseCategorySelectScreen
import com.jaemin.fitzam.ui.screen.detailexerciseadd.DetailExerciseAddScreen
import com.jaemin.fitzam.ui.screen.home.HomeScreen
import com.jaemin.fitzam.ui.screen.settings.SettingsScreen
import com.jaemin.fitzam.ui.screen.workoutadd.WorkoutAddScreen
import com.jaemin.fitzam.ui.screen.workoutseteditor.WorkoutSetEditorScreen
import java.time.LocalDate
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Home : NavKey
    @Serializable data object Settings : NavKey
    @Serializable data class ExerciseCategorySelect(
        val selectedDate: String,
        val sessionId: Long,
    ) : NavKey
    @Serializable data class DetailExerciseAdd(
        val selectedDate: String,
        val selectedCategoryIds: String,
        val sessionId: Long,
    ) : NavKey
    @Serializable data class WorkoutAdd(
        val selectedDate: String,
        val selectedExerciseIds: String,
        val selectedCategoryIds: String,
        val sessionId: Long,
    ) : NavKey
    @Serializable data class WorkoutSetEditor(
        val selectedDate: String,
        val exerciseId: Long,
        val exerciseName: String,
        val sessionId: Long,
    ) : NavKey
}

@Composable
fun FitzamApp() {
    val backStack = rememberNavBackStack(Screen.Home)
    val popBackStack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = popBackStack,
        entryProvider = entryProvider {
            entry<Screen.Home> {
                HomeScreen(
                    onAddOrEditWorkout = { selectedDate ->
                        backStack.add(
                            Screen.ExerciseCategorySelect(
                                selectedDate = selectedDate.toString(),
                                sessionId = System.currentTimeMillis(),
                            )
                        )
                    },
                    onSettingsClick = { backStack.add(Screen.Settings) },
                )
            }
            entry<Screen.ExerciseCategorySelect> { screen ->
                ExerciseCategorySelectScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    sessionId = screen.sessionId,
                    onBackClick = popBackStack,
                    onDetailAddClick = { selectedCategoryIds ->
                        backStack.add(
                            Screen.DetailExerciseAdd(
                                selectedDate = screen.selectedDate,
                                selectedCategoryIds = selectedCategoryIds.joinToString(","),
                                sessionId = System.currentTimeMillis(),
                            )
                        )
                    },
                    onCompleteClick = popBackStack,
                )
            }
            entry<Screen.DetailExerciseAdd> { screen ->
                DetailExerciseAddScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    selectedCategoryIds = screen.selectedCategoryIds
                        .split(",")
                        .mapNotNull { value -> value.toLongOrNull() }
                        .toSet(),
                    sessionId = screen.sessionId,
                    onBackClick = popBackStack,
                    onCompleteClick = { selectedExerciseIds ->
                        backStack.add(
                            Screen.WorkoutAdd(
                                selectedDate = screen.selectedDate,
                                selectedExerciseIds = selectedExerciseIds.joinToString(","),
                                selectedCategoryIds = screen.selectedCategoryIds,
                                sessionId = System.currentTimeMillis(),
                            )
                        )
                    },
                )
            }
            entry<Screen.WorkoutAdd> { screen ->
                WorkoutAddScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    selectedExerciseIds = screen.selectedExerciseIds
                        .split(",")
                        .mapNotNull { value -> value.toLongOrNull() }
                        .toSet(),
                    sessionId = screen.sessionId,
                    onBackClick = popBackStack,
                    onDetailAddClick = {
                        backStack.add(
                            Screen.DetailExerciseAdd(
                                selectedDate = screen.selectedDate,
                                selectedCategoryIds = screen.selectedCategoryIds,
                                sessionId = System.currentTimeMillis(),
                            )
                        )
                    },
                    onCompleteClick = {
                        popBackStack()
                        popBackStack()
                        popBackStack()
                    },
                    onExerciseStartClick = { exercise ->
                        backStack.add(
                            Screen.WorkoutSetEditor(
                                selectedDate = screen.selectedDate,
                                exerciseId = exercise.id,
                                exerciseName = exercise.name,
                                sessionId = screen.sessionId,
                            )
                        )
                    },
                )
            }
            entry<Screen.WorkoutSetEditor> { screen ->
                WorkoutSetEditorScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    exerciseId = screen.exerciseId,
                    exerciseName = screen.exerciseName,
                    sessionId = screen.sessionId,
                    onBackClick = popBackStack,
                    onCompleteClick = popBackStack,
                )
            }
            entry<Screen.Settings> {
                SettingsScreen(
                    onBackClick = popBackStack,
                )
            }
        },
    )
}
