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
        SELECT DISTINCT wr.*
        FROM workout_record wr
        LEFT JOIN workout_record_exercise_category wrec ON wr.date = wrec.workoutRecordDate
        WHERE wr.date BETWEEN :startDate AND :endDate
        ORDER BY wr.date
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
