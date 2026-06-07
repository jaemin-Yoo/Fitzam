package com.jaemin.fitzam.ui.screen.exerciseselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.ExerciseRepository
import com.jaemin.fitzam.data.repository.WorkoutDraft
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.data.repository.WorkoutSetDraft
import com.jaemin.fitzam.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExerciseSelectViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    companion object {
        private const val MIN_LOADING_DURATION_MS = 300L
    }

    private val _uiState = MutableStateFlow<ExerciseSelectUiState>(
        ExerciseSelectUiState.Loading,
    )
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ExerciseSelectEvent>()
    val event = _event.asSharedFlow()
    private val _selectedExerciseIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedExerciseIds = _selectedExerciseIds.asStateFlow()
    private val _selectedFilterCategoryId = MutableStateFlow<Long?>(null)
    val selectedFilterCategoryId = _selectedFilterCategoryId.asStateFlow()
    private var lastLoadedDate: LocalDate? = null
    private var lastLoadedCategoryIds: Set<Long>? = null
    private var lastRefreshVersion: Long? = null
    private var hasInitializedSelection = false

    fun toggleSelectedExercise(exerciseId: Long) {
        _selectedExerciseIds.update { selectedIds ->
            if (selectedIds.contains(exerciseId)) {
                selectedIds - exerciseId
            } else {
                selectedIds + exerciseId
            }
        }
    }

    fun selectFilterCategory(categoryId: Long?) {
        _selectedFilterCategoryId.value = categoryId
    }

    fun loadExercises(
        selectedDate: LocalDate,
        selectedCategoryIds: Set<Long>,
        incomingSelectedExerciseIds: Set<Long>,
        preselectSavedExercises: Boolean,
        refreshVersion: Long,
    ) {
        if (lastLoadedDate != selectedDate) {
            hasInitializedSelection = false
        }
        val shouldSkipReload = _uiState.value is ExerciseSelectUiState.Success &&
            lastLoadedDate == selectedDate &&
            lastLoadedCategoryIds == selectedCategoryIds &&
            lastRefreshVersion == refreshVersion
        if (shouldSkipReload) return

        _selectedFilterCategoryId.value = null
        viewModelScope.launch {
            _uiState.value = ExerciseSelectUiState.Loading
            val startedAt = System.currentTimeMillis()

            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val exercises = exerciseRepository.getCustomExercisesByCategoryIds(selectedCategoryIds)
                    val savedExerciseIds = workoutRepository.getWorkouts(selectedDate)
                        .first()
                        .map { workout -> workout.exercise.id }
                        .toSet()
                    ExerciseLoadResult(
                        uiState = ExerciseSelectUiState.Success(
                            exercises = exercises,
                        ),
                        savedExerciseIds = savedExerciseIds,
                    )
                }
            }

            val elapsed = System.currentTimeMillis() - startedAt
            if (elapsed < MIN_LOADING_DURATION_MS) {
                delay(MIN_LOADING_DURATION_MS - elapsed)
            }

            val loadResult = result.getOrNull()
            if (loadResult == null) {
                _uiState.value = ExerciseSelectUiState.Failed
                return@launch
            }

            _uiState.value = loadResult.uiState
            if (_uiState.value is ExerciseSelectUiState.Success) {
                val loadedExerciseIds = loadResult.uiState
                    .exercises
                    .map { exercise -> exercise.id }
                    .toSet()
                val initialSelectedExerciseIds = (loadResult.savedExerciseIds + incomingSelectedExerciseIds)
                    .let { candidateIds ->
                        if (preselectSavedExercises) {
                            candidateIds
                        } else {
                            incomingSelectedExerciseIds
                        }
                    }
                    .intersect(loadedExerciseIds)
                _selectedExerciseIds.update { currentSelectedIds ->
                    if (!hasInitializedSelection) {
                        hasInitializedSelection = true
                        initialSelectedExerciseIds
                    } else {
                        currentSelectedIds.intersect(loadedExerciseIds)
                    }
                }
                lastLoadedDate = selectedDate
                lastLoadedCategoryIds = selectedCategoryIds
                lastRefreshVersion = refreshVersion
            }
        }
    }

    fun completeSelection(
        selectedDate: LocalDate,
        selectedCategoryIds: Set<Long>,
        preselectSavedExercises: Boolean,
        onSuccess: () -> Unit,
    ) {
        val selectedIds = _selectedExerciseIds.value
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val savedExercises = workoutRepository.getWorkouts(selectedDate)
                        .first()
                        .filter { workout ->
                            workout.exercise.category.id in selectedCategoryIds
                        }
                    val savedExerciseIds = savedExercises.map { workout ->
                        workout.exercise.id
                    }
                    val lookupExerciseIds = (savedExerciseIds + selectedIds).toSet()
                    val exercisesById = exerciseRepository.getExercisesByIds(lookupExerciseIds)
                        .filter { exercise ->
                            exercise.category.id in selectedCategoryIds
                        }
                        .associateBy { exercise ->
                            exercise.id
                        }
                    val drafts = buildList<WorkoutDraft> {
                        savedExercises.forEachIndexed { index, savedWorkout ->
                            val metricTypes = savedWorkout.exercise.metricTypes
                            add(
                                WorkoutDraft(
                                    exerciseId = savedWorkout.exercise.id,
                                    categoryId = savedWorkout.exercise.category.id,
                                    orderIndex = index,
                                    metricTypes = metricTypes,
                                    sets = savedWorkout.sets.map { set ->
                                        WorkoutSetDraft(
                                            setIndex = set.index,
                                            metrics = set.metrics.filterKeys { metricType ->
                                                metricType in metricTypes
                                            },
                                        )
                                    },
                                )
                            )
                        }

                        val appendedIds = if (preselectSavedExercises) {
                            selectedIds.filterNot { exerciseId -> exerciseId in savedExerciseIds }
                        } else {
                            selectedIds.toList()
                        }
                        appendedIds.forEachIndexed { appendIndex, exerciseId ->
                            val exercise = exercisesById[exerciseId] ?: return@forEachIndexed
                            val metricTypes = workoutRepository.getLatestMetricTypes(exerciseId)
                                ?: exercise.metricTypes
                            add(
                                WorkoutDraft(
                                    exerciseId = exercise.id,
                                    categoryId = exercise.category.id,
                                    orderIndex = savedExercises.size + appendIndex,
                                    metricTypes = metricTypes,
                                    sets = emptyList(),
                                )
                            )
                        }
                    }

                    workoutRepository.replaceWorkouts(
                        date = selectedDate,
                        workouts = drafts,
                        categoryIds = selectedCategoryIds.toList(),
                    )
                }
            }.onSuccess {
                onSuccess()
            }
        }
    }
}

data class ExerciseLoadResult(
    val uiState: ExerciseSelectUiState.Success,
    val savedExerciseIds: Set<Long>,
)

sealed interface ExerciseSelectEvent

sealed interface ExerciseSelectUiState {
    data object Loading : ExerciseSelectUiState

    data object Failed : ExerciseSelectUiState

    data class Success(
        val exercises: List<Exercise>,
    ) : ExerciseSelectUiState
}
