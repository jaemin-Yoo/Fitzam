package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.mapper.toModel
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.FavoriteExerciseDao
import com.jaemin.fitzam.data.source.local.entity.FavoriteExerciseEntity
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.model.serializeMetricTypes
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExerciseRepository @Inject constructor(
    private val exerciseCategoryDao: ExerciseCategoryDao,
    private val exerciseDao: ExerciseDao,
    private val favoriteExerciseDao: FavoriteExerciseDao,
) {

    suspend fun getExercisesByCategoryIds(categoryIds: Set<Long>): List<Exercise> {
        if (categoryIds.isEmpty()) {
            return emptyList()
        }

        val exerciseEntities = exerciseDao.getExerciseEntitiesByCategoryIds(categoryIds.toList())
        val categoryEntities = exerciseCategoryDao.getExerciseCategoryEntitiesByIds(
            ids = exerciseEntities.map { entity -> entity.categoryId }.distinct(),
        )
        val categoryMap = categoryEntities.associateBy { entity -> entity.id }

        return exerciseEntities.mapNotNull { entity ->
            val category = categoryMap[entity.categoryId] ?: return@mapNotNull null
            entity.toModel(category = category.toModel())
        }
    }

    suspend fun getExercisesByIds(ids: Set<Long>): List<Exercise> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        val exerciseEntities = exerciseDao.getExerciseEntitiesByIds(ids.toList())
        val categoryEntities = exerciseCategoryDao.getExerciseCategoryEntitiesByIds(
            ids = exerciseEntities.map { entity -> entity.categoryId }.distinct(),
        )
        val categoryMap = categoryEntities.associateBy { entity -> entity.id }

        return exerciseEntities.mapNotNull { entity ->
            val category = categoryMap[entity.categoryId] ?: return@mapNotNull null
            entity.toModel(category = category.toModel())
        }
    }

    suspend fun getFavoriteExerciseIds(): Set<Long> {
        return favoriteExerciseDao.getFavoriteExerciseEntities()
            .first()
            .map { entity -> entity.exerciseId }
            .toSet()
    }

    suspend fun addFavoriteExercise(exerciseId: Long) {
        favoriteExerciseDao.insert(FavoriteExerciseEntity(exerciseId = exerciseId))
    }

    suspend fun removeFavoriteExercise(exerciseId: Long) {
        favoriteExerciseDao.deleteByExerciseId(exerciseId)
    }

    suspend fun updateExerciseMetricTypes(
        exerciseId: Long,
        metricTypes: List<WorkoutMetricType>,
    ) {
        exerciseDao.updateRecordSchema(
            exerciseId = exerciseId,
            recordSchema = serializeMetricTypes(metricTypes),
        )
    }
}
