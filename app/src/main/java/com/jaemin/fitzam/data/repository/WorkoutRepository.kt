package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.mapper.toModel
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.FavoriteExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutEntryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutPartDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutSetDao
import com.jaemin.fitzam.model.WorkoutRecord
import kotlinx.coroutines.flow.Flow
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

        return recordDao.getWorkoutRecordEntities(startDate, endDate).map { workoutRecords ->
            workoutRecords.map { record ->
                val partNames = record.partIds.split(",").map {
                    partDao.getWorkoutPartEntityById(it.toLong()).displayName
                }
                record.toModel(partNames)
            }
        }
    }
}