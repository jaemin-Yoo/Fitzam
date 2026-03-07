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
import java.util.Locale

data class EditableWorkoutSetUi(
    val index: Int,
    val weightText: String,
    val repsText: String,
)

data class WorkoutAddExerciseUiModel(
    val exercise: Exercise,
    val sets: List<EditableWorkoutSetUi>,
)

data class WorkoutSetEditorInitialValue(
    val weightKg: Double,
    val reps: Int,
)

@HiltViewModel
class WorkoutAddViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutAddUiState>(WorkoutAddUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _exerciseItems = MutableStateFlow<List<WorkoutAddExerciseUiModel>>(emptyList())
    val exerciseItems = _exerciseItems.asStateFlow()

    fun loadExercises(selectedExerciseIds: Set<Long>) {
        viewModelScope.launch {
            _uiState.value = WorkoutAddUiState.Loading
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    exerciseRepository.getExercisesByIds(selectedExerciseIds)
                }
            }
            _uiState.value = result.fold(
                onSuccess = { exercises ->
                    val previousMap = _exerciseItems.value.associateBy { item -> item.exercise.id }
                    _exerciseItems.value = exercises.map { exercise ->
                        previousMap[exercise.id] ?: WorkoutAddExerciseUiModel(
                            exercise = exercise,
                            sets = emptyList(),
                        )
                    }
                    WorkoutAddUiState.Success(exercises = exercises)
                },
                onFailure = {
                    _exerciseItems.value = emptyList()
                    WorkoutAddUiState.Failed
                },
            )
        }
    }

    fun getExercise(exerciseId: Long): Exercise? {
        return _exerciseItems.value.firstOrNull { item ->
            item.exercise.id == exerciseId
        }?.exercise
    }

    fun updateSetWeight(exerciseId: Long, setIndex: Int, weight: String) {
        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.exercise.id != exerciseId) {
                item
            } else {
                item.copy(
                    sets = item.sets.map { set ->
                        if (set.index == setIndex) {
                            set.copy(weightText = weight)
                        } else {
                            set
                        }
                    },
                )
            }
        }
    }

    fun updateSetReps(exerciseId: Long, setIndex: Int, reps: String) {
        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.exercise.id != exerciseId) {
                item
            } else {
                item.copy(
                    sets = item.sets.map { set ->
                        if (set.index == setIndex) {
                            set.copy(repsText = reps)
                        } else {
                            set
                        }
                    },
                )
            }
        }
    }

    fun deleteSet(exerciseId: Long, setIndex: Int) {
        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.exercise.id != exerciseId) {
                item
            } else {
                val reindexedSets = item.sets
                    .filterNot { set -> set.index == setIndex }
                    .mapIndexed { index, set ->
                        set.copy(index = index + 1)
                    }
                item.copy(sets = reindexedSets)
            }
        }
    }

    fun deleteExercise(exerciseId: Long) {
        _exerciseItems.value = _exerciseItems.value.filterNot { item ->
            item.exercise.id == exerciseId
        }
    }

    fun getEditorInitialValue(exerciseId: Long): WorkoutSetEditorInitialValue {
        val lastSet = _exerciseItems.value.firstOrNull { item ->
            item.exercise.id == exerciseId
        }?.sets?.lastOrNull()

        val weight = lastSet?.weightText?.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val reps = lastSet?.repsText?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return WorkoutSetEditorInitialValue(weightKg = weight, reps = reps)
    }

    fun appendSet(exerciseId: Long, weightKg: Double, reps: Int) {
        val normalizedWeight = weightKg.coerceAtLeast(0.0)
        val normalizedReps = reps.coerceAtLeast(0)

        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.exercise.id != exerciseId) {
                item
            } else {
                val nextIndex = item.sets.size + 1
                val appendedSet = EditableWorkoutSetUi(
                    index = nextIndex,
                    weightText = formatWeightText(normalizedWeight),
                    repsText = normalizedReps.toString(),
                )
                item.copy(sets = item.sets + appendedSet)
            }
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

fun formatWeightText(weightKg: Double): String {
    return if (weightKg % 1.0 == 0.0) {
        weightKg.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", weightKg).trimEnd('0').trimEnd('.')
    }
}
