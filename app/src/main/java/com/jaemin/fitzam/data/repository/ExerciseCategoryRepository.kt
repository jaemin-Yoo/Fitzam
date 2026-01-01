package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.remote.FirebaseUtil
import com.jaemin.fitzam.model.ExerciseCategory
import javax.inject.Inject

class ExerciseCategoryRepository @Inject constructor(
    private val exerciseCategoryDao: ExerciseCategoryDao,
) {

    suspend fun getExerciseCategories(): List<ExerciseCategory> {
        return exerciseCategoryDao.getExerciseCategoryEntities().map { entity ->
            ExerciseCategory(
                id = entity.id,
                name = entity.name,
                imageUrl = FirebaseUtil.getImageUrl(entity.imagePath),
                colorHex = entity.colorHex,
                colorDarkHex = entity.colorDarkHex,
            )
        }
    }
}