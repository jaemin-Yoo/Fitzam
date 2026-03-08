package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutRecordExerciseDao {

    @Query(
        """
        SELECT * FROM workout_record_exercise
        WHERE workoutRecordDate = :date
        ORDER BY orderIndex
    """
    )
    fun getWorkoutRecordExerciseEntities(date: String): Flow<List<WorkoutRecordExerciseEntity>>

    @Insert
    suspend fun insert(entry: WorkoutRecordExerciseEntity): Long

    @Query("DELETE FROM workout_record_exercise WHERE workoutRecordDate = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM workout_record_exercise WHERE id = :id")
    suspend fun deleteById(id: Long)
}
