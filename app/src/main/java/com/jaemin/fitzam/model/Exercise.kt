package com.jaemin.fitzam.model

data class Exercise(
    val id: Long,
    val name: String,
    val category: ExerciseCategory,
    val imageName: String,
    val equipmentType: ExerciseEquipmentType = ExerciseEquipmentType.OTHER,
    val metricTypes: List<WorkoutMetricType> = defaultMetricTypesForExercise(null),
)
