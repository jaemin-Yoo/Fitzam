package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.source.local.dao.ImageUrlCacheDao
import com.jaemin.fitzam.data.source.local.entity.ImageUrlCacheEntity
import com.jaemin.fitzam.data.source.remote.FirebaseUtil
import javax.inject.Inject

class ImageUrlRepository @Inject constructor(
    private val cacheDao: ImageUrlCacheDao,
) {

    suspend fun getImageUrl(path: String): String {
        val cached = cacheDao.find(path)
        if (cached != null) {
            return cached.url
        }

        val url = FirebaseUtil.getImageUrl(path)
        cacheDao.upsert(
            ImageUrlCacheEntity(
                path = path,
                url = url,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        return url
    }

    suspend fun invalidate(path: String) {
        cacheDao.delete(path)
    }
}
