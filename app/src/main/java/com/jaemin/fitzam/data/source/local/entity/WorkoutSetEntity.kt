package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_set",
    primaryKeys = ["entryId", "setIndex"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("entryId")]
)
data class WorkoutSetEntity(
    val entryId: Long,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
    val createdAt: Long,
)