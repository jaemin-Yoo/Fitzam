package com.jaemin.fitzam.model

data class WorkoutEntry(
    val id: Long,
    val exerciseName: String,
    val partName: String,
    val sets: List<WorkoutSet>,
)
