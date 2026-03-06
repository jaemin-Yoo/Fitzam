package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.FavoriteExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteExerciseDao {

    @Query("SELECT * FROM favorite_exercise")
    fun getFavoriteExerciseEntities(): Flow<List<FavoriteExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: FavoriteExerciseEntity): Long

    @Query("DELETE FROM favorite_exercise WHERE exerciseId = :exerciseId")
    suspend fun deleteByExerciseId(exerciseId: Long)
}
