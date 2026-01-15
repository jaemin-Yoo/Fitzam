package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.WorkoutCategoryEntity

@Dao
interface WorkoutCategoryDao {

    @Query("SELECT exerciseCategoryId FROM workout_category WHERE workoutDate = :date")
    suspend fun getExerciseCategoryIds(date: String): List<Long>

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insert(workoutCategory: WorkoutCategoryEntity)

    @Query("DELETE FROM workout_category WHERE workoutDate = :date")
    suspend fun deleteByDate(date: String)
}