package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_entry",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRecordEntity::class,
            parentColumns = ["date"],
            childColumns = ["recordDate"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkoutPartEntity::class,
            parentColumns = ["id"],
            childColumns = ["partId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT,
        )
    ],
    indices = [
        Index("recordDate"),
        Index("partId"),
        Index("exerciseId"),
    ]
)
data class WorkoutEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordDate: String,
    val partId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val createdAt: Long,
)