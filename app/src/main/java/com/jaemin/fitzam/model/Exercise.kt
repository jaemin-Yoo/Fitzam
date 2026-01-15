package com.jaemin.fitzam.model

data class Exercise(
    val id: Long,
    val name: String,
    val category: ExerciseCategory,
    val imageName: String,
)
