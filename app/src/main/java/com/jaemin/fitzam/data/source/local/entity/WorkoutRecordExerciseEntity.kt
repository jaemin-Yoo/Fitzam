package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_record_exercise",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRecordEntity::class,
            parentColumns = ["date"],
            childColumns = ["workoutRecordDate"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT,
        )
    ],
    indices = [
        Index("workoutRecordDate"),
        Index("exerciseId"),
    ]
)
data class WorkoutRecordExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutRecordDate: String,
    val exerciseId: Long,
    val orderIndex: Int,
    val recordSchema: String,
)
