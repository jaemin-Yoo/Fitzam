package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.WorkoutEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutEntryDao {

    @Query(
        """
        SELECT * FROM workout_entry
        WHERE recordDate = :date
        ORDER BY orderIndex
    """
    )
    fun getWorkoutEntryEntities(date: String): Flow<List<WorkoutEntryEntity>>

    @Insert
    suspend fun insert(entry: WorkoutEntryEntity): Long

    @Query("DELETE FROM workout_entry WHERE id = :id")
    suspend fun deleteById(id: Long)
}