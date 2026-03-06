package com.jaemin.fitzam.ui.screen.workoutadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class WorkoutAddViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutAddUiState>(WorkoutAddUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadExercises(selectedExerciseIds: Set<Long>) {
        viewModelScope.launch {
            _uiState.value = WorkoutAddUiState.Loading
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    exerciseRepository.getExercisesByIds(selectedExerciseIds)
                }
            }
            _uiState.value = result.fold(
                onSuccess = { exercises -> WorkoutAddUiState.Success(exercises = exercises) },
                onFailure = { WorkoutAddUiState.Failed },
            )
        }
    }
}

sealed interface WorkoutAddUiState {
    data object Loading : WorkoutAddUiState

    data object Failed : WorkoutAddUiState

    data class Success(
        val exercises: List<Exercise>,
    ) : WorkoutAddUiState
}
