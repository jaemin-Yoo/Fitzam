package com.jaemin.fitzam.ui.screen.workoutrecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.data.repository.WorkoutExerciseDraft
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

data class WorkoutRecordExerciseUiModel(
    val workoutExerciseId: Long,
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

    private val _exerciseItems = MutableStateFlow<List<WorkoutRecordExerciseUiModel>>(emptyList())
    val exerciseItems = _exerciseItems.asStateFlow()
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges = _hasUnsavedChanges.asStateFlow()
    private var initializedDate: LocalDate? = null
    private var initializedCategoryIds: Set<Long>? = null
    private var initializedExerciseIds: Set<Long>? = null
    private var originalExerciseItems: List<WorkoutRecordExerciseUiModel> = emptyList()
    private var isWorkoutLoaded = false

    fun loadWorkoutForDate(selectedDate: LocalDate) {
        if (initializedDate == selectedDate && isWorkoutLoaded) {
            _uiState.value = WorkoutRecordUiState.Success(
                exercises = _exerciseItems.value.map { item -> item.exercise },
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = WorkoutRecordUiState.Loading
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    workoutRepository.getWorkoutExercises(selectedDate)
                        .first()
                        .map { workoutExercise ->
                            val metricTypes = workoutExercise.exercise.metricTypes
                            WorkoutRecordExerciseUiModel(
                                workoutExerciseId = workoutExercise.id,
                                exercise = workoutExercise.exercise,
                                sets = workoutExercise.sets.map { set ->
                                    set.toEditableSet(metricTypes)
                                },
                            )
                        }
                }
            }
            _uiState.value = result.fold(
                onSuccess = { items ->
                    _exerciseItems.value = items
                    originalExerciseItems = items
                    _hasUnsavedChanges.value = false
                    initializedDate = selectedDate
                    initializedCategoryIds = items.map { item -> item.exercise.category.id }.toSet()
                    initializedExerciseIds = items.map { item -> item.exercise.id }.toSet()
                    isWorkoutLoaded = true
                    WorkoutRecordUiState.Success(exercises = items.map { item -> item.exercise })
                },
                onFailure = {
                    _exerciseItems.value = emptyList()
                    originalExerciseItems = emptyList()
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
                        selectedCategoryIds = selectedCategoryIds,
                        selectedExerciseIds = selectedExerciseIds,
                    )
                }
            }
            _uiState.value = result.fold(
                onSuccess = { items ->
                    _exerciseItems.value = items
                    originalExerciseItems = items
                    _hasUnsavedChanges.value = false
                    initializedDate = selectedDate
                    initializedCategoryIds = selectedCategoryIds
                    initializedExerciseIds = selectedExerciseIds
                    WorkoutRecordUiState.Success(exercises = items.map { item -> item.exercise })
                },
                onFailure = {
                    _exerciseItems.value = emptyList()
                    originalExerciseItems = emptyList()
                    _hasUnsavedChanges.value = false
                    initializedDate = null
                    initializedCategoryIds = null
                    initializedExerciseIds = null
                    WorkoutRecordUiState.Failed
                },
            )
        }
    }

    fun getExercise(workoutExerciseId: Long): Exercise? {
        return _exerciseItems.value.firstOrNull { item ->
            item.workoutExerciseId == workoutExerciseId
        }?.exercise
    }

    fun updateSetMetric(
        workoutExerciseId: Long,
        setIndex: Int,
        metricType: WorkoutMetricType,
        value: String,
    ) {
        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.workoutExerciseId != workoutExerciseId) {
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

    fun deleteSet(workoutExerciseId: Long, setIndex: Int) {
        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.workoutExerciseId != workoutExerciseId) {
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

    fun deleteExercise(workoutExerciseId: Long) {
        _exerciseItems.value = _exerciseItems.value.filterNot { item ->
            item.workoutExerciseId == workoutExerciseId
        }
        updateDirtyState()
    }

    fun moveExerciseUp(workoutExerciseId: Long) {
        moveExerciseBy(workoutExerciseId = workoutExerciseId, offset = -1)
    }

    fun moveExerciseDown(workoutExerciseId: Long) {
        moveExerciseBy(workoutExerciseId = workoutExerciseId, offset = 1)
    }

    fun getEditorInitialValue(workoutExerciseId: Long): WorkoutStartInitialValue {
        val item = _exerciseItems.value.firstOrNull { exerciseItem ->
            exerciseItem.workoutExerciseId == workoutExerciseId
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
        workoutExerciseId: Long,
        metricTypes: List<WorkoutMetricType>,
        metricValues: Map<WorkoutMetricType, String>,
    ) {
        val exerciseId = _exerciseItems.value.firstOrNull { item ->
            item.workoutExerciseId == workoutExerciseId
        }?.exercise?.id

        _exerciseItems.value = _exerciseItems.value.map { item ->
            if (item.workoutExerciseId != workoutExerciseId) {
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

        if (exerciseId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                exerciseRepository.updateExerciseMetricTypes(
                    exerciseId = exerciseId,
                    metricTypes = metricTypes,
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
                    workoutRepository.replaceWorkoutExercises(
                        date = selectedDate,
                        exercises = saveTarget,
                    )
                }
            }.onSuccess {
                originalExerciseItems = _exerciseItems.value
                _hasUnsavedChanges.value = false
                isWorkoutLoaded = true
                onSuccess()
            }
        }
    }

    fun discardDraftChanges() {
        _exerciseItems.value = originalExerciseItems
        _hasUnsavedChanges.value = false
        _uiState.value = WorkoutRecordUiState.Success(
            exercises = originalExerciseItems.map { item -> item.exercise },
        )
    }

    private suspend fun buildInitialItems(
        selectedDate: LocalDate,
        selectedCategoryIds: Set<Long>,
        selectedExerciseIds: Set<Long>,
    ): List<WorkoutRecordExerciseUiModel> {
        val savedWorkoutExercises = workoutRepository.getWorkoutExercises(selectedDate).first()
            .filter { workoutExercise ->
                workoutExercise.exercise.category.id in selectedCategoryIds
            }
        val savedExerciseMap = savedWorkoutExercises.associateBy { workoutExercise ->
            workoutExercise.exercise.id
        }

        val mergedExerciseIds = (savedExerciseMap.keys + selectedExerciseIds).toSet()
        val exercisesById = exerciseRepository.getExercisesByIds(mergedExerciseIds)
            .filter { exercise ->
                exercise.category.id in selectedCategoryIds
            }
            .associateBy { exercise ->
                exercise.id
            }

        val mergedOrderedIds = buildList {
            addAll(savedWorkoutExercises.map { savedExercise -> savedExercise.exercise.id })
            addAll(selectedExerciseIds.filterNot { selectedId -> selectedId in savedExerciseMap.keys })
        }

        return mergedOrderedIds.mapNotNull { exerciseId ->
            val savedExercise = savedExerciseMap[exerciseId]
            val exercise = savedExercise?.exercise ?: exercisesById[exerciseId]
            if (exercise == null || exercise.category.id !in selectedCategoryIds) {
                return@mapNotNull null
            }
            val metricTypes = savedExercise?.exercise?.metricTypes
                ?: workoutRepository.getLatestMetricTypes(exerciseId)
                ?: exercise.metricTypes
            WorkoutRecordExerciseUiModel(
                workoutExerciseId = savedExercise?.id ?: -(exerciseId + 1),
                exercise = exercise.copy(metricTypes = metricTypes),
                sets = savedExercise?.sets
                    ?.map { set ->
                        set.toEditableSet(metricTypes)
                    }
                    .orEmpty(),
            )
        }
    }

    private fun moveExerciseBy(workoutExerciseId: Long, offset: Int) {
        val currentItems = _exerciseItems.value
        val currentIndex = currentItems.indexOfFirst { item ->
            item.workoutExerciseId == workoutExerciseId
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
        updateDirtyState()
    }

    private fun updateDirtyState() {
        _hasUnsavedChanges.value = _exerciseItems.value != originalExerciseItems
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
