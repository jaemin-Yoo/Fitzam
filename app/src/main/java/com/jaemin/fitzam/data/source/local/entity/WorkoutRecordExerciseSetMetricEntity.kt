package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "workout_record_exercise_set_metric",
    primaryKeys = ["workoutRecordExerciseId", "setIndex", "metricType"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRecordExerciseSetEntity::class,
            parentColumns = ["workoutRecordExerciseId", "setIndex"],
            childColumns = ["workoutRecordExerciseId", "setIndex"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("workoutRecordExerciseId"),
        Index("workoutRecordExerciseId", "setIndex"),
    ]
)
data class WorkoutRecordExerciseSetMetricEntity(
    val workoutRecordExerciseId: Long,
    val setIndex: Int,
    val metricType: String,
    val value: Double,
)
