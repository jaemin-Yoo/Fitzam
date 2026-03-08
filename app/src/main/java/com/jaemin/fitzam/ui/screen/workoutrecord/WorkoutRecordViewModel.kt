package com.jaemin.fitzam.ui.screen.workoutrecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.data.repository.WorkoutExerciseDraft
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.data.repository.WorkoutSetDraft
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseRecordSchema
import com.jaemin.fitzam.model.WorkoutMetricType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
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
    val recordSchema: ExerciseRecordSchema,
    val isSchemaLocked: Boolean,
)

@HiltViewModel
class WorkoutRecordViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutRecordUiState>(WorkoutRecordUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _exerciseItems = MutableStateFlow<List<WorkoutRecordExerciseUiModel>>(emptyList())
    val exerciseItems = _exerciseItems.asStateFlow()
    private var initializedDate: LocalDate? = null

    fun loadExercises(selectedDate: LocalDate, selectedExerciseIds: Set<Long>) {
        if (initializedDate == selectedDate) {
            _uiState.value = WorkoutRecordUiState.Success(
                exercises = _exerciseItems.value.map { item -> item.exercise },
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = WorkoutRecordUiState.Loading
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    buildInitialItems(
                        selectedDate = selectedDate,
                        selectedExerciseIds = selectedExerciseIds,
                    )
                }
            }
            _uiState.value = result.fold(
                onSuccess = { items ->
                    _exerciseItems.value = items
                    initializedDate = selectedDate
                    WorkoutRecordUiState.Success(exercises = items.map { item -> item.exercise })
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

    fun moveExerciseUp(exerciseId: Long) {
        moveExerciseBy(exerciseId = exerciseId, offset = -1)
    }

    fun moveExerciseDown(exerciseId: Long) {
        moveExerciseBy(exerciseId = exerciseId, offset = 1)
    }

    fun getEditorInitialValue(exerciseId: Long): WorkoutStartInitialValue {
        val item = _exerciseItems.value.firstOrNull { exerciseItem ->
            exerciseItem.exercise.id == exerciseId
        }
        val lastSet = item?.sets?.lastOrNull()
        val recordSchema = item?.exercise?.recordSchema ?: ExerciseRecordSchema.WEIGHT_REPS
        val firstValue = lastSet?.firstMetricText?.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val secondValue = lastSet?.secondMetricText?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return WorkoutStartInitialValue(
            firstValue = firstValue,
            secondValue = secondValue,
            recordSchema = recordSchema,
            isSchemaLocked = !item?.sets.isNullOrEmpty(),
        )
    }

    fun appendSet(
        exerciseId: Long,
        firstValue: Double,
        secondValue: Int,
        recordSchema: ExerciseRecordSchema,
    ) {
        val normalizedFirst = firstValue.coerceAtLeast(0.0)
        val normalizedSecond = secondValue.coerceAtLeast(0)

        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.exercise.id != exerciseId) {
                item
            } else {
                val nextIndex = item.sets.size + 1
                val appendedSet = EditableWorkoutSetUi(
                    index = nextIndex,
                    firstMetricText = formatMetricFirstText(recordSchema, normalizedFirst),
                    secondMetricText = normalizedSecond.toString(),
                )
                item.copy(
                    exercise = item.exercise.copy(recordSchema = recordSchema),
                    sets = item.sets + appendedSet,
                )
            }
        }
    }

    fun saveWorkout(
        selectedDate: LocalDate,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val saveTarget = _exerciseItems.value.mapIndexed { index, item ->
                WorkoutExerciseDraft(
                    exerciseId = item.exercise.id,
                    categoryId = item.exercise.category.id,
                    orderIndex = index,
                    recordSchema = item.exercise.recordSchema,
                    sets = item.sets.map { set ->
                        WorkoutSetDraft(
                            setIndex = set.index,
                            metrics = when (item.exercise.recordSchema) {
                                ExerciseRecordSchema.WEIGHT_REPS -> mapOf(
                                    WorkoutMetricType.WEIGHT_KG to (set.firstMetricText.toDoubleOrNull() ?: 0.0),
                                    WorkoutMetricType.REPS to (set.secondMetricText.toDoubleOrNull() ?: 0.0),
                                )

                                ExerciseRecordSchema.DISTANCE_DURATION -> mapOf(
                                    WorkoutMetricType.DISTANCE_KM to (set.firstMetricText.toDoubleOrNull() ?: 0.0),
                                    WorkoutMetricType.DURATION_SEC to (set.secondMetricText.toDoubleOrNull() ?: 0.0),
                                )
                            },
                        )
                    },
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    workoutRepository.replaceWorkoutExercises(
                        date = selectedDate,
                        exercises = saveTarget,
                    )
                }
            }.onSuccess {
                onSuccess()
            }
        }
    }

    private suspend fun buildInitialItems(
        selectedDate: LocalDate,
        selectedExerciseIds: Set<Long>,
    ): List<WorkoutRecordExerciseUiModel> {
        val savedWorkoutExercises = workoutRepository.getWorkoutExercises(selectedDate).first()
        val savedExerciseMap = savedWorkoutExercises.associateBy { workoutExercise ->
            workoutExercise.exercise.id
        }

        val mergedExerciseIds = (savedExerciseMap.keys + selectedExerciseIds).toSet()
        val exercisesById = exerciseRepository.getExercisesByIds(mergedExerciseIds).associateBy { exercise ->
            exercise.id
        }

        val mergedOrderedIds = buildList {
            addAll(savedWorkoutExercises.map { savedExercise -> savedExercise.exercise.id })
            addAll(selectedExerciseIds.filterNot { selectedId -> selectedId in savedExerciseMap.keys })
        }

        return mergedOrderedIds.mapNotNull { exerciseId ->
            val savedExercise = savedExerciseMap[exerciseId]
            val exercise = savedExercise?.exercise ?: exercisesById[exerciseId]
            val recordSchema = savedExercise?.exercise?.recordSchema
                ?: workoutRepository.getLatestRecordSchema(exerciseId)
                ?: exercise?.recordSchema
                ?: ExerciseRecordSchema.WEIGHT_REPS
            exercise?.let { existingExercise ->
                WorkoutRecordExerciseUiModel(
                    exercise = existingExercise.copy(recordSchema = recordSchema),
                    sets = savedExercise?.sets
                        ?.map { set ->
                            set.toEditableSet(recordSchema)
                        }
                        .orEmpty(),
                )
            }
        }
    }

    private fun moveExerciseBy(exerciseId: Long, offset: Int) {
        val currentItems = _exerciseItems.value
        val currentIndex = currentItems.indexOfFirst { item ->
            item.exercise.id == exerciseId
        }
        if (currentIndex < 0) {
            return
        }

        val targetIndex = currentIndex + offset
        if (targetIndex !in currentItems.indices) {
            return
        }

        val mutableItems = currentItems.toMutableList()
        val targetItem = mutableItems[targetIndex]
        mutableItems[targetIndex] = mutableItems[currentIndex]
        mutableItems[currentIndex] = targetItem
        _exerciseItems.value = mutableItems.toList()
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

private fun com.jaemin.fitzam.model.WorkoutSet.toEditableSet(
    recordSchema: ExerciseRecordSchema,
): EditableWorkoutSetUi {
    val firstValue = when (recordSchema) {
        ExerciseRecordSchema.WEIGHT_REPS -> metrics[WorkoutMetricType.WEIGHT_KG] ?: 0.0
        ExerciseRecordSchema.DISTANCE_DURATION -> metrics[WorkoutMetricType.DISTANCE_KM] ?: 0.0
    }
    val secondValue = when (recordSchema) {
        ExerciseRecordSchema.WEIGHT_REPS -> metrics[WorkoutMetricType.REPS] ?: 0.0
        ExerciseRecordSchema.DISTANCE_DURATION -> metrics[WorkoutMetricType.DURATION_SEC] ?: 0.0
    }
    return EditableWorkoutSetUi(
        index = index,
        firstMetricText = formatMetricFirstText(recordSchema, firstValue),
        secondMetricText = secondValue.toInt().toString(),
    )
}
