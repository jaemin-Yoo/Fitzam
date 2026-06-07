package com.jaemin.fitzam.ui.util

import androidx.annotation.DrawableRes
import com.jaemin.fitzam.R

@DrawableRes
fun drawableResIdByName(
    imageName: String,
    @DrawableRes fallbackResId: Int = R.drawable.ic_launcher_foreground,
): Int {
    val normalizedName = imageName.substringAfterLast("/")
        .substringBeforeLast(".")

    return when (normalizedName) {
        "img_chest" -> R.drawable.ic_chest
        "img_back" -> R.drawable.ic_back_muscle
        "img_shoulder" -> R.drawable.ic_shoulder
        "img_triceps" -> R.drawable.ic_triceps
        "img_biceps" -> R.drawable.ic_biceps
        "img_lower_body" -> R.drawable.ic_lower_body
        "img_abs" -> R.drawable.ic_abs
        "img_aerobic" -> R.drawable.ic_aerobic
        else -> fallbackResId
    }
}
