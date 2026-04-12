package com.jaemin.fitzam.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.FavoriteExerciseDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseSetDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordExerciseSetMetricDao
import com.jaemin.fitzam.data.source.local.entity.ExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.FavoriteExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.ExerciseCategoryEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseCategoryEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseSetEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordExerciseSetMetricEntity

@Database(
    entities = [
        WorkoutRecordEntity::class,
        WorkoutRecordExerciseCategoryEntity::class,
        WorkoutRecordExerciseEntity::class,
        WorkoutRecordExerciseSetEntity::class,
        WorkoutRecordExerciseSetMetricEntity::class,
        ExerciseCategoryEntity::class,
        ExerciseEntity::class,
        FavoriteExerciseEntity::class,
    ],
    version = 5
)
abstract class FitzamDatabase : RoomDatabase() {
    abstract fun workoutRecordDao(): WorkoutRecordDao
    abstract fun workoutRecordExerciseCategoryDao(): WorkoutRecordExerciseCategoryDao
    abstract fun workoutRecordExerciseDao(): WorkoutRecordExerciseDao
    abstract fun workoutRecordExerciseSetDao(): WorkoutRecordExerciseSetDao
    abstract fun workoutRecordExerciseSetMetricDao(): WorkoutRecordExerciseSetMetricDao
    abstract fun exerciseCategoryDao(): ExerciseCategoryDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun favoriteExerciseDao(): FavoriteExerciseDao
}
