package com.jaemin.fitzam.ui

import androidx.compose.runtime.Composable
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import com.jaemin.fitzam.ui.screen.exercisecategoryselect.ExerciseCategorySelectScreen
import com.jaemin.fitzam.ui.screen.exerciseselect.ExerciseSelectScreen
import com.jaemin.fitzam.ui.screen.home.HomeScreen
import com.jaemin.fitzam.ui.screen.home.WorkoutDetailScreen
import com.jaemin.fitzam.ui.screen.settings.SettingsScreen
import com.jaemin.fitzam.ui.screen.workoutstart.WorkoutStartScreen
import java.time.LocalDate
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Home : NavKey
    @Serializable data object Settings : NavKey
    @Serializable data class ExerciseCategorySelect(
        val selectedDate: String,
        val sessionId: Long,
    ) : NavKey
    @Serializable data class ExerciseSelect(
        val selectedDate: String,
        val selectedCategoryIds: String,
        val selectedExerciseIds: String = "",
        val preselectSavedExercises: Boolean = true,
        val closeScreenCount: Int = 2,
        val sessionId: Long,
    ) : NavKey
    @Serializable data class WorkoutDetail(
        val selectedDate: String,
        val workoutExerciseId: Long,
        val exerciseId: Long,
        val sessionId: Long,
    ) : NavKey
    @Serializable data class WorkoutStart(
        val selectedDate: String,
        val workoutExerciseId: Long,
        val exerciseId: Long,
        val exerciseName: String,
        val sessionId: Long,
    ) : NavKey
}

@Composable
fun FitzamApp() {
    val backStack = rememberNavBackStack(Screen.Home)
    val pushSlideDurationMillis = 300
    val popSlideDurationMillis = 380
    val popBackStack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = popBackStack,
        transitionSpec = {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    animationSpec = tween(durationMillis = pushSlideDurationMillis),
                    initialOffsetX = { fullWidth -> fullWidth },
                ),
                initialContentExit = slideOutHorizontally(
                    animationSpec = tween(durationMillis = pushSlideDurationMillis),
                    targetOffsetX = { fullWidth -> -fullWidth },
                ),
            )
        },
        popTransitionSpec = {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    animationSpec = tween(durationMillis = popSlideDurationMillis),
                    initialOffsetX = { fullWidth -> -fullWidth },
                ),
                initialContentExit = slideOutHorizontally(
                    animationSpec = tween(durationMillis = popSlideDurationMillis),
                    targetOffsetX = { fullWidth -> fullWidth },
                ),
            )
        },
        predictivePopTransitionSpec = { swipeEdge ->
            val isRightEdgeSwipe = swipeEdge == NavigationEvent.EDGE_RIGHT
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    animationSpec = tween(durationMillis = popSlideDurationMillis),
                    initialOffsetX = { fullWidth -> if (isRightEdgeSwipe) fullWidth else -fullWidth },
                ),
                initialContentExit = slideOutHorizontally(
                    animationSpec = tween(durationMillis = popSlideDurationMillis),
                    targetOffsetX = { fullWidth -> if (isRightEdgeSwipe) -fullWidth else fullWidth },
                ),
            )
        },
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
                    onAddWorkout = { selectedDate, selectedCategoryIds ->
                        backStack.add(
                            Screen.ExerciseSelect(
                                selectedDate = selectedDate.toString(),
                                selectedCategoryIds = selectedCategoryIds.joinToString(","),
                                selectedExerciseIds = "",
                                preselectSavedExercises = false,
                                closeScreenCount = 1,
                                sessionId = System.currentTimeMillis(),
                            )
                        )
                    },
                    onWorkoutDetailClick = { selectedDate, workoutExerciseId, exerciseId ->
                        backStack.add(
                            Screen.WorkoutDetail(
                                selectedDate = selectedDate.toString(),
                                workoutExerciseId = workoutExerciseId,
                                exerciseId = exerciseId,
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
                            Screen.ExerciseSelect(
                                selectedDate = screen.selectedDate,
                                selectedCategoryIds = selectedCategoryIds.joinToString(","),
                                selectedExerciseIds = "",
                                preselectSavedExercises = true,
                                closeScreenCount = 2,
                                sessionId = System.currentTimeMillis(),
                            )
                        )
                    },
                    onCompleteClick = popBackStack,
                )
            }
            entry<Screen.ExerciseSelect> { screen ->
                ExerciseSelectScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    selectedCategoryIds = screen.selectedCategoryIds
                        .split(",")
                        .mapNotNull { value -> value.toLongOrNull() }
                        .toSet(),
                    selectedExerciseIds = screen.selectedExerciseIds
                        .split(",")
                        .mapNotNull { value -> value.toLongOrNull() }
                        .toSet(),
                    preselectSavedExercises = screen.preselectSavedExercises,
                    sessionId = screen.sessionId,
                    onBackClick = popBackStack,
                    onCompleteClick = {
                        repeat(screen.closeScreenCount) {
                            popBackStack()
                        }
                    },
                )
            }
            entry<Screen.WorkoutDetail> { screen ->
                WorkoutDetailScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    workoutExerciseId = screen.workoutExerciseId,
                    exerciseId = screen.exerciseId,
                    sessionId = screen.sessionId,
                    onDismissRequest = popBackStack,
                    onWorkoutStartClick = { workoutExerciseId, exerciseId, exerciseName ->
                        backStack.add(
                            Screen.WorkoutStart(
                                selectedDate = screen.selectedDate,
                                workoutExerciseId = workoutExerciseId,
                                exerciseId = exerciseId,
                                exerciseName = exerciseName,
                                sessionId = screen.sessionId,
                            )
                        )
                    },
                )
            }
            entry<Screen.WorkoutStart> { screen ->
                WorkoutStartScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    workoutExerciseId = screen.workoutExerciseId,
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
