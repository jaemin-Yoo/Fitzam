package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.mapper.toModel
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseSetDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseSetMetricDao
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseCategoryEntity
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutExercise
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val workoutRecordDao: WorkoutRecordDao,
    private val workoutRecordExerciseCategoryDao: WorkoutRecordExerciseCategoryDao,
    private val workoutRecordExerciseDao: WorkoutRecordExerciseDao,
    private val exerciseCategoryDao: ExerciseCategoryDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: WorkoutRecordExerciseSetDao,
    private val setMetricDao: WorkoutRecordExerciseSetMetricDao,
) {

    fun getWorkoutsForYearMonth(yearMonth: YearMonth): Flow<List<Workout>> {
        val startDate = yearMonth.atDay(1).toString()
        val endDate = yearMonth.atEndOfMonth().toString()

        return workoutRecordDao.getWorkoutRecordEntities(startDate, endDate).map { entities ->
            entities.map { workoutRecord ->
                val exerciseCategoryIds =
                    workoutRecordExerciseCategoryDao.getExerciseCategoryIds(workoutRecord.date)
                val exerciseCategories = exerciseCategoryDao.getExerciseCategoryEntitiesByIds(exerciseCategoryIds)
                workoutRecord.toModel(
                    exerciseCategories = exerciseCategories.map { category ->
                        category.toModel()
                    },
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWorkoutExercises(date: LocalDate): Flow<List<WorkoutExercise>> {
        return workoutRecordExerciseDao.getWorkoutRecordExerciseEntities(date.toString())
            .flatMapLatest { entities ->
                if (entities.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows = entities.map { workoutRecordExercise ->
                        combine(
                            setDao.getSetEntities(workoutRecordExercise.id),
                            setMetricDao.getMetricEntities(workoutRecordExercise.id),
                        ) { setEntities, metricEntities ->
                            val exerciseEntity = exerciseDao.getExerciseEntity(
                                workoutRecordExercise.exerciseId,
                            )
                            val categoryEntity = exerciseCategoryDao.getExerciseCategoryEntityById(
                                exerciseEntity.categoryId,
                            )
                            val exercise = exerciseEntity.toModel(
                                category = categoryEntity.toModel(),
                            )
                            val metricsBySetIndex = metricEntities.groupBy { entity -> entity.setIndex }
                            workoutRecordExercise.toModel(
                                exercise = exercise,
                                sets = setEntities.map { entity ->
                                    entity.toModel(
                                        metrics = metricsBySetIndex[entity.setIndex].orEmpty(),
                                    )
                                },
                            )
                        }
                    }
                    combine(flows) { values -> values.toList() }
                }
            }
    }

    suspend fun applyWorkoutChanges(
        categoryIds: List<Long>,
        date: LocalDate,
    ) {
        if (categoryIds.isEmpty()) {
            deleteWorkout(date)
        } else {
            upsertWorkout(
                date = date,
                categoryIds = categoryIds,
            )
        }
    }

    private suspend fun deleteWorkout(date: LocalDate) {
        workoutRecordExerciseCategoryDao.deleteByDate(date.toString())
        workoutRecordDao.deleteByDate(date.toString())
    }

    private suspend fun upsertWorkout(date: LocalDate, categoryIds: List<Long>) {
        val workoutRecord = WorkoutRecordEntity(
            date = date.toString(),
        )
        workoutRecordDao.insert(workoutRecord)

        // 매핑 데이터 삭제 후 추가
        workoutRecordExerciseCategoryDao.deleteByDate(date.toString())
        categoryIds.forEach { id ->
            val workoutRecordExerciseCategory = WorkoutRecordExerciseCategoryEntity(
                workoutRecordDate = date.toString(),
                exerciseCategoryId = id,
            )
            workoutRecordExerciseCategoryDao.insert(workoutRecordExerciseCategory)
        }
    }
}
