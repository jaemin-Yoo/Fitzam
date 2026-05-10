package com.jaemin.fitzam.model

enum class ExerciseEquipmentType(
    val displayName: String,
) {
    MACHINE("머신"),
    BARBELL("바벨"),
    DUMBBELL("덤벨"),
    KETTLEBELL("케틀벨"),
    BODYWEIGHT("맨몸"),
    OTHER("기타"),
}

fun parseExerciseEquipmentType(value: String?): ExerciseEquipmentType {
    return value
        ?.let { rawValue ->
            runCatching { ExerciseEquipmentType.valueOf(rawValue) }.getOrNull()
        }
        ?: ExerciseEquipmentType.OTHER
}

fun defaultEquipmentTypeForExercise(exerciseName: String?): ExerciseEquipmentType {
    val name = exerciseName.orEmpty()
    return when {
        name.contains("머신", ignoreCase = true) -> ExerciseEquipmentType.MACHINE
        name.contains("바벨", ignoreCase = true) -> ExerciseEquipmentType.BARBELL
        name.contains("덤벨", ignoreCase = true) -> ExerciseEquipmentType.DUMBBELL
        name.contains("케틀벨", ignoreCase = true) -> ExerciseEquipmentType.KETTLEBELL
        name in BODYWEIGHT_EXERCISE_NAMES -> ExerciseEquipmentType.BODYWEIGHT
        else -> ExerciseEquipmentType.OTHER
    }
}

private val BODYWEIGHT_EXERCISE_NAMES = setOf(
    "푸시업",
    "딥스",
    "크런치",
    "레그 레이즈",
    "플랭크",
    "바이시클 크런치",
    "러닝",
    "줄넘기",
)
