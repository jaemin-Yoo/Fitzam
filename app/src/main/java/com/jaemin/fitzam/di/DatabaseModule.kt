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
import com.jaemin.fitzam.data.source.local.entity.ExerciseCategoryEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

                    CoroutineScope(Dispatchers.IO).launch {
                        val database = Room.databaseBuilder(
                            context,
                            FitzamDatabase::class.java,
                            "fitzam.db"
                        ).build()

                        database.exerciseCategoryDao().insertAll(
                            listOf(
                                ExerciseCategoryEntity(
                                    name = "가슴",
                                    imageName = "img_chest",
                                    colorHex = 0xFF2563EB,
                                    colorDarkHex = 0xFF2563EB,
                                ),
                                ExerciseCategoryEntity(
                                    name = "등",
                                    imageName = "img_back",
                                    colorHex = 0xFF06B6D4,
                                    colorDarkHex = 0xFF06B6D4,
                                ),
                                ExerciseCategoryEntity(
                                    name = "어깨",
                                    imageName = "img_shoulder",
                                    colorHex = 0xFFD81DAF,
                                    colorDarkHex = 0xFFD81DAF,
                                ),
                                ExerciseCategoryEntity(
                                    name = "삼두",
                                    imageName = "img_triceps",
                                    colorHex = 0xFF3CAD36,
                                    colorDarkHex = 0xFF3CAD36,
                                ),
                                ExerciseCategoryEntity(
                                    name = "이두",
                                    imageName = "img_biceps",
                                    colorHex = 0xFFFACC15,
                                    colorDarkHex = 0xFFFACC15,
                                ),
                                ExerciseCategoryEntity(
                                    name = "하체",
                                    imageName = "img_lower_body",
                                    colorHex = 0xFFF97316,
                                    colorDarkHex = 0xFFF97316,
                                ),
                                ExerciseCategoryEntity(
                                    name = "복근",
                                    imageName = "img_abs",
                                    colorHex = 0xFF8B5CF6,
                                    colorDarkHex = 0xFF8B5CF6,
                                ),
                                ExerciseCategoryEntity(
                                    name = "유산소",
                                    imageName = "img_aerobic",
                                    colorHex = 0xFF64748B,
                                    colorDarkHex = 0xFF64748B,
                                ),
                            )
                        )
                    }
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
