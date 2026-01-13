package com.jaemin.fitzam.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val repository: WorkoutRepository,
) : ViewModel() {
    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts = _workouts.asStateFlow()
    private var workoutsJob: Job? = null

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    private var currentYearMonth: YearMonth = YearMonth.from(_selectedDate.value)

    init {
        loadWorkoutsForMonth(currentYearMonth)
    }

    fun updateSelectedDate(date: LocalDate) {
        if (date == _selectedDate.value) {
            return
        }

        _selectedDate.value = date
    }

    fun updateDisplayedYearMonth(yearMonth: YearMonth) {
        if (yearMonth == currentYearMonth) {
            return
        }

        currentYearMonth = yearMonth
        loadWorkoutsForMonth(yearMonth)
    }

    private fun loadWorkoutsForMonth(yearMonth: YearMonth) {
        workoutsJob?.cancel()
        workoutsJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getWorkoutsForMonth(yearMonth).collect { workouts ->
                _workouts.value = workouts
            }
        }
    }
}
