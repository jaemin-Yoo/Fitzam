package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jaemin.fitzam.data.source.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {

    @Query(
        """
        SELECT * FROM workout_set
        WHERE entryId = :entryId
        ORDER BY setIndex
    """
    )
    fun getSetEntities(entryId: Long): Flow<List<WorkoutSetEntity>>

    @Upsert
    suspend fun insertOrUpdateAll(sets: List<WorkoutSetEntity>)

    @Query(
        """
            DELETE FROM workout_set
            WHERE entryId = :entryId
            AND setIndex IN (:setIndexes)
    """
    )
    suspend fun deleteSets(
        entryId: Long,
        setIndexes: List<Int>,
    )
}