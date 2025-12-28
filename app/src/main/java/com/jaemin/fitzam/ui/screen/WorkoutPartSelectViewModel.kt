package com.jaemin.fitzam.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaemin.fitzam.data.repository.WorkoutRepository
import com.jaemin.fitzam.model.WorkoutPart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutPartSelectViewModel @Inject constructor(
    val repository: WorkoutRepository,
) : ViewModel() {

    private val _workoutParts = MutableStateFlow<List<WorkoutPart>>(emptyList())
    val workoutParts: StateFlow<List<WorkoutPart>> = _workoutParts.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _workoutParts.value = repository.getWorkoutParts()
        }
    }
}
