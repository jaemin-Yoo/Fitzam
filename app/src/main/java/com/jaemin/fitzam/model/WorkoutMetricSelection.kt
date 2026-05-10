package com.jaemin.fitzam.model

private val DefaultMetricTypes = listOf(
    WorkoutMetricType.WEIGHT_KG,
    WorkoutMetricType.REPS,
)

private val CardioMetricTypes = listOf(
    WorkoutMetricType.DISTANCE_KM,
    WorkoutMetricType.DURATION_SEC,
)

private val RopeMetricTypes = listOf(
    WorkoutMetricType.DURATION_SEC,
    WorkoutMetricType.REPS,
)

private val TimeOnlyMetricTypes = listOf(WorkoutMetricType.DURATION_SEC)

fun defaultMetricTypesForExercise(exerciseName: String?): List<WorkoutMetricType> {
    return when (exerciseName) {
        "러닝", "사이클", "로잉 머신" -> CardioMetricTypes
        "줄넘기" -> RopeMetricTypes
        "플랭크" -> TimeOnlyMetricTypes
        else -> DefaultMetricTypes
    }
}

fun parseMetricTypes(
    rawValue: String?,
    exerciseName: String? = null,
): List<WorkoutMetricType> {
    val trimmed = rawValue?.trim().orEmpty()
    if (trimmed.isBlank()) {
        return defaultMetricTypesForExercise(exerciseName)
    }

    return when (trimmed) {
        ExerciseRecordSchema.WEIGHT_REPS.name -> DefaultMetricTypes
        ExerciseRecordSchema.DISTANCE_DURATION.name -> CardioMetricTypes
        else -> {
            trimmed
                .split(",")
                .mapNotNull { token ->
                    runCatching { WorkoutMetricType.valueOf(token.trim()) }.getOrNull()
                }
                .distinct()
                .take(2)
                .ifEmpty { defaultMetricTypesForExercise(exerciseName) }
        }
    }
}

fun serializeMetricTypes(metricTypes: List<WorkoutMetricType>): String {
    return metricTypes
        .distinct()
        .take(2)
        .ifEmpty { DefaultMetricTypes }
        .joinToString(separator = ",") { metricType -> metricType.name }
}
