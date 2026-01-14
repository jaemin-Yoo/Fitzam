package com.jaemin.fitzam.ui.screen.exercisecategoryselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseCategoryRepository
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.model.ExerciseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExerciseCategorySelectViewModel @Inject constructor(
    val workoutRepository: WorkoutRepository,
    val exerciseCategoryRepository: ExerciseCategoryRepository,
) : ViewModel() {

    val exerciseCategorySelectUiState = flow {
        val result = runCatching { exerciseCategoryRepository.getExerciseCategories() }
        emit(
            result.fold(
                onSuccess = { categories -> ExerciseCategorySelectUiState.Success(categories) },
                onFailure = { ExerciseCategorySelectUiState.Failed },
            )
        )
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ExerciseCategorySelectUiState.Loading,
        )

    private val _selectedCategoryIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCategoryIds = _selectedCategoryIds.asStateFlow()

    fun loadSelectedCategories(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedCategoryIds.value = exerciseCategoryRepository.getExerciseCategoryIds(date).toSet()
        }
    }

    fun toggleCategory(id: Long) {
        _selectedCategoryIds.value = if (_selectedCategoryIds.value.contains(id)) {
            _selectedCategoryIds.value - id
        } else {
            _selectedCategoryIds.value + id
        }
    }

    fun applyWorkoutChanges(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.applyWorkoutChanges(
                categoryIds = _selectedCategoryIds.value.toList(),
                date = date,
            )
        }
    }
}

sealed interface ExerciseCategorySelectUiState {
    data object Loading : ExerciseCategorySelectUiState

    data object Failed : ExerciseCategorySelectUiState

    data class Success(
        val exerciseCategories: List<ExerciseCategory>
    ) : ExerciseCategorySelectUiState
}