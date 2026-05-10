package com.jaemin.fitzam.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.WorkoutDraft
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.data.repository.WorkoutSetDraft
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WorkoutRepository,
) : ViewModel() {
    private val _workoutRecords = MutableStateFlow<List<WorkoutRecord>>(emptyList())
    val workoutRecords = _workoutRecords.asStateFlow()

    private val _selectedDateWorkouts = MutableStateFlow<List<Workout>>(emptyList())
    val selectedDateWorkouts = _selectedDateWorkouts.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private var workoutRecordsJob: Job? = null
    private var selectedDateWorkoutsJob: Job? = null
    private var latestLoadedWorkouts: List<Workout> = emptyList()
    private var selectedDate: LocalDate = LocalDate.now()

    fun loadWorkoutRecordsForYearMonth(yearMonth: YearMonth) {
        workoutRecordsJob?.cancel()
        workoutRecordsJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getWorkoutRecordsForYearMonth(yearMonth).collect { workoutRecords ->
                _workoutRecords.value = workoutRecords
            }
        }
    }

    fun onSelectedDateChanged(date: LocalDate) {
        if (selectedDate == date && selectedDateWorkoutsJob != null) {
            return
        }

        if (_isEditMode.value) {
            discardWorkoutEdit()
        }

        selectedDate = date
        selectedDateWorkoutsJob?.cancel()
        selectedDateWorkoutsJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getWorkouts(date).collect { workouts ->
                latestLoadedWorkouts = workouts
                if (!_isEditMode.value) {
                    _selectedDateWorkouts.value = workouts
                }
            }
        }
    }

    fun enterWorkoutEdit() {
        if (_selectedDateWorkouts.value.isEmpty() || _isEditMode.value) {
            return
        }
        _selectedDateWorkouts.value = latestLoadedWorkouts
        _isEditMode.value = true
    }

    fun discardWorkoutEdit() {
        _isEditMode.value = false
        _selectedDateWorkouts.value = latestLoadedWorkouts
    }

    fun moveWorkoutUp(workoutId: Long) {
        if (!_isEditMode.value) {
            return
        }
        moveWorkout(workoutId = workoutId, offset = -1)
    }

    fun moveWorkoutDown(workoutId: Long) {
        if (!_isEditMode.value) {
            return
        }
        moveWorkout(workoutId = workoutId, offset = 1)
    }

    fun deleteWorkout(workoutId: Long) {
        if (!_isEditMode.value) {
            return
        }
        _selectedDateWorkouts.value = _selectedDateWorkouts.value.filterNot { workout ->
            workout.id == workoutId
        }
    }

    fun saveWorkoutEdit(onSuccess: () -> Unit) {
        if (!_isEditMode.value) {
            return
        }

        val workoutsToSave = _selectedDateWorkouts.value
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.replaceWorkouts(
                        date = selectedDate,
                        workouts = workoutsToSave.mapIndexed { index, workout ->
                            WorkoutDraft(
                                exerciseId = workout.exercise.id,
                                categoryId = workout.exercise.category.id,
                                orderIndex = index,
                                metricTypes = workout.exercise.metricTypes,
                                sets = workout.sets.map { set ->
                                    WorkoutSetDraft(
                                        setIndex = set.index,
                                        metrics = set.metrics.filterKeys { metricType ->
                                            metricType in workout.exercise.metricTypes
                                        },
                                    )
                                },
                            )
                        },
                    )
                }
            }.onSuccess {
                _isEditMode.value = false
                latestLoadedWorkouts = workoutsToSave
                _selectedDateWorkouts.value = workoutsToSave
                onSuccess()
            }
        }
    }

    private fun moveWorkout(workoutId: Long, offset: Int) {
        val currentWorkouts = _selectedDateWorkouts.value
        val currentIndex = currentWorkouts.indexOfFirst { workout ->
            workout.id == workoutId
        }
        if (currentIndex < 0) {
            return
        }

        val targetIndex = currentIndex + offset
        if (targetIndex !in currentWorkouts.indices) {
            return
        }

        val reorderedWorkouts = currentWorkouts.toMutableList()
        val targetWorkout = reorderedWorkouts[targetIndex]
        reorderedWorkouts[targetIndex] = reorderedWorkouts[currentIndex]
        reorderedWorkouts[currentIndex] = targetWorkout
        _selectedDateWorkouts.value = reorderedWorkouts.toList()
    }
}
