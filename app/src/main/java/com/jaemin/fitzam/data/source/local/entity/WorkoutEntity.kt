package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout")
data class WorkoutEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
)
