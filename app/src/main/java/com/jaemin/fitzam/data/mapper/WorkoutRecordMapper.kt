package com.jaemin.fitzam.data.mapper

import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordEntity
import com.jaemin.fitzam.model.WorkoutRecord
import java.time.LocalDate

fun WorkoutRecordEntity.toModel(partNames: List<String>): WorkoutRecord {
    return WorkoutRecord(
        date = LocalDate.parse(date),
        partNames = partNames,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun WorkoutRecord.toEntity(partIds: String): WorkoutRecordEntity {
    return WorkoutRecordEntity(
        date = date.toString(),
        partIds = partIds,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}