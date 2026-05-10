package com.jaemin.fitzam.ui.screen.exercisecategoryselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle,
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

    private val _selectedCategoryIds = MutableStateFlow(
        savedStateHandle.get<LongArray>(SELECTED_CATEGORY_IDS_KEY)
            ?.toSet()
            ?: emptySet()
    )
    val selectedCategoryIds = _selectedCategoryIds.asStateFlow()

    private var loadedDate: LocalDate? = savedStateHandle.get<String>(LOADED_DATE_KEY)?.let { value ->
        LocalDate.parse(value)
    }

    fun loadSelectedCategories(date: LocalDate) {
        if (loadedDate == date) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val selectedIds = exerciseCategoryRepository.getExerciseCategoryIds(date).toSet()
            _selectedCategoryIds.value = selectedIds
            loadedDate = date
            saveSelectedState(date = date, selectedIds = selectedIds)
        }
    }

    fun toggleCategory(id: Long) {
        _selectedCategoryIds.value = if (_selectedCategoryIds.value.contains(id)) {
            _selectedCategoryIds.value - id
        } else {
            _selectedCategoryIds.value + id
        }
        loadedDate?.let { date ->
            saveSelectedState(date = date, selectedIds = _selectedCategoryIds.value)
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

    private fun saveSelectedState(date: LocalDate, selectedIds: Set<Long>) {
        savedStateHandle[LOADED_DATE_KEY] = date.toString()
        savedStateHandle[SELECTED_CATEGORY_IDS_KEY] = selectedIds.toLongArray()
    }

    companion object {
        private const val SELECTED_CATEGORY_IDS_KEY = "selected_category_ids"
        private const val LOADED_DATE_KEY = "loaded_date"
    }
}

sealed interface ExerciseCategorySelectUiState {
    data object Loading : ExerciseCategorySelectUiState

    data object Failed : ExerciseCategorySelectUiState

    data class Success(
        val exerciseCategories: List<ExerciseCategory>
    ) : ExerciseCategorySelectUiState
}
