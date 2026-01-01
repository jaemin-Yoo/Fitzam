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
import com.jaemin.fitzam.data.source.remote.FirebaseUtil
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    fun getWorkoutsForMonth(yearMonth: YearMonth): Flow<List<Workout>> {
        val startDate = yearMonth.atDay(1).toString()
        val endDate = yearMonth.atEndOfMonth().toString()

        return workoutDao.getWorkoutEntities(startDate, endDate).map { entities ->
            entities.map { workout ->
                val exerciseCategoryIds = workoutCategoryDao.getExerciseCategoryIds(workout.date)
                val exerciseCategories = exerciseCategoryIds.map { id ->
                    exerciseCategoryDao.getExerciseCategoryEntityById(id)
                }
                workout.toModel(
                    exerciseCategories = exerciseCategories.map { category ->
                        category.toModel(FirebaseUtil.getImageUrl(category.imagePath))
                    },
                )
            }
        }
    }

    fun getWorkoutExercises(date: LocalDate): Flow<List<WorkoutExercise>> {
        return workoutExerciseDao.getWorkoutExerciseEntities(date.toString()).map { entities ->
            entities.map { workoutExercise ->
                val exerciseEntity = exerciseDao.getExerciseEntity(workoutExercise.exerciseId)
                val categoryEntity = exerciseCategoryDao.getExerciseCategoryEntityById(
                    exerciseEntity.categoryId,
                )
                val sets = setDao.getSetEntities(workoutExercise.id).map { entities ->
                    entities.map { entity -> entity.toModel() }
                }.first() // TODO: 확인 필요

                val category = categoryEntity.toModel(
                    FirebaseUtil.getImageUrl(categoryEntity.imagePath),
                )
                val exercise = exerciseEntity.toModel(
                    category = category,
                    imageUrl = FirebaseUtil.getImageUrl(exerciseEntity.imagePath),
                )

                workoutExercise.toModel(
                    exercise = exercise,
                    sets = sets,
                )
            }
        }
    }

    suspend fun insertWorkout(date: LocalDate, categoryIds: List<Long>) {
        val workout = WorkoutEntity(
            date = date.toString(),
        )
        workoutDao.insert(workout)

        // 매핑 데이터 추가
        categoryIds.forEach { id ->
            val workoutCategory = WorkoutCategoryEntity(
                workoutDate = date.toString(),
                exerciseCategoryId = id,
            )
            workoutCategoryDao.insert(workoutCategory)
        }
    }
}
