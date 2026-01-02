package com.jaemin.fitzam.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_url_cache")
data class ImageUrlCacheEntity(
    @PrimaryKey val path: String,
    val url: String,
    val updatedAt: Long,
)
