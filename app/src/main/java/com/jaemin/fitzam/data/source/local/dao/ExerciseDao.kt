package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.ExerciseEntity

@Dao
interface ExerciseDao {

    @Query("""
        SELECT * FROM exercise
        WHERE partCode = :partCode
        ORDER BY id
    """)
    fun getExerciseEntities(partCode: String): List<ExerciseEntity>

    @Query(
        """
            SELECT * FROM exercise
            WHERE id = :id
        """
    )
    fun getExerciseEntity(id: Long): ExerciseEntity
}