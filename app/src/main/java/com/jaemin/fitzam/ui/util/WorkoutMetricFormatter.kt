package com.jaemin.fitzam.ui.util

fun formatDurationInMinutesAndSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}분 ${seconds.toString().padStart(2, '0')}초"
}

fun formatDurationInMinutesAndSeconds(secondsText: String): String {
    val totalSeconds = secondsText.toIntOrNull() ?: return secondsText
    return formatDurationInMinutesAndSeconds(totalSeconds)
}
