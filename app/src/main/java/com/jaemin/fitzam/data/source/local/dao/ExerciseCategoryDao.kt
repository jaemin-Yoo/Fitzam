package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.ExerciseCategoryEntity

@Dao
interface ExerciseCategoryDao {

    @Query("SELECT * FROM exercise_category ORDER BY id")
    fun getExerciseCategoryEntities(): List<ExerciseCategoryEntity>

    @Query("SELECT * FROM exercise_category WHERE id = :id")
    fun getExerciseCategoryEntityById(id: Long): ExerciseCategoryEntity

    @Query("SELECT * FROM exercise_category WHERE id IN (:ids)")
    fun getExerciseCategoryEntitiesByIds(ids: List<Long>): List<ExerciseCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(parts: List<ExerciseCategoryEntity>)
}
