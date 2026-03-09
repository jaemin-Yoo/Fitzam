package com.jaemin.fitzam.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    val repository: WorkoutRepository,
) : ViewModel() {
    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts = _workouts.asStateFlow()
    private var workoutsJob: Job? = null
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val selectedDateWorkoutExercises = selectedDate.flatMapLatest { date ->
        repository.getWorkoutExercises(date)
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList<WorkoutExercise>(),
    )

    fun loadWorkoutsForYearMonth(yearMonth: YearMonth) {
        workoutsJob?.cancel()
        workoutsJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getWorkoutsForYearMonth(yearMonth).collect { workouts ->
                _workouts.value = workouts
            }
        }
    }

    fun onSelectedDateChanged(date: LocalDate) {
        if (selectedDate.value == date) {
            return
        }
        selectedDate.value = date
    }
}
