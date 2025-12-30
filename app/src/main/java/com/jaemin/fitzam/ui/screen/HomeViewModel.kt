package com.jaemin.fitzam.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.model.WorkoutRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val repository: WorkoutRepository,
) : ViewModel() {
    private val _workoutRecords = MutableStateFlow<List<WorkoutRecord>>(emptyList())
    val workoutRecords = _workoutRecords.asStateFlow()
    private var recordsJob: Job? = null

    init {
        loadRecordsForDate(LocalDate.now())
    }

    fun loadRecordsForDate(date: LocalDate) {
        val yearMonth = YearMonth.from(date)
        recordsJob?.cancel()
        recordsJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getWorkoutRecordsForMonth(yearMonth).collect { records ->
                _workoutRecords.value = records
            }
        }
    }
}
