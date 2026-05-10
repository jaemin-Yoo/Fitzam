package com.jaemin.fitzam.data.mapper

import com.jaemin.fitzam.data.source.local.entity.ExerciseCategoryEntity
import com.jaemin.fitzam.data.source.local.entity.ExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseSetEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseSetMetricEntity
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutRecord
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.model.WorkoutSet
import com.jaemin.fitzam.model.parseExerciseEquipmentType
import com.jaemin.fitzam.model.parseMetricTypes
import java.time.LocalDate

fun WorkoutRecordEntity.toModel(exerciseCategories: List<ExerciseCategory>): WorkoutRecord {
    return WorkoutRecord(
        date = LocalDate.parse(date),
        exerciseCategories = exerciseCategories,
    )
}

fun ExerciseCategoryEntity.toModel(): ExerciseCategory {
    return ExerciseCategory(
        id = id,
        name = name,
        imageName = imageName,
        colorHex = colorHex,
        colorDarkHex = colorDarkHex,
    )
}

fun ExerciseEntity.toModel(category: ExerciseCategory): Exercise {
    return Exercise(
        id = id,
        name = name,
        category = category,
        imageName = imageName,
        equipmentType = parseExerciseEquipmentType(equipmentType),
        metricTypes = parseMetricTypes(recordSchema, exerciseName = name),
    )
}

fun WorkoutRecordExerciseSetEntity.toModel(
    metrics: List<WorkoutRecordExerciseSetMetricEntity>,
): WorkoutSet {
    return WorkoutSet(
        index = setIndex,
        metrics = metrics.associate { entity ->
            WorkoutMetricType.valueOf(entity.metricType) to entity.value
        },
    )
}

fun WorkoutRecordExerciseEntity.toModel(
    exercise: Exercise,
    sets: List<WorkoutSet>,
): Workout {
    return Workout(
        id = id,
        exercise = exercise.copy(
            metricTypes = parseMetricTypes(recordSchema, exerciseName = exercise.name),
        ),
        sets = sets,
    )
}
