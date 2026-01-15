package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.mapper.toModel
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutCategoryDao
import com.jaemin.fitzam.model.ExerciseCategory
import java.time.LocalDate
import javax.inject.Inject

class ExerciseCategoryRepository @Inject constructor(
    private val workoutCategoryDao: WorkoutCategoryDao,
    private val exerciseCategoryDao: ExerciseCategoryDao,
) {

    suspend fun getExerciseCategories(): List<ExerciseCategory> {
        return exerciseCategoryDao.getExerciseCategoryEntities().map { entity ->
            entity.toModel()
        }
    }

    suspend fun getExerciseCategoryIds(date: LocalDate): List<Long> {
        return workoutCategoryDao.getExerciseCategoryIds(date.toString())
    }
}