package com.jaemin.fitzam.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaemin.fitzam.data.source.local.entity.ImageUrlCacheEntity

@Dao
interface ImageUrlCacheDao {
    @Query("SELECT * FROM image_url_cache WHERE path = :path")
    suspend fun find(path: String): ImageUrlCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ImageUrlCacheEntity)

    @Query("DELETE FROM image_url_cache WHERE path = :path")
    suspend fun delete(path: String)
}
