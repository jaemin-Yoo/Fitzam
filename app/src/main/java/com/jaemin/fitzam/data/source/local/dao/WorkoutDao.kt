package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query(
        """
        SELECT *
        FROM workout
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date
    """
    )
    fun getWorkoutEntities(
        startDate: String,
        endDate: String,
    ): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workout: WorkoutEntity)

    @Query("DELETE FROM workout WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
