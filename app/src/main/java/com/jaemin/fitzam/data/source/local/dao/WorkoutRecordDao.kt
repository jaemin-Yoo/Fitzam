package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
    fun getWorkoutRecordEntity(
        startDate: String,
        endDate: String,
    ): Flow<WorkoutRecordEntity>

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insert(record: WorkoutRecordEntity)

    @Update
    suspend fun update(record: WorkoutRecordEntity)

    @Query("DELETE FROM workout_record WHERE date = :date")
    suspend fun deleteByDate(date: String)
}