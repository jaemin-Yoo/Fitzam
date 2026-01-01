package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "workout_category",
    primaryKeys = ["workoutDate", "exerciseCategoryId"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["date"],
            childColumns = ["workoutDate"],
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
data class WorkoutCategoryEntity(
    val workoutDate: String, // YYYY-MM-DD
    val exerciseCategoryId: Long,
)
