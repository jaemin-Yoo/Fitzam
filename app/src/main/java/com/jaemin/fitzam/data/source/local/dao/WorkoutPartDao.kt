package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.WorkoutPartEntity

@Dao
interface WorkoutPartDao {

    @Query("SELECT * FROM workout_part ORDER BY id")
    fun getWorkoutPartEntities(): List<WorkoutPartEntity>

    @Query("SELECT * FROM workout_part WHERE id = :id")
    fun getWorkoutPartEntityById(id: Long): List<WorkoutPartEntity>
}