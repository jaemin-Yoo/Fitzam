package com.jaemin.fitzam.ui.screen.detailexerciseadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailExerciseAddViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {
    companion object {
        private const val MIN_LOADING_DURATION_MS = 300L
    }

    private val _uiState = MutableStateFlow<DetailExerciseAddUiState>(
        DetailExerciseAddUiState.Loading,
    )
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<DetailExerciseAddEvent>()
    val event = _event.asSharedFlow()

    fun loadExercises(selectedCategoryIds: Set<Long>) {
        viewModelScope.launch {
            _uiState.value = DetailExerciseAddUiState.Loading
            val startedAt = System.currentTimeMillis()

            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val exercises = exerciseRepository.getExercisesByCategoryIds(selectedCategoryIds)
                    val favoriteIds = exerciseRepository.getFavoriteExerciseIds()
                    DetailExerciseAddUiState.Success(
                        exercises = exercises,
                        favoriteIds = favoriteIds,
                    )
                }
            }

            val elapsed = System.currentTimeMillis() - startedAt
            if (elapsed < MIN_LOADING_DURATION_MS) {
                delay(MIN_LOADING_DURATION_MS - elapsed)
            }

            _uiState.value = result.getOrElse {
                DetailExerciseAddUiState.Failed
            }
        }
    }

    fun toggleFavorite(exerciseId: Long) {
        val currentState = _uiState.value as? DetailExerciseAddUiState.Success ?: return
        val wasFavorite = currentState.favoriteIds.contains(exerciseId)
        val updatedFavoriteIds = if (wasFavorite) {
            currentState.favoriteIds - exerciseId
        } else {
            currentState.favoriteIds + exerciseId
        }

        _uiState.update { state ->
            if (state is DetailExerciseAddUiState.Success) {
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
                    if (state is DetailExerciseAddUiState.Success) {
                        state.copy(favoriteIds = currentState.favoriteIds)
                    } else {
                        state
                    }
                }
                _event.emit(DetailExerciseAddEvent.FavoriteSaveFailed)
            }
        }
    }
}

sealed interface DetailExerciseAddEvent {
    data object FavoriteSaveFailed : DetailExerciseAddEvent
}

sealed interface DetailExerciseAddUiState {
    data object Loading : DetailExerciseAddUiState

    data object Failed : DetailExerciseAddUiState

    data class Success(
        val exercises: List<Exercise>,
        val favoriteIds: Set<Long>,
    ) : DetailExerciseAddUiState
}
