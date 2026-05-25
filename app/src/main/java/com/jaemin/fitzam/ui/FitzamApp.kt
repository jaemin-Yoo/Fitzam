package com.jaemin.fitzam.ui

import androidx.compose.runtime.Composable
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import com.jaemin.fitzam.ui.screen.exerciseadd.ExerciseAddScreen
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
    @Serializable data class ExerciseSelect(
        val selectedDate: String,
        val selectedCategoryIds: String,
        val selectedExerciseIds: String = "",
        val preselectSavedExercises: Boolean = true,
        val closeScreenCount: Int = 2,
        val sessionId: Long,
    ) : NavKey
    @Serializable data class ExerciseAdd(
        val selectedCategoryIds: String,
        val sessionId: Long,
    ) : NavKey
    @Serializable data class WorkoutDetail(
        val selectedDate: String,
        val workoutId: Long,
        val exerciseId: Long,
        val sessionId: Long,
    ) : NavKey
    @Serializable data class WorkoutStart(
        val selectedDate: String,
        val workoutId: Long,
        val exerciseId: Long,
        val exerciseName: String,
        val sessionId: Long,
    ) : NavKey
}

@Composable
fun FitzamApp() {
    val backStack = rememberNavBackStack(Screen.Home)
    var exerciseSelectRefreshVersion by remember { mutableLongStateOf(0L) }
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
                    onWorkoutDetailClick = { selectedDate, workoutId, exerciseId ->
                        backStack.add(
                            Screen.WorkoutDetail(
                                selectedDate = selectedDate.toString(),
                                workoutId = workoutId,
                                exerciseId = exerciseId,
                                sessionId = System.currentTimeMillis(),
                            )
                        )
                    },
                    onSettingsClick = { backStack.add(Screen.Settings) },
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
                    refreshVersion = exerciseSelectRefreshVersion,
                    onBackClick = popBackStack,
                    onAddExerciseClick = {
                        backStack.add(
                            Screen.ExerciseAdd(
                                selectedCategoryIds = screen.selectedCategoryIds,
                                sessionId = System.currentTimeMillis(),
                            )
                        )
                    },
                    onCompleteClick = {
                        repeat(screen.closeScreenCount) {
                            popBackStack()
                        }
                    },
                )
            }
            entry<Screen.ExerciseAdd> { screen ->
                ExerciseAddScreen(
                    selectedCategoryIds = screen.selectedCategoryIds
                        .split(",")
                        .mapNotNull { value -> value.toLongOrNull() }
                        .toSet(),
                    sessionId = screen.sessionId,
                    onBackClick = popBackStack,
                    onExerciseAdded = {
                        exerciseSelectRefreshVersion += 1
                        popBackStack()
                    },
                )
            }
            entry<Screen.WorkoutDetail> { screen ->
                WorkoutDetailScreen(
                    selectedDate = LocalDate.parse(screen.selectedDate),
                    workoutId = screen.workoutId,
                    exerciseId = screen.exerciseId,
                    sessionId = screen.sessionId,
                    onDismissRequest = popBackStack,
                    onWorkoutStartClick = { workoutId, exerciseId, exerciseName ->
                        backStack.add(
                            Screen.WorkoutStart(
                                selectedDate = screen.selectedDate,
                                workoutId = workoutId,
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
                    workoutId = screen.workoutId,
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
