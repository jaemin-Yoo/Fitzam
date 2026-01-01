package com.jaemin.fitzam.model

data class WorkoutExercise(
    val id: Long,
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
)
