package com.jaemin.fitzam.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jaemin.fitzam.data.source.local.dao.WorkoutEntryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutSetDao
import com.jaemin.fitzam.data.source.local.entity.ExerciseEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutEntryEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutPartEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutRecordEntity
import com.jaemin.fitzam.data.source.local.entity.WorkoutSetEntity

@Database(
    entities = [
        WorkoutRecordEntity::class,
        WorkoutEntryEntity::class,
        WorkoutSetEntity::class,
        WorkoutPartEntity::class,
        ExerciseEntity::class,
    ],
    version = 1
)
abstract class FitzamDatabase : RoomDatabase() {
    abstract fun workoutRecordDao(): WorkoutRecordDao
    abstract fun workoutEntryDao(): WorkoutEntryDao
    abstract fun workoutSetDao(): WorkoutSetDao
}