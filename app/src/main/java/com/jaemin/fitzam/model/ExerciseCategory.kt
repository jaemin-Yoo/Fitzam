package com.jaemin.fitzam.model

import androidx.annotation.DrawableRes

data class ExerciseCategory(
    val id: Long,
    val name: String,
    @DrawableRes val imageDrawableRes: Int,
    val colorHex: Long,
    val colorDarkHex: Long,
)
