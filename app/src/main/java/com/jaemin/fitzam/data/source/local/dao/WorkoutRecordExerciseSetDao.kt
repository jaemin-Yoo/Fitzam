package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutRecordExerciseSetDao {

    @Query(
        """
        SELECT * FROM workout_record_exercise_set
        WHERE workoutRecordExerciseId = :workoutRecordExerciseId
        ORDER BY setIndex
    """
    )
    fun getSetEntities(workoutRecordExerciseId: Long): Flow<List<WorkoutRecordExerciseSetEntity>>

    @Upsert
    suspend fun insertOrUpdateAll(sets: List<WorkoutRecordExerciseSetEntity>)

    @Query(
        """
            DELETE FROM workout_record_exercise_set
            WHERE workoutRecordExerciseId = :workoutRecordExerciseId
            AND setIndex IN (:setIndexes)
    """
    )
    suspend fun deleteSets(
        workoutRecordExerciseId: Long,
        setIndexes: List<Int>,
    )
}
