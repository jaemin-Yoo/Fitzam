package com.jaemin.fitzam.data.mapper

import com.jaemin.fitzam.data.source.local.entity.ExerciseCategoryEntity
import com.jaemin.fitzam.data.source.local.entity.ExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseSetEntity
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutExercise
import com.jaemin.fitzam.model.WorkoutSet
import java.time.LocalDate

fun WorkoutRecordEntity.toModel(exerciseCategories: List<ExerciseCategory>): Workout {
    return Workout(
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
    )
}

fun WorkoutRecordExerciseSetEntity.toModel(): WorkoutSet {
    return WorkoutSet(
        index = setIndex,
        weightKg = weightKg,
        reps = reps,
    )
}

fun WorkoutRecordExerciseEntity.toModel(
    exercise: Exercise,
    sets: List<WorkoutSet>,
): WorkoutExercise {
    return WorkoutExercise(
        id = id,
        exercise = exercise,
        sets = sets,
    )
}
