package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_category")
data class ExerciseCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val imagePath: String,
    val colorHex: String,
    val colorDarkHex: String,
)
