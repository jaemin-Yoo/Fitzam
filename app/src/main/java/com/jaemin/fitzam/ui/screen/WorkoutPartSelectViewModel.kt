package com.jaemin.fitzam.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.model.WorkoutPart
import com.jaemin.fitzam.model.WorkoutRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WorkoutPartSelectViewModel @Inject constructor(
    val repository: WorkoutRepository,
) : ViewModel() {

    private val _workoutParts = MutableStateFlow<List<WorkoutPart>>(emptyList())
    val workoutParts: StateFlow<List<WorkoutPart>> = _workoutParts.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _workoutParts.value = repository.getWorkoutParts()
        }
    }

    fun togglePart(id: Long) {
        _selectedIds.value = if (_selectedIds.value.contains(id)) {
            _selectedIds.value - id
        } else {
            _selectedIds.value + id
        }
    }

    fun complete(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertWorkoutRecord(
                date = date,
                partIds = _selectedIds.value.toList(),
            )
        }
    }
}
