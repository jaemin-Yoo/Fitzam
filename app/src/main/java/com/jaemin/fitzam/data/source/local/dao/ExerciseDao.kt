package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.ExerciseEntity

@Dao
interface ExerciseDao {

    @Query(
        """
        SELECT * FROM exercise
        WHERE categoryId IN (:categoryIds)
        ORDER BY id
    """
    )
    suspend fun getExerciseEntitiesByCategoryIds(categoryIds: List<Long>): List<ExerciseEntity>

    @Query(
        """
            SELECT * FROM exercise
            WHERE id = :id
        """
    )
    fun getExerciseEntity(id: Long): ExerciseEntity
}
