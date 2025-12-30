package com.jaemin.fitzam.data.mapper

import com.jaemin.fitzam.data.source.local.entity.WorkoutEntryEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutPartEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutSetEntity
import com.jaemin.fitzam.model.WorkoutEntry
import com.jaemin.fitzam.model.WorkoutPart
import com.jaemin.fitzam.model.WorkoutRecord
import com.jaemin.fitzam.model.WorkoutSet
import java.time.LocalDate

fun WorkoutRecordEntity.toModel(partCodes: List<String>): WorkoutRecord {
    return WorkoutRecord(
        date = LocalDate.parse(date),
        partNames = partCodes,
    )
}

fun WorkoutRecord.toEntity(partIds: String): WorkoutRecordEntity {
    return WorkoutRecordEntity(
        date = date.toString(),
        partIds = partIds,
    )
}

fun WorkoutEntryEntity.toModel(
    exerciseName: String,
    partName: String,
    sets: List<WorkoutSet>,
): WorkoutEntry {
    return WorkoutEntry(
        id = id,
        exerciseName = exerciseName,
        partName = partName,
        sets = sets,
    )
}

fun WorkoutSetEntity.toModel(): WorkoutSet {
    return WorkoutSet(
        index = setIndex,
        weightKg = weightKg,
        reps = reps,
    )
}

fun WorkoutPartEntity.toModel(imageUrl: String): WorkoutPart {
    return WorkoutPart(
        id = id,
        name = name,
        imageUrl = imageUrl,
        colorHex = colorHex,
        colorDarkHex = colorDarkHex,
    )
}