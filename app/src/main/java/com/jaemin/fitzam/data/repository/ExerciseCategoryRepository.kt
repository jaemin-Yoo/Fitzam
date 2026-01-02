package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.repository.ImageUrlRepository
import com.jaemin.fitzam.model.ExerciseCategory
import javax.inject.Inject

class ExerciseCategoryRepository @Inject constructor(
    private val exerciseCategoryDao: ExerciseCategoryDao,
    private val imageUrlRepository: ImageUrlRepository,
) {

    suspend fun getExerciseCategories(): List<ExerciseCategory> {
        return exerciseCategoryDao.getExerciseCategoryEntities().map { entity ->
            ExerciseCategory(
                id = entity.id,
                name = entity.name,
                imageUrl = imageUrlRepository.getImageUrl(entity.imagePath),
                colorHex = entity.colorHex,
                colorDarkHex = entity.colorDarkHex,
            )
        }
    }
}