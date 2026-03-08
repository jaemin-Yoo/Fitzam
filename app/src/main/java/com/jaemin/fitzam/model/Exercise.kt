package com.jaemin.fitzam.model

data class Exercise(
    val id: Long,
    val name: String,
    val category: ExerciseCategory,
    val imageName: String,
    val recordSchema: ExerciseRecordSchema = ExerciseRecordSchema.WEIGHT_REPS,
)
