package com.jaemin.fitzam.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jaemin.fitzam.data.source.local.FitzamDatabase
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.FavoriteExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutExerciseDao
import com.jaemin.fitzam.data.source.local.dao.ExerciseCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutCategoryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutDao
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

private const val EXERCISE_CATEGORY_PATH = "fitzam/workout_part"

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
                                    imagePath = "${EXERCISE_CATEGORY_PATH}/chest.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                ExerciseCategoryEntity(
                                    name = "등",
                                    imagePath = "${EXERCISE_CATEGORY_PATH}/back.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                ExerciseCategoryEntity(
                                    name = "어깨",
                                    imagePath = "${EXERCISE_CATEGORY_PATH}/shoulder.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                ExerciseCategoryEntity(
                                    name = "삼두",
                                    imagePath = "${EXERCISE_CATEGORY_PATH}/triceps.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                ExerciseCategoryEntity(
                                    name = "이두",
                                    imagePath = "${EXERCISE_CATEGORY_PATH}/biceps.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                ExerciseCategoryEntity(
                                    name = "하체",
                                    imagePath = "${EXERCISE_CATEGORY_PATH}/lower_body.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                ExerciseCategoryEntity(
                                    name = "복근",
                                    imagePath = "${EXERCISE_CATEGORY_PATH}/abs.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                ExerciseCategoryEntity(
                                    name = "유산소",
                                    imagePath = "${EXERCISE_CATEGORY_PATH}/aerobic.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
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
}
