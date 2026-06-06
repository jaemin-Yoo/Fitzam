package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.mapper.toModel
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.entity.ExerciseEntity
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseEquipmentType
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.model.serializeMetricTypes
import javax.inject.Inject

class ExerciseRepository @Inject constructor(
    private val exerciseCategoryDao: ExerciseCategoryDao,
    private val exerciseDao: ExerciseDao,
) {

    suspend fun addExercise(
        name: String,
        categoryId: Long,
        imageName: String,
        equipmentType: ExerciseEquipmentType,
        metricTypes: List<WorkoutMetricType>,
    ): Long {
        return exerciseDao.insert(
            ExerciseEntity(
                name = name,
                categoryId = categoryId,
                imageName = imageName,
                equipmentType = equipmentType.name,
                recordSchema = serializeMetricTypes(metricTypes),
                isCustom = true,
            )
        )
    }

    suspend fun getCustomExercisesByCategoryIds(categoryIds: Set<Long>): List<Exercise> {
        if (categoryIds.isEmpty()) {
            return emptyList()
        }

        val exerciseEntities = exerciseDao.getCustomExerciseEntitiesByCategoryIds(categoryIds.toList())
        val categoryEntities = exerciseCategoryDao.getExerciseCategoryEntitiesByIds(
            ids = exerciseEntities.map { entity -> entity.categoryId }.distinct(),
        )
        val categoryMap = categoryEntities.associateBy { entity -> entity.id }

        return exerciseEntities.mapNotNull { entity ->
            val category = categoryMap[entity.categoryId] ?: return@mapNotNull null
            entity.toModel(category = category.toModel())
        }
    }

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

    suspend fun searchPresetExercises(query: String): List<Exercise> {
        if (query.isBlank()) return emptyList()

        val exerciseEntities = exerciseDao.searchPresetExercisesByName(query)
        val categoryEntities = exerciseCategoryDao.getExerciseCategoryEntitiesByIds(
            ids = exerciseEntities.map { entity -> entity.categoryId }.distinct(),
        )
        val categoryMap = categoryEntities.associateBy { entity -> entity.id }

        return exerciseEntities.mapNotNull { entity ->
            val category = categoryMap[entity.categoryId] ?: return@mapNotNull null
            entity.toModel(category = category.toModel())
        }
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

    suspend fun updateExerciseEquipmentType(
        exerciseId: Long,
        equipmentType: ExerciseEquipmentType,
    ) {
        exerciseDao.updateEquipmentType(
            exerciseId = exerciseId,
            equipmentType = equipmentType.name,
        )
    }
}
