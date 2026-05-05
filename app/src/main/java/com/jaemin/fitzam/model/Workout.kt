package com.jaemin.fitzam.model

data class Workout(
    val id: Long,
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
)
