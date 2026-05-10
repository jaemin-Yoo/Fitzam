package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseSetMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutRecordExerciseSetMetricDao {

    @Query(
        """
        SELECT * FROM workout_record_exercise_set_metric
        WHERE workoutRecordExerciseId = :workoutRecordExerciseId
        ORDER BY setIndex, metricType
    """
    )
    fun getMetricEntities(workoutRecordExerciseId: Long): Flow<List<WorkoutRecordExerciseSetMetricEntity>>

    @Upsert
    suspend fun insertOrUpdateAll(metrics: List<WorkoutRecordExerciseSetMetricEntity>)

    @Query(
        """
        DELETE FROM workout_record_exercise_set_metric
        WHERE workoutRecordExerciseId = :workoutRecordExerciseId
        AND setIndex IN (:setIndexes)
    """
    )
    suspend fun deleteMetrics(
        workoutRecordExerciseId: Long,
        setIndexes: List<Int>,
    )
}
