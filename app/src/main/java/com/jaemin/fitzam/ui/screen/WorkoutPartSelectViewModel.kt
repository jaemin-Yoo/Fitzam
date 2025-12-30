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

    private val _selectedCodes = MutableStateFlow<Set<String>>(emptySet())
    val selectedCodes: StateFlow<Set<String>> = _selectedCodes.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _workoutParts.value = repository.getWorkoutParts()
        }
    }

    fun togglePart(code: String) {
        _selectedCodes.value = if (_selectedCodes.value.contains(code)) {
            _selectedCodes.value - code
        } else {
            _selectedCodes.value + code
        }
    }

    fun complete(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertWorkoutRecord(
                WorkoutRecord(
                    date = date,
                    partCodes = _selectedCodes.value.toList(),
                )
            )
        }
    }
}
