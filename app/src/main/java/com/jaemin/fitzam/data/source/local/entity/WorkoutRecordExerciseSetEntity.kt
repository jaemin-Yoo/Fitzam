package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "workout_record_exercise_set",
    primaryKeys = ["workoutRecordExerciseId", "setIndex"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRecordExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutRecordExerciseId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("workoutRecordExerciseId")]
)
data class WorkoutRecordExerciseSetEntity(
    val workoutRecordExerciseId: Long,
    val setIndex: Int,
)
