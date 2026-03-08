package com.jaemin.fitzam.ui.screen.exerciseselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExerciseSelectViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    companion object {
        private const val MIN_LOADING_DURATION_MS = 300L
    }

    private val _uiState = MutableStateFlow<ExerciseSelectUiState>(
        ExerciseSelectUiState.Loading,
    )
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ExerciseSelectEvent>()
    val event = _event.asSharedFlow()
    private val _selectedExerciseIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedExerciseIds = _selectedExerciseIds.asStateFlow()
    private var lastLoadedDate: LocalDate? = null
    private var lastLoadedCategoryIds: Set<Long>? = null
    private var hasInitializedSelection = false

    fun toggleSelectedExercise(exerciseId: Long) {
        _selectedExerciseIds.update { selectedIds ->
            if (selectedIds.contains(exerciseId)) {
                selectedIds - exerciseId
            } else {
                selectedIds + exerciseId
            }
        }
    }

    fun loadExercises(
        selectedDate: LocalDate,
        selectedCategoryIds: Set<Long>,
        incomingSelectedExerciseIds: Set<Long>,
    ) {
        if (lastLoadedDate != selectedDate) {
            hasInitializedSelection = false
        }
        val shouldSkipReload = _uiState.value is ExerciseSelectUiState.Success &&
            lastLoadedDate == selectedDate &&
            lastLoadedCategoryIds == selectedCategoryIds
        if (shouldSkipReload) return

        viewModelScope.launch {
            _uiState.value = ExerciseSelectUiState.Loading
            val startedAt = System.currentTimeMillis()

            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val exercises = exerciseRepository.getExercisesByCategoryIds(selectedCategoryIds)
                    val favoriteIds = exerciseRepository.getFavoriteExerciseIds()
                    val savedExerciseIds = workoutRepository.getWorkoutExercises(selectedDate)
                        .first()
                        .map { workoutExercise -> workoutExercise.exercise.id }
                        .toSet()
                    ExerciseLoadResult(
                        uiState = ExerciseSelectUiState.Success(
                            exercises = exercises,
                            favoriteIds = favoriteIds,
                        ),
                        savedExerciseIds = savedExerciseIds,
                    )
                }
            }

            val elapsed = System.currentTimeMillis() - startedAt
            if (elapsed < MIN_LOADING_DURATION_MS) {
                delay(MIN_LOADING_DURATION_MS - elapsed)
            }

            val loadResult = result.getOrNull()
            if (loadResult == null) {
                _uiState.value = ExerciseSelectUiState.Failed
                return@launch
            }

            _uiState.value = loadResult.uiState
            if (_uiState.value is ExerciseSelectUiState.Success) {
                val loadedExerciseIds = loadResult.uiState
                    .exercises
                    .map { exercise -> exercise.id }
                    .toSet()
                val initialSelectedExerciseIds = (loadResult.savedExerciseIds + incomingSelectedExerciseIds)
                    .intersect(loadedExerciseIds)
                _selectedExerciseIds.update { currentSelectedIds ->
                    if (!hasInitializedSelection) {
                        hasInitializedSelection = true
                        initialSelectedExerciseIds
                    } else {
                        currentSelectedIds.intersect(loadedExerciseIds)
                    }
                }
                lastLoadedDate = selectedDate
                lastLoadedCategoryIds = selectedCategoryIds
            }
        }
    }

    fun toggleFavorite(exerciseId: Long) {
        val currentState = _uiState.value as? ExerciseSelectUiState.Success ?: return
        val wasFavorite = currentState.favoriteIds.contains(exerciseId)
        val updatedFavoriteIds = if (wasFavorite) {
            currentState.favoriteIds - exerciseId
        } else {
            currentState.favoriteIds + exerciseId
        }

        _uiState.update { state ->
            if (state is ExerciseSelectUiState.Success) {
                state.copy(favoriteIds = updatedFavoriteIds)
            } else {
                state
            }
        }

        viewModelScope.launch {
            val saveResult = runCatching {
                withContext(Dispatchers.IO) {
                    if (wasFavorite) {
                        exerciseRepository.removeFavoriteExercise(exerciseId)
                    } else {
                        exerciseRepository.addFavoriteExercise(exerciseId)
                    }
                }
            }

            if (saveResult.isFailure) {
                _uiState.update { state ->
                    if (state is ExerciseSelectUiState.Success) {
                        state.copy(favoriteIds = currentState.favoriteIds)
                    } else {
                        state
                    }
                }
                _event.emit(ExerciseSelectEvent.FavoriteSaveFailed)
            }
        }
    }
}

private data class ExerciseLoadResult(
    val uiState: ExerciseSelectUiState.Success,
    val savedExerciseIds: Set<Long>,
)

sealed interface ExerciseSelectEvent {
    data object FavoriteSaveFailed : ExerciseSelectEvent
}

sealed interface ExerciseSelectUiState {
    data object Loading : ExerciseSelectUiState

    data object Failed : ExerciseSelectUiState

    data class Success(
        val exercises: List<Exercise>,
        val favoriteIds: Set<Long>,
    ) : ExerciseSelectUiState
}
