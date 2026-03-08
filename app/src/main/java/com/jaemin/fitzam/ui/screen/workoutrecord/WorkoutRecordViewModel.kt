package com.jaemin.fitzam.ui.screen.workoutrecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseRecordSchema
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
    val firstMetricText: String,
    val secondMetricText: String,
)

data class WorkoutRecordExerciseUiModel(
    val exercise: Exercise,
    val sets: List<EditableWorkoutSetUi>,
)

data class WorkoutStartInitialValue(
    val firstValue: Double,
    val secondValue: Int,
)

@HiltViewModel
class WorkoutRecordViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutRecordUiState>(WorkoutRecordUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _exerciseItems = MutableStateFlow<List<WorkoutRecordExerciseUiModel>>(emptyList())
    val exerciseItems = _exerciseItems.asStateFlow()

    fun loadExercises(selectedExerciseIds: Set<Long>) {
        viewModelScope.launch {
            _uiState.value = WorkoutRecordUiState.Loading
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    exerciseRepository.getExercisesByIds(selectedExerciseIds)
                }
            }
            _uiState.value = result.fold(
                onSuccess = { exercises ->
                    val previousMap = _exerciseItems.value.associateBy { item -> item.exercise.id }
                    _exerciseItems.value = exercises.map { exercise ->
                        previousMap[exercise.id] ?: WorkoutRecordExerciseUiModel(
                            exercise = exercise,
                            sets = emptyList(),
                        )
                    }
                    WorkoutRecordUiState.Success(exercises = exercises)
                },
                onFailure = {
                    _exerciseItems.value = emptyList()
                    WorkoutRecordUiState.Failed
                },
            )
        }
    }

    fun getExercise(exerciseId: Long): Exercise? {
        return _exerciseItems.value.firstOrNull { item ->
            item.exercise.id == exerciseId
        }?.exercise
    }

    fun updateSetFirstMetric(exerciseId: Long, setIndex: Int, value: String) {
        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.exercise.id != exerciseId) {
                item
            } else {
                item.copy(
                    sets = item.sets.map { set ->
                        if (set.index == setIndex) {
                            set.copy(firstMetricText = value)
                        } else {
                            set
                        }
                    },
                )
            }
        }
    }

    fun updateSetSecondMetric(exerciseId: Long, setIndex: Int, value: String) {
        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.exercise.id != exerciseId) {
                item
            } else {
                item.copy(
                    sets = item.sets.map { set ->
                        if (set.index == setIndex) {
                            set.copy(secondMetricText = value)
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

    fun getEditorInitialValue(exerciseId: Long): WorkoutStartInitialValue {
        val item = _exerciseItems.value.firstOrNull { exerciseItem ->
            exerciseItem.exercise.id == exerciseId
        }
        val lastSet = item?.sets?.lastOrNull()
        val firstValue = lastSet?.firstMetricText?.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val secondValue = lastSet?.secondMetricText?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return WorkoutStartInitialValue(
            firstValue = firstValue,
            secondValue = secondValue,
        )
    }

    fun appendSet(exerciseId: Long, firstValue: Double, secondValue: Int) {
        val normalizedFirst = firstValue.coerceAtLeast(0.0)
        val normalizedSecond = secondValue.coerceAtLeast(0)

        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.exercise.id != exerciseId) {
                item
            } else {
                val nextIndex = item.sets.size + 1
                val appendedSet = EditableWorkoutSetUi(
                    index = nextIndex,
                    firstMetricText = formatMetricFirstText(item.exercise.recordSchema, normalizedFirst),
                    secondMetricText = normalizedSecond.toString(),
                )
                item.copy(sets = item.sets + appendedSet)
            }
        }
    }
}

sealed interface WorkoutRecordUiState {
    data object Loading : WorkoutRecordUiState

    data object Failed : WorkoutRecordUiState

    data class Success(
        val exercises: List<Exercise>,
    ) : WorkoutRecordUiState
}

fun formatWeightText(weightKg: Double): String {
    return if (weightKg % 1.0 == 0.0) {
        weightKg.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", weightKg).trimEnd('0').trimEnd('.')
    }
}

private fun formatMetricFirstText(recordSchema: ExerciseRecordSchema, value: Double): String {
    return when (recordSchema) {
        ExerciseRecordSchema.WEIGHT_REPS -> formatWeightText(value)
        ExerciseRecordSchema.DISTANCE_DURATION -> formatWeightText(value)
    }
}
