package com.jaemin.fitzam.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseCategoryRepository
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.model.ExerciseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExerciseCategorySelectViewModel @Inject constructor(
    val workoutRepository: WorkoutRepository,
    val exerciseCategoryRepository: ExerciseCategoryRepository,
) : ViewModel() {

    private val _exerciseCategorySelectUiState = MutableStateFlow<ExerciseCategorySelectUiState>(
        ExerciseCategorySelectUiState.Loading
    )
    val exerciseCategorySelectUiState = _exerciseCategorySelectUiState.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { exerciseCategoryRepository.getExerciseCategories() }
                .onSuccess { categories ->
                    _exerciseCategorySelectUiState.value =
                        ExerciseCategorySelectUiState.Success(categories)
                }
                .onFailure {
                    _exerciseCategorySelectUiState.value = ExerciseCategorySelectUiState.Failed
                }
        }
    }

    fun loadSelectedCategories(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedIds.value = workoutRepository.getSelectedCategoryIds(date).toSet()
        }
    }

    fun togglePart(id: Long) {
        _selectedIds.value = if (_selectedIds.value.contains(id)) {
            _selectedIds.value - id
        } else {
            _selectedIds.value + id
        }
    }

    fun complete(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_selectedIds.value.isEmpty()) {
                workoutRepository.deleteWorkout(date)
            } else {
                workoutRepository.upsertWorkout(
                    date = date,
                    categoryIds = _selectedIds.value.toList(),
                )
            }
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