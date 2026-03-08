package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutRecordDao {

    @Query(
        """
        SELECT *
        FROM workout_record
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date
    """
    )
    fun getWorkoutRecordEntities(
        startDate: String,
        endDate: String,
    ): Flow<List<WorkoutRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workoutRecord: WorkoutRecordEntity)

    @Query("DELETE FROM workout_record WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
