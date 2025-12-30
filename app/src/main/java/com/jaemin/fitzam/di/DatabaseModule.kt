package com.jaemin.fitzam.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jaemin.fitzam.data.source.local.FitzamDatabase
import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import com.jaemin.fitzam.data.source.local.dao.FavoriteExerciseDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutEntryDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutPartDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutRecordDao
import com.jaemin.fitzam.data.source.local.dao.WorkoutSetDao
import com.jaemin.fitzam.data.source.local.entity.WorkoutPartEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

private const val WORKOUT_PART_PATH = "fitzam/workout_part"

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

                        database.workoutPartDao().insertAll(
                            listOf(
                                WorkoutPartEntity(
                                    name = "가슴",
                                    imagePath = "${WORKOUT_PART_PATH}/chest.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                WorkoutPartEntity(
                                    name = "등",
                                    imagePath = "${WORKOUT_PART_PATH}/back.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                WorkoutPartEntity(
                                    name = "어깨",
                                    imagePath = "${WORKOUT_PART_PATH}/shoulder.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                WorkoutPartEntity(
                                    name = "삼두",
                                    imagePath = "${WORKOUT_PART_PATH}/triceps.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                WorkoutPartEntity(
                                    name = "이두",
                                    imagePath = "${WORKOUT_PART_PATH}/biceps.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                WorkoutPartEntity(
                                    name = "하체",
                                    imagePath = "${WORKOUT_PART_PATH}/lower_body.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                WorkoutPartEntity(
                                    name = "복근",
                                    imagePath = "${WORKOUT_PART_PATH}/abs.png",
                                    colorHex = "0xFFD32F2F",
                                    colorDarkHex = "0xFFD32F2F",
                                ),
                                WorkoutPartEntity(
                                    name = "유산소",
                                    imagePath = "${WORKOUT_PART_PATH}/aerobic.png",
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
    fun provideWorkoutRecordDao(db: FitzamDatabase): WorkoutRecordDao =
        db.workoutRecordDao()

    @Provides
    fun provideWorkoutEntryDao(db: FitzamDatabase): WorkoutEntryDao =
        db.workoutEntryDao()

    @Provides
    fun provideWorkoutSetDao(db: FitzamDatabase): WorkoutSetDao =
        db.workoutSetDao()

    @Provides
    fun provideWorkoutPartDao(db: FitzamDatabase): WorkoutPartDao =
        db.workoutPartDao()

    @Provides
    fun provideExerciseDao(db: FitzamDatabase): ExerciseDao =
        db.exerciseDao()

    @Provides
    fun provideFavoriteExerciseDao(db: FitzamDatabase): FavoriteExerciseDao =
        db.favoriteExerciseDao()
}