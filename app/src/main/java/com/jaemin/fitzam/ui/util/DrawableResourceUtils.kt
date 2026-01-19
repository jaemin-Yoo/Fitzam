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
        "img_chest" -> R.drawable.img_chest
        "img_back" -> R.drawable.img_back
        "img_shoulder" -> R.drawable.img_shoulder
        "img_triceps" -> R.drawable.img_triceps
        "img_biceps" -> R.drawable.img_biceps
        "img_lower_body" -> R.drawable.img_lower_body
        "img_abs" -> R.drawable.img_abs
        "img_aerobic" -> R.drawable.img_aerobic
        else -> fallbackResId
    }
}
