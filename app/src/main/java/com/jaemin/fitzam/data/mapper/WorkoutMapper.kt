package com.jaemin.fitzam.data.mapper

import com.jaemin.fitzam.data.source.local.entity.ExerciseCategoryEntity
import com.jaemin.fitzam.data.source.local.entity.ExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutSetEntity
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutExercise
import com.jaemin.fitzam.model.WorkoutSet
import java.time.LocalDate

fun WorkoutEntity.toModel(exerciseCategories: List<ExerciseCategory>): Workout {
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

fun ExerciseEntity.toModel(category: ExerciseCategory, imageName: String): Exercise {
    return Exercise(
        id = id,
        name = name,
        category = category,
        imageName = imageName,
    )
}

fun WorkoutSetEntity.toModel(): WorkoutSet {
    return WorkoutSet(
        index = setIndex,
        weightKg = weightKg,
        reps = reps,
    )
}

fun WorkoutExerciseEntity.toModel(
    exercise: Exercise,
    sets: List<WorkoutSet>,
): WorkoutExercise {
    return WorkoutExercise(
        id = id,
        exercise = exercise,
        sets = sets,
    )
}
