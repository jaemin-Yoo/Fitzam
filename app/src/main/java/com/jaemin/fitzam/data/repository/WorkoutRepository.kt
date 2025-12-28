package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.mapper.toModel
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.FavoriteExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutEntryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutPartDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutSetDao
import com.jaemin.fitzam.data.source.remote.FirebaseUtil
import com.jaemin.fitzam.model.WorkoutEntry
import com.jaemin.fitzam.model.WorkoutPart
import com.jaemin.fitzam.model.WorkoutRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val recordDao: WorkoutRecordDao,
    private val entryDao: WorkoutEntryDao,
    private val setDao: WorkoutSetDao,
    private val partDao: WorkoutPartDao,
    private val exerciseDao: ExerciseDao,
    private val favoriteExerciseDao: FavoriteExerciseDao,
) {

    fun getWorkoutRecordsForMonth(yearMonth: YearMonth): Flow<List<WorkoutRecord>> {
        val startDate = yearMonth.atDay(1).toString()
        val endDate = yearMonth.atEndOfMonth().toString()

        return recordDao.getWorkoutRecordEntities(startDate, endDate).map { records ->
            records.map { record ->
                val partNames = resolvePartNames(record.partIds)
                record.toModel(partNames)
            }
        }
    }

    fun getWorkoutEntries(date: LocalDate): Flow<List<WorkoutEntry>> {
        return entryDao.getWorkoutEntryEntities(date.toString()).map { entities ->
            entities.map { entry ->
                val exercise = exerciseDao.getExerciseEntity(entry.exerciseId)
                val part = partDao.getWorkoutPartEntityById(entry.partId)
                val sets = setDao.getSetEntities(entry.id).map { entities ->
                    entities.map { it.toModel() }
                }.first() // TODO: 확인 필요
                entry.toModel(
                    exerciseName = exercise.name,
                    partName = part.displayName,
                    sets = sets,
                )
            }
        }
    }

    suspend fun getWorkoutParts(): List<WorkoutPart> {
        return partDao.getWorkoutPartEntities().map { entity ->
            val imageUrl = runCatching { FirebaseUtil.getImageUrl(entity.imagePath) }
                .getOrElse { entity.imagePath }
            entity.toModel(imageUrl)
        }
    }

    private fun resolvePartNames(partIds: String): List<String> {
        return partIds.split(",").map { strId ->
            partDao.getWorkoutPartEntityById(strId.toLong()).displayName
        }
    }
}
