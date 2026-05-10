package com.jaemin.fitzam.model

data class WorkoutSet(
    val index: Int,
    val metrics: Map<WorkoutMetricType, Double>,
)
