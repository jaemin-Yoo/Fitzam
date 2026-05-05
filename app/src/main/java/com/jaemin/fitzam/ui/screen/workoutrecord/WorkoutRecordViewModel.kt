package com.jaemin.fitzam.ui.screen.workoutrecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.data.repository.WorkoutDraft
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.data.repository.WorkoutSetDraft
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.model.defaultMetricTypesForExercise
import com.jaemin.fitzam.ui.util.formatMetricValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class EditableWorkoutSetUi(
    val index: Int,
    val metricValues: Map<WorkoutMetricType, String>,
)

data class WorkoutUiModel(
    val workoutId: Long,
    val exercise: Exercise,
    val sets: List<EditableWorkoutSetUi>,
)

data class WorkoutStartInitialValue(
    val metricValues: Map<WorkoutMetricType, String>,
    val metricTypes: List<WorkoutMetricType>,
)

@HiltViewModel
class WorkoutRecordViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutRecordUiState>(WorkoutRecordUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _workoutItems = MutableStateFlow<List<WorkoutUiModel>>(emptyList())
    val workoutItems = _workoutItems.asStateFlow()
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges = _hasUnsavedChanges.asStateFlow()
    private var initializedDate: LocalDate? = null
    private var initializedCategoryIds: Set<Long>? = null
    private var initializedExerciseIds: Set<Long>? = null
    private var originalWorkoutItems: List<WorkoutUiModel> = emptyList()
    private var isWorkoutLoaded = false

    fun loadWorkoutForDate(selectedDate: LocalDate) {
        if (initializedDate == selectedDate && isWorkoutLoaded) {
            _uiState.value = WorkoutRecordUiState.Success(
                exercises = _workoutItems.value.map { item -> item.exercise },
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = WorkoutRecordUiState.Loading
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    workoutRepository.getWorkouts(selectedDate)
                        .first()
                        .map { workout ->
                            val metricTypes = workout.exercise.metricTypes
                            WorkoutUiModel(
                                workoutId = workout.id,
                                exercise = workout.exercise,
                                sets = workout.sets.map { set ->
                                    set.toEditableSet(metricTypes)
                                },
                            )
                        }
                }
            }
            _uiState.value = result.fold(
                onSuccess = { items ->
                    _workoutItems.value = items
                    originalWorkoutItems = items
                    _hasUnsavedChanges.value = false
                    initializedDate = selectedDate
                    initializedCategoryIds = items.map { item -> item.exercise.category.id }.toSet()
                    initializedExerciseIds = items.map { item -> item.exercise.id }.toSet()
                    isWorkoutLoaded = true
                    WorkoutRecordUiState.Success(exercises = items.map { item -> item.exercise })
                },
                onFailure = {
                    _workoutItems.value = emptyList()
                    originalWorkoutItems = emptyList()
                    _hasUnsavedChanges.value = false
                    initializedDate = null
                    initializedCategoryIds = null
                    initializedExerciseIds = null
                    isWorkoutLoaded = false
                    WorkoutRecordUiState.Failed
                },
            )
        }
    }

    fun loadExercises(
        selectedDate: LocalDate,
        selectedCategoryIds: Set<Long>,
        selectedExerciseIds: Set<Long>,
    ) {
        if (
            initializedDate == selectedDate &&
            initializedCategoryIds == selectedCategoryIds &&
            initializedExerciseIds == selectedExerciseIds
        ) {
            _uiState.value = WorkoutRecordUiState.Success(
                exercises = _workoutItems.value.map { item -> item.exercise },
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = WorkoutRecordUiState.Loading
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    buildInitialItems(
                        selectedDate = selectedDate,
                        selectedCategoryIds = selectedCategoryIds,
                        selectedExerciseIds = selectedExerciseIds,
                    )
                }
            }
            _uiState.value = result.fold(
                onSuccess = { items ->
                    _workoutItems.value = items
                    originalWorkoutItems = items
                    _hasUnsavedChanges.value = false
                    initializedDate = selectedDate
                    initializedCategoryIds = selectedCategoryIds
                    initializedExerciseIds = selectedExerciseIds
                    WorkoutRecordUiState.Success(exercises = items.map { item -> item.exercise })
                },
                onFailure = {
                    _workoutItems.value = emptyList()
                    originalWorkoutItems = emptyList()
                    _hasUnsavedChanges.value = false
                    initializedDate = null
                    initializedCategoryIds = null
                    initializedExerciseIds = null
                    WorkoutRecordUiState.Failed
                },
            )
        }
    }

    fun getExercise(workoutId: Long): Exercise? {
        return _workoutItems.value.firstOrNull { item ->
            item.workoutId == workoutId
        }?.exercise
    }

    fun updateSetMetric(
        workoutId: Long,
        setIndex: Int,
        metricType: WorkoutMetricType,
        value: String,
    ) {
        _workoutItems.value = _workoutItems.value.map { item ->
            if (item.workoutId != workoutId) {
                item
            } else {
                item.copy(
                    sets = item.sets.map { set ->
                        if (set.index == setIndex) {
                            set.copy(
                                metricValues = set.metricValues + (metricType to value),
                            )
                        } else {
                            set
                        }
                    },
                )
            }
        }
        updateDirtyState()
    }

    fun deleteSet(workoutId: Long, setIndex: Int) {
        _workoutItems.value = _workoutItems.value.map { item ->
            if (item.workoutId != workoutId) {
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
        updateDirtyState()
    }

    fun deleteWorkout(workoutId: Long) {
        _workoutItems.value = _workoutItems.value.filterNot { item ->
            item.workoutId == workoutId
        }
        updateDirtyState()
    }

    fun moveWorkoutUp(workoutId: Long) {
        moveWorkoutBy(workoutId = workoutId, offset = -1)
    }

    fun moveWorkoutDown(workoutId: Long) {
        moveWorkoutBy(workoutId = workoutId, offset = 1)
    }

    fun getEditorInitialValue(workoutId: Long): WorkoutStartInitialValue {
        val item = _workoutItems.value.firstOrNull { workoutItem ->
            workoutItem.workoutId == workoutId
        }
        val lastSet = item?.sets?.lastOrNull()
        val metricTypes = item?.exercise?.metricTypes ?: defaultMetricTypesForExercise(item?.exercise?.name)
        return WorkoutStartInitialValue(
            metricValues = metricTypes.associateWith { metricType ->
                lastSet?.metricValues?.get(metricType).orEmpty()
            },
            metricTypes = metricTypes,
        )
    }

    fun appendSet(
        workoutId: Long,
        metricTypes: List<WorkoutMetricType>,
        metricValues: Map<WorkoutMetricType, String>,
    ) {
        _workoutItems.value = _workoutItems.value.map { item ->
            if (item.workoutId != workoutId) {
                item
            } else {
                val nextIndex = item.sets.size + 1
                val appendedSet = EditableWorkoutSetUi(
                    index = nextIndex,
                    metricValues = metricTypes.associateWith { metricType ->
                        sanitizeMetricInput(
                            metricType = metricType,
                            value = metricValues[metricType].orEmpty(),
                        )
                    },
                )
                item.copy(
                    exercise = item.exercise.copy(metricTypes = metricTypes),
                    sets = item.sets + appendedSet,
                )
            }
        }
        updateDirtyState()
    }

    fun saveWorkout(
        selectedDate: LocalDate,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val saveTarget = _workoutItems.value.mapIndexed { index, item ->
                WorkoutDraft(
                    exerciseId = item.exercise.id,
                    categoryId = item.exercise.category.id,
                    orderIndex = index,
                    metricTypes = item.exercise.metricTypes,
                    sets = item.sets.map { set ->
                        WorkoutSetDraft(
                            setIndex = set.index,
                            metrics = item.exercise.metricTypes.associateWith { metricType ->
                                parseMetricInput(
                                    metricType = metricType,
                                    value = set.metricValues[metricType].orEmpty(),
                                )
                            },
                        )
                    },
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    workoutRepository.replaceWorkouts(
                        date = selectedDate,
                        workouts = saveTarget,
                    )
                    saveTarget.forEach { workout ->
                        exerciseRepository.updateExerciseMetricTypes(
                            exerciseId = workout.exerciseId,
                            metricTypes = workout.metricTypes,
                        )
                    }
                }
            }.onSuccess {
                originalWorkoutItems = _workoutItems.value
                _hasUnsavedChanges.value = false
                isWorkoutLoaded = true
                onSuccess()
            }
        }
    }

    fun discardDraftChanges() {
        _workoutItems.value = originalWorkoutItems
        _hasUnsavedChanges.value = false
        _uiState.value = WorkoutRecordUiState.Success(
            exercises = originalWorkoutItems.map { item -> item.exercise },
        )
    }

    private suspend fun buildInitialItems(
        selectedDate: LocalDate,
        selectedCategoryIds: Set<Long>,
        selectedExerciseIds: Set<Long>,
    ): List<WorkoutUiModel> {
        val savedWorkouts = workoutRepository.getWorkouts(selectedDate).first()
            .filter { workout ->
                workout.exercise.category.id in selectedCategoryIds
            }
        val savedWorkoutMap = savedWorkouts.associateBy { workout ->
            workout.exercise.id
        }

        val mergedExerciseIds = (savedWorkoutMap.keys + selectedExerciseIds).toSet()
        val exercisesById = exerciseRepository.getExercisesByIds(mergedExerciseIds)
            .filter { exercise ->
                exercise.category.id in selectedCategoryIds
            }
            .associateBy { exercise ->
                exercise.id
            }

        val mergedOrderedIds = buildList {
            addAll(savedWorkouts.map { savedWorkout -> savedWorkout.exercise.id })
            addAll(selectedExerciseIds.filterNot { selectedId -> selectedId in savedWorkoutMap.keys })
        }

        return mergedOrderedIds.mapNotNull { exerciseId ->
            val savedWorkout = savedWorkoutMap[exerciseId]
            val exercise = savedWorkout?.exercise ?: exercisesById[exerciseId]
            if (exercise == null || exercise.category.id !in selectedCategoryIds) {
                return@mapNotNull null
            }
            val metricTypes = savedWorkout?.exercise?.metricTypes
                ?: workoutRepository.getLatestMetricTypes(exerciseId)
                ?: exercise.metricTypes
            WorkoutUiModel(
                workoutId = savedWorkout?.id ?: -(exerciseId + 1),
                exercise = exercise.copy(metricTypes = metricTypes),
                sets = savedWorkout?.sets
                    ?.map { set ->
                        set.toEditableSet(metricTypes)
                    }
                    .orEmpty(),
            )
        }
    }

    private fun moveWorkoutBy(workoutId: Long, offset: Int) {
        val currentItems = _workoutItems.value
        val currentIndex = currentItems.indexOfFirst { item ->
            item.workoutId == workoutId
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
        _workoutItems.value = mutableItems.toList()
        updateDirtyState()
    }

    private fun updateDirtyState() {
        _hasUnsavedChanges.value = _workoutItems.value != originalWorkoutItems
    }
}

sealed interface WorkoutRecordUiState {
    data object Loading : WorkoutRecordUiState

    data object Failed : WorkoutRecordUiState

    data class Success(
        val exercises: List<Exercise>,
    ) : WorkoutRecordUiState
}

private fun com.jaemin.fitzam.model.WorkoutSet.toEditableSet(
    metricTypes: List<WorkoutMetricType>,
): EditableWorkoutSetUi {
    return EditableWorkoutSetUi(
        index = index,
        metricValues = metricTypes.associateWith { metricType ->
            when (metricType) {
                WorkoutMetricType.DURATION_SEC,
                WorkoutMetricType.REPS -> (metrics[metricType] ?: 0.0).toInt().toString()
                WorkoutMetricType.WEIGHT_KG,
                WorkoutMetricType.DISTANCE_KM -> formatMetricValue(metrics[metricType] ?: 0.0)
            }
        },
    )
}

private fun sanitizeMetricInput(
    metricType: WorkoutMetricType,
    value: String,
): String {
    return when (metricType) {
        WorkoutMetricType.DURATION_SEC,
        WorkoutMetricType.REPS -> (value.toIntOrNull() ?: 0).coerceAtLeast(0).toString()
        WorkoutMetricType.WEIGHT_KG,
        WorkoutMetricType.DISTANCE_KM -> formatMetricValue((value.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0))
    }
}

private fun parseMetricInput(
    metricType: WorkoutMetricType,
    value: String,
): Double {
    return when (metricType) {
        WorkoutMetricType.DURATION_SEC,
        WorkoutMetricType.REPS -> (value.toIntOrNull() ?: 0).coerceAtLeast(0).toDouble()
        WorkoutMetricType.WEIGHT_KG,
        WorkoutMetricType.DISTANCE_KM -> (value.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    }
}
