package com.jaemin.fitzam.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jaemin.fitzam.data.source.local.DatabaseConfig
import com.jaemin.fitzam.data.source.local.FitzamDatabase
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.FavoriteExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutSetDao
import com.jaemin.fitzam.data.source.local.seed.DefaultExerciseSeedManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FitzamDatabase =
        Room.databaseBuilder(
            context,
            FitzamDatabase::class.java,
            DatabaseConfig.localDbFileName()
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    runCatching {
                        DefaultExerciseSeedManager(context).seedIfNeeded(db)
                    }.onFailure { throwable ->
                        Log.e(
                            DATABASE_SEED_LOG_TAG,
                            "기본 운동 데이터 로드에 실패했습니다.",
                            throwable
                        )
                    }
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.execSQL("PRAGMA foreign_keys=ON;")
                    runCatching {
                        DefaultExerciseSeedManager(context).seedIfNeeded(db)
                    }.onFailure { throwable ->
                        Log.e(
                            DATABASE_SEED_LOG_TAG,
                            "기존 DB 시드 보정에 실패했습니다.",
                            throwable
                        )
                    }
                }
            })
            .build()

    @Provides
    fun provideWorkoutDao(db: FitzamDatabase): WorkoutDao =
        db.workoutDao()

    @Provides
    fun provideWorkoutCategoryDao(db: FitzamDatabase): WorkoutCategoryDao =
        db.workoutCategoryDao()

    @Provides
    fun provideWorkoutExerciseDao(db: FitzamDatabase): WorkoutExerciseDao =
        db.workoutExerciseDao()

    @Provides
    fun provideWorkoutSetDao(db: FitzamDatabase): WorkoutSetDao =
        db.workoutSetDao()

    @Provides
    fun provideExerciseCategoryDao(db: FitzamDatabase): ExerciseCategoryDao =
        db.exerciseCategoryDao()

    @Provides
    fun provideExerciseDao(db: FitzamDatabase): ExerciseDao =
        db.exerciseDao()

    @Provides
    fun provideFavoriteExerciseDao(db: FitzamDatabase): FavoriteExerciseDao =
        db.favoriteExerciseDao()

    private const val DATABASE_SEED_LOG_TAG = "DatabaseSeed"
}
