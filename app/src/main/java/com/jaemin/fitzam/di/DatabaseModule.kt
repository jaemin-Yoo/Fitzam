package com.jaemin.fitzam.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jaemin.fitzam.data.source.local.FitzamDatabase
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.ImageUrlCacheDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.FavoriteExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutSetDao
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
            "fitzam.db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    db.execSQL(
                        """
                        INSERT INTO exercise_category (name, imageName, colorHex, colorDarkHex) VALUES
                        ('가슴', 'img_chest', 0xFF2563EB, 0xFF2563EB),
                        ('등', 'img_back', 0xFF06B6D4, 0xFF06B6D4),
                        ('어깨', 'img_shoulder', 0xFFD81DAF, 0xFFD81DAF),
                        ('삼두', 'img_triceps', 0xFF3CAD36, 0xFF3CAD36),
                        ('이두', 'img_biceps', 0xFFFACC15, 0xFFFACC15),
                        ('하체', 'img_lower_body', 0xFFF97316, 0xFFF97316),
                        ('복근', 'img_abs', 0xFF8B5CF6, 0xFF8B5CF6),
                        ('유산소', 'img_aerobic', 0xFF64748B, 0xFF64748B)
                        """.trimIndent()
                    )
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.execSQL("PRAGMA foreign_keys=ON;")
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

    @Provides
    fun provideImageUrlCacheDao(db: FitzamDatabase): ImageUrlCacheDao =
        db.imageUrlCacheDao()
}
