package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseCategoryEntity

@Dao
interface WorkoutRecordExerciseCategoryDao {

    @Query("SELECT exerciseCategoryId FROM workout_record_exercise_category WHERE workoutRecordDate = :date")
    suspend fun getExerciseCategoryIds(date: String): List<Long>

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insert(workoutRecordExerciseCategory: WorkoutRecordExerciseCategoryEntity)

    @Query("DELETE FROM workout_record_exercise_category WHERE workoutRecordDate = :date")
    suspend fun deleteByDate(date: String)
}
