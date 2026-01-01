package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {

    @Query(
        """
        SELECT * FROM workout_exercise
        WHERE workoutDate = :date
        ORDER BY orderIndex
    """
    )
    fun getWorkoutExerciseEntities(date: String): Flow<List<WorkoutExerciseEntity>>

    @Insert
    suspend fun insert(entry: WorkoutExerciseEntity): Long

    @Query("DELETE FROM workout_exercise WHERE id = :id")
    suspend fun deleteById(id: Long)
}
