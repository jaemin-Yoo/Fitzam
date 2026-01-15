package com.jaemin.fitzam.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.FavoriteExerciseDao
import com.jaemin.fitzam.data.source.local.dao.ImageUrlCacheDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutExerciseDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutSetDao
import com.jaemin.fitzam.data.source.local.entity.ExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.FavoriteExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.ImageUrlCacheEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.ExerciseCategoryEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutCategoryEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutSetEntity

@Database(
    entities = [
        WorkoutEntity::class,
        WorkoutCategoryEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        ExerciseCategoryEntity::class,
        ExerciseEntity::class,
        FavoriteExerciseEntity::class,
        ImageUrlCacheEntity::class,
    ],
    version = 1
)
abstract class FitzamDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutCategoryDao(): WorkoutCategoryDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun exerciseCategoryDao(): ExerciseCategoryDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun favoriteExerciseDao(): FavoriteExerciseDao
    abstract fun imageUrlCacheDao(): ImageUrlCacheDao
}
