package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "workout_set",
    primaryKeys = ["workoutExerciseId", "setIndex"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("workoutExerciseId")]
)
data class WorkoutSetEntity(
    val workoutExerciseId: Long,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
)
