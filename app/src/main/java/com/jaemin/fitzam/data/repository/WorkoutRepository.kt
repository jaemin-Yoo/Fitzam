package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.mapper.toModel
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutSetDao
import com.jaemin.fitzam.data.source.local.entity.WorkoutCategoryEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutEntity
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
    private val workoutDao: WorkoutDao,
    private val workoutCategoryDao: WorkoutCategoryDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val exerciseCategoryDao: ExerciseCategoryDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: WorkoutSetDao,
) {

    fun getWorkoutsForYearMonth(yearMonth: YearMonth): Flow<List<Workout>> {
        val startDate = yearMonth.atDay(1).toString()
        val endDate = yearMonth.atEndOfMonth().toString()

        return workoutDao.getWorkoutEntities(startDate, endDate).map { entities ->
            entities.map { workout ->
                val exerciseCategoryIds = workoutCategoryDao.getExerciseCategoryIds(workout.date)
                val exerciseCategories = exerciseCategoryDao.getExerciseCategoryEntitiesByIds(exerciseCategoryIds)
                workout.toModel(
                    exerciseCategories = exerciseCategories.map { category ->
                        category.toModel()
                    },
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWorkoutExercises(date: LocalDate): Flow<List<WorkoutExercise>> {
        return workoutExerciseDao.getWorkoutExerciseEntities(date.toString())
            .flatMapLatest { entities ->
                if (entities.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows = entities.map { workoutExercise ->
                        setDao.getSetEntities(workoutExercise.id).map { setEntities ->
                            val exerciseEntity = exerciseDao.getExerciseEntity(
                                workoutExercise.exerciseId,
                            )
                            val categoryEntity = exerciseCategoryDao.getExerciseCategoryEntityById(
                                exerciseEntity.categoryId,
                            )
                            val exercise = exerciseEntity.toModel(
                                category = categoryEntity.toModel(),
                            )
                            workoutExercise.toModel(
                                exercise = exercise,
                                sets = setEntities.map { entity -> entity.toModel() },
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
        workoutCategoryDao.deleteByDate(date.toString())
        workoutDao.deleteByDate(date.toString())
    }

    private suspend fun upsertWorkout(date: LocalDate, categoryIds: List<Long>) {
        val workout = WorkoutEntity(
            date = date.toString(),
        )
        workoutDao.insert(workout)

        // 매핑 데이터 삭제 후 추가
        workoutCategoryDao.deleteByDate(date.toString())
        categoryIds.forEach { id ->
            val workoutCategory = WorkoutCategoryEntity(
                workoutDate = date.toString(),
                exerciseCategoryId = id,
            )
            workoutCategoryDao.insert(workoutCategory)
        }
    }
}
