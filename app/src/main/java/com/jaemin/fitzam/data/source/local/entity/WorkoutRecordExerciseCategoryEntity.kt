package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "workout_record_exercise_category",
    primaryKeys = ["workoutRecordDate", "exerciseCategoryId"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRecordEntity::class,
            parentColumns = ["date"],
            childColumns = ["workoutRecordDate"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseCategoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ]
)
data class WorkoutRecordExerciseCategoryEntity(
    val workoutRecordDate: String, // YYYY-MM-DD
    val exerciseCategoryId: Long,
)
