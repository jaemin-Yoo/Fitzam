package com.jaemin.fitzam.model

import java.time.LocalDate

data class Workout(
    val date: LocalDate,
    val exerciseCategories: List<ExerciseCategory>,
)
