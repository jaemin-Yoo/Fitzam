package com.jaemin.fitzam.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseCategoryRepository
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.model.ExerciseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ExerciseCategorySelectViewModel @Inject constructor(
    val workoutRepository: WorkoutRepository,
    val exerciseCategoryRepository: ExerciseCategoryRepository,
) : ViewModel() {

    private val _exerciseCategories = MutableStateFlow<List<ExerciseCategory>>(emptyList())
    val exerciseCategories: StateFlow<List<ExerciseCategory>> = _exerciseCategories.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _exerciseCategories.value = exerciseCategoryRepository.getExerciseCategories()
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
            workoutRepository.insertWorkout(
                date = date,
                categoryIds = _selectedIds.value.toList(),
            )
        }
    }
}
