package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.mapper.toModel
import com.jaemin.fitzam.data.source.local.FitzamDatabase
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseSetDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseSetMetricDao
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseCategoryEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseSetEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseSetMetricEntity
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutRecord
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.model.parseMetricTypes
import com.jaemin.fitzam.model.serializeMetricTypes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class WorkoutSetDraft(
    val setIndex: Int,
    val metrics: Map<WorkoutMetricType, Double>,
)

data class WorkoutDraft(
    val exerciseId: Long,
    val categoryId: Long,
    val orderIndex: Int,
    val metricTypes: List<WorkoutMetricType>,
    val sets: List<WorkoutSetDraft>,
)

class WorkoutRepository @Inject constructor(
    private val database: FitzamDatabase,
    private val workoutRecordDao: WorkoutRecordDao,
    private val workoutRecordExerciseCategoryDao: WorkoutRecordExerciseCategoryDao,
    private val workoutRecordExerciseDao: WorkoutRecordExerciseDao,
    private val exerciseCategoryDao: ExerciseCategoryDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: WorkoutRecordExerciseSetDao,
    private val setMetricDao: WorkoutRecordExerciseSetMetricDao,
) {

    fun getWorkoutRecordsForYearMonth(yearMonth: YearMonth): Flow<List<WorkoutRecord>> {
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
    fun getWorkouts(date: LocalDate): Flow<List<Workout>> {
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
        database.withTransaction {
            if (categoryIds.isEmpty()) {
                deleteWorkoutRecord(date)
                return@withTransaction
            }

            val dateString = date.toString()
            val selectedCategoryIdSet = categoryIds.toSet()

            workoutRecordDao.insert(
                WorkoutRecordEntity(date = dateString),
            )

            val exerciseRecords = workoutRecordExerciseDao.getWorkoutRecordExerciseEntitiesOnce(dateString)
            if (exerciseRecords.isNotEmpty()) {
                val exerciseIds = exerciseRecords.map { entry -> entry.exerciseId }.distinct()
                val exerciseCategoryByExerciseId = exerciseDao.getExerciseEntitiesByIds(exerciseIds)
                    .associate { entity -> entity.id to entity.categoryId }

                val deleteTargetIds = exerciseRecords.mapNotNull { entry ->
                    val categoryId = exerciseCategoryByExerciseId[entry.exerciseId]
                    if (categoryId == null || categoryId !in selectedCategoryIdSet) {
                        entry.id
                    } else {
                        null
                    }
                }
                if (deleteTargetIds.isNotEmpty()) {
                    workoutRecordExerciseDao.deleteByIds(deleteTargetIds)
                }
            }

            workoutRecordExerciseCategoryDao.deleteByDate(dateString)
            categoryIds.forEach { categoryId ->
                workoutRecordExerciseCategoryDao.insert(
                    WorkoutRecordExerciseCategoryEntity(
                        workoutRecordDate = dateString,
                        exerciseCategoryId = categoryId,
                    ),
                )
            }
        }
    }

    suspend fun replaceWorkouts(
        date: LocalDate,
        workouts: List<WorkoutDraft>,
        categoryIds: List<Long> = emptyList(),
    ) {
        database.withTransaction {
            if (workouts.isEmpty() && categoryIds.isEmpty()) {
                deleteWorkoutRecord(date)
                return@withTransaction
            }

            workoutRecordDao.insert(
                WorkoutRecordEntity(date = date.toString()),
            )

            val categoriesToSave = categoryIds.ifEmpty {
                workouts.map { workout -> workout.categoryId }.distinct()
            }
            workoutRecordExerciseCategoryDao.deleteByDate(date.toString())
            categoriesToSave.forEach { categoryId ->
                workoutRecordExerciseCategoryDao.insert(
                    WorkoutRecordExerciseCategoryEntity(
                        workoutRecordDate = date.toString(),
                        exerciseCategoryId = categoryId,
                    ),
                )
            }

            workoutRecordExerciseDao.deleteByDate(date.toString())
            workouts.sortedBy { workout -> workout.orderIndex }.forEach { workout ->
                val workoutId = workoutRecordExerciseDao.insert(
                    WorkoutRecordExerciseEntity(
                        workoutRecordDate = date.toString(),
                        exerciseId = workout.exerciseId,
                        orderIndex = workout.orderIndex,
                        recordSchema = serializeMetricTypes(workout.metricTypes),
                    ),
                )

                if (workout.sets.isNotEmpty()) {
                    setDao.insertOrUpdateAll(
                        workout.sets.map { set ->
                            WorkoutRecordExerciseSetEntity(
                                workoutRecordExerciseId = workoutId,
                                setIndex = set.setIndex,
                            )
                        },
                    )
                    setMetricDao.insertOrUpdateAll(
                        workout.sets.flatMap { set ->
                            set.metrics.map { (metricType, value) ->
                                WorkoutRecordExerciseSetMetricEntity(
                                    workoutRecordExerciseId = workoutId,
                                    setIndex = set.setIndex,
                                    metricType = metricType.name,
                                    value = value,
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    private suspend fun deleteWorkoutRecord(date: LocalDate) {
        workoutRecordExerciseCategoryDao.deleteByDate(date.toString())
        workoutRecordDao.deleteByDate(date.toString())
    }

    suspend fun getLatestMetricTypes(exerciseId: Long): List<WorkoutMetricType>? {
        val rawSchema = workoutRecordExerciseDao.getLatestRecordSchemaByExerciseId(exerciseId) ?: return null
        return parseMetricTypes(rawSchema)
    }

}
