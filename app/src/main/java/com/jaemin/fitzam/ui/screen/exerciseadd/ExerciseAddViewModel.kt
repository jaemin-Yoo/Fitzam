package com.jaemin.fitzam.ui.screen.exerciseadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseCategoryRepository
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.model.ExerciseEquipmentType
import com.jaemin.fitzam.model.WorkoutMetricType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ExerciseAddViewModel @Inject constructor(
    private val exerciseCategoryRepository: ExerciseCategoryRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExerciseAddUiState>(ExerciseAddUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    fun loadCategories(selectedCategoryIds: Set<Long>) {
        if (_uiState.value is ExerciseAddUiState.Success) return

        viewModelScope.launch {
            _uiState.value = ExerciseAddUiState.Loading
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val categories = exerciseCategoryRepository.getExerciseCategories()
                    categories
                        .filter { category -> selectedCategoryIds.contains(category.id) }
                        .ifEmpty { categories }
                }
            }

            _uiState.value = result.fold(
                onSuccess = { categories ->
                    if (categories.isEmpty()) {
                        ExerciseAddUiState.Failed
                    } else {
                        ExerciseAddUiState.Success(categories)
                    }
                },
                onFailure = { ExerciseAddUiState.Failed },
            )
        }
    }

    fun addExercise(
        name: String,
        category: ExerciseCategory,
        equipmentType: ExerciseEquipmentType,
        metricTypes: List<WorkoutMetricType>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    exerciseRepository.addExercise(
                        name = name.trim(),
                        categoryId = category.id,
                        imageName = category.imageName,
                        equipmentType = equipmentType,
                        metricTypes = metricTypes,
                    )
                }
            }
            _isSaving.value = false

            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onFailure() },
            )
        }
    }
}

sealed interface ExerciseAddUiState {
    data object Loading : ExerciseAddUiState

    data object Failed : ExerciseAddUiState

    data class Success(
        val categories: List<ExerciseCategory>,
    ) : ExerciseAddUiState
}
