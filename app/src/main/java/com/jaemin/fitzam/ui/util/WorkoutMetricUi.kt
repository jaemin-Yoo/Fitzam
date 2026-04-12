package com.jaemin.fitzam.ui.util

import com.jaemin.fitzam.model.WorkoutMetricType
import java.util.Locale

fun formatMetricValue(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}

fun metricLabel(metricType: WorkoutMetricType): String {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG -> "무게"
        WorkoutMetricType.REPS -> "횟수"
        WorkoutMetricType.DISTANCE_KM -> "거리"
        WorkoutMetricType.DURATION_SEC -> "시간"
    }
}

fun metricHeader(metricType: WorkoutMetricType): String {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG -> "무게(KG)"
        WorkoutMetricType.REPS -> "횟수"
        WorkoutMetricType.DISTANCE_KM -> "거리(KM)"
        WorkoutMetricType.DURATION_SEC -> "시간"
    }
}

fun metricQuickAdjustValues(metricType: WorkoutMetricType): List<Double> {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG -> listOf(2.5, 5.0, 10.0, 15.0, 20.0)
        WorkoutMetricType.REPS -> listOf(5.0, 10.0, 50.0)
        WorkoutMetricType.DISTANCE_KM -> listOf(0.5, 1.0, 2.0, 3.0, 5.0)
        WorkoutMetricType.DURATION_SEC -> listOf(10.0, 60.0, 300.0)
    }
}

fun metricStep(metricType: WorkoutMetricType): Double {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG -> 2.5
        WorkoutMetricType.REPS -> 1.0
        WorkoutMetricType.DISTANCE_KM -> 0.1
        WorkoutMetricType.DURATION_SEC -> 10.0
    }
}

fun formatMetricDisplayValue(
    metricType: WorkoutMetricType,
    value: Double,
): String {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG -> "${formatMetricValue(value)} KG"
        WorkoutMetricType.REPS -> "${formatMetricValue(value)} 개"
        WorkoutMetricType.DISTANCE_KM -> formatDistanceDisplayValue(value)
        WorkoutMetricType.DURATION_SEC -> formatDurationDisplayValue(value.toInt())
    }
}

fun formatQuickAdjustLabel(
    metricType: WorkoutMetricType,
    value: Double,
): String {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG -> formatMetricValue(value)
        WorkoutMetricType.REPS -> formatMetricValue(value)
        WorkoutMetricType.DISTANCE_KM -> formatMetricValue(value)
        WorkoutMetricType.DURATION_SEC -> formatDurationShortLabel(value.toInt())
    }
}

fun formatDistanceDisplayValue(distanceKm: Double): String {
    val totalMeters = (distanceKm * 1000).toInt()
    val km = totalMeters / 1000
    val meters = totalMeters % 1000
    return buildString {
        if (km > 0 || meters == 0) {
            append(km)
            append(" KM")
        }
        if (meters > 0) {
            if (isNotEmpty()) append(' ')
            append(meters)
            append(" M")
        }
    }
}

fun formatDurationDisplayValue(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "0초"
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return buildString {
        if (hours > 0) append("${hours}시간 ")
        if (minutes > 0) append("${minutes}분 ")
        if (seconds > 0 || (hours == 0 && minutes == 0)) append("${seconds}초")
    }.trim()
}

fun formatDurationShortLabel(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0 && seconds > 0) {
        "${minutes}분 ${seconds}초"
    } else if (minutes > 0) {
        "${minutes}분"
    } else {
        "${seconds}초"
    }
}
