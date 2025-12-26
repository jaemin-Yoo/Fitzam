package com.jaemin.fitzam.model

import java.time.LocalDate

data class WorkoutRecord(
    val date: LocalDate,
    val partNames: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
)
