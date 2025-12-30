package com.jaemin.fitzam.model

import java.time.LocalDate

data class WorkoutRecord(
    val date: LocalDate,
    val partCodes: List<String>,
)
