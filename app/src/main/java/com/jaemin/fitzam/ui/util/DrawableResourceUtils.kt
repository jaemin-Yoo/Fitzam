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

    val resolved = try {
        val field = R.drawable::class.java.getDeclaredField(normalizedName)
        field.getInt(null)
    } catch (e: Exception) {
        0
    }

    return if (resolved != 0) resolved else fallbackResId
}
