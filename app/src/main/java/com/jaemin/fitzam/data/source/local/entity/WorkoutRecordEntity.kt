package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_record")
data class WorkoutRecordEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val partIds: String,
    val createdAt: Long,
    val updatedAt: Long,
)