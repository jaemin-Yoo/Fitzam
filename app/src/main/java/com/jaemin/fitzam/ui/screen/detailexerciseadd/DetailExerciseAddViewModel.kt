package com.jaemin.fitzam.ui.screen.detailexerciseadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailExerciseAddViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailExerciseAddUiState>(
        DetailExerciseAddUiState.Loading,
    )
    val uiState = _uiState.asStateFlow()

    fun loadExercises(selectedCategoryIds: Set<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = DetailExerciseAddUiState.Loading

            val result = runCatching {
                val exercises = exerciseRepository.getExercisesByCategoryIds(selectedCategoryIds)
                val favoriteIds = exerciseRepository.getFavoriteExerciseIds()
                DetailExerciseAddUiState.Success(
                    exercises = exercises,
                    favoriteIds = favoriteIds,
                )
            }

            _uiState.value = result.getOrElse {
                DetailExerciseAddUiState.Failed
            }
        }
    }
}

sealed interface DetailExerciseAddUiState {
    data object Loading : DetailExerciseAddUiState

    data object Failed : DetailExerciseAddUiState

    data class Success(
        val exercises: List<Exercise>,
        val favoriteIds: Set<Long>,
    ) : DetailExerciseAddUiState
}
