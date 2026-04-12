package com.jaemin.fitzam.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.WorkoutExerciseDraft
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.data.repository.WorkoutSetDraft
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutExercise
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
    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts = _workouts.asStateFlow()

    private val _selectedDateWorkoutExercises = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val selectedDateWorkoutExercises = _selectedDateWorkoutExercises.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private var workoutsJob: Job? = null
    private var selectedDateExercisesJob: Job? = null
    private var latestLoadedWorkoutExercises: List<WorkoutExercise> = emptyList()
    private var selectedDate: LocalDate = LocalDate.now()

    fun loadWorkoutsForYearMonth(yearMonth: YearMonth) {
        workoutsJob?.cancel()
        workoutsJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getWorkoutsForYearMonth(yearMonth).collect { workouts ->
                _workouts.value = workouts
            }
        }
    }

    fun onSelectedDateChanged(date: LocalDate) {
        if (selectedDate == date && selectedDateExercisesJob != null) {
            return
        }

        if (_isEditMode.value) {
            discardExerciseEdit()
        }

        selectedDate = date
        selectedDateExercisesJob?.cancel()
        selectedDateExercisesJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getWorkoutExercises(date).collect { exercises ->
                latestLoadedWorkoutExercises = exercises
                if (!_isEditMode.value) {
                    _selectedDateWorkoutExercises.value = exercises
                }
            }
        }
    }

    fun enterExerciseEdit() {
        if (_selectedDateWorkoutExercises.value.isEmpty() || _isEditMode.value) {
            return
        }
        _selectedDateWorkoutExercises.value = latestLoadedWorkoutExercises
        _isEditMode.value = true
    }

    fun discardExerciseEdit() {
        _isEditMode.value = false
        _selectedDateWorkoutExercises.value = latestLoadedWorkoutExercises
    }

    fun moveExerciseUp(workoutExerciseId: Long) {
        if (!_isEditMode.value) {
            return
        }
        moveExercise(workoutExerciseId = workoutExerciseId, offset = -1)
    }

    fun moveExerciseDown(workoutExerciseId: Long) {
        if (!_isEditMode.value) {
            return
        }
        moveExercise(workoutExerciseId = workoutExerciseId, offset = 1)
    }

    fun deleteExercise(workoutExerciseId: Long) {
        if (!_isEditMode.value) {
            return
        }
        _selectedDateWorkoutExercises.value = _selectedDateWorkoutExercises.value.filterNot { workoutExercise ->
            workoutExercise.id == workoutExerciseId
        }
    }

    fun saveExerciseEdit(onSuccess: () -> Unit) {
        if (!_isEditMode.value) {
            return
        }

        val exercisesToSave = _selectedDateWorkoutExercises.value
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.replaceWorkoutExercises(
                        date = selectedDate,
                        exercises = exercisesToSave.mapIndexed { index, workoutExercise ->
                            WorkoutExerciseDraft(
                                exerciseId = workoutExercise.exercise.id,
                                categoryId = workoutExercise.exercise.category.id,
                                orderIndex = index,
                                metricTypes = workoutExercise.exercise.metricTypes,
                                sets = workoutExercise.sets.map { set ->
                                    WorkoutSetDraft(
                                        setIndex = set.index,
                                        metrics = set.metrics.filterKeys { metricType ->
                                            metricType in workoutExercise.exercise.metricTypes
                                        },
                                    )
                                },
                            )
                        },
                    )
                }
            }.onSuccess {
                _isEditMode.value = false
                latestLoadedWorkoutExercises = exercisesToSave
                _selectedDateWorkoutExercises.value = exercisesToSave
                onSuccess()
            }
        }
    }

    private fun moveExercise(workoutExerciseId: Long, offset: Int) {
        val currentExercises = _selectedDateWorkoutExercises.value
        val currentIndex = currentExercises.indexOfFirst { workoutExercise ->
            workoutExercise.id == workoutExerciseId
        }
        if (currentIndex < 0) {
            return
        }

        val targetIndex = currentIndex + offset
        if (targetIndex !in currentExercises.indices) {
            return
        }

        val reorderedExercises = currentExercises.toMutableList()
        val targetExercise = reorderedExercises[targetIndex]
        reorderedExercises[targetIndex] = reorderedExercises[currentIndex]
        reorderedExercises[currentIndex] = targetExercise
        _selectedDateWorkoutExercises.value = reorderedExercises.toList()
    }
}
