package com.jaemin.fitzam.data.source.local.entity

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_category")
data class ExerciseCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @DrawableRes val imageDrawableRes: Int,
    val colorHex: Long,
    val colorDarkHex: Long,
)
