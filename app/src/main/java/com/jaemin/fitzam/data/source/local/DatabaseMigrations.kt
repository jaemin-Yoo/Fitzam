package com.jaemin.fitzam.data.source.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=OFF")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_record (
                date TEXT NOT NULL,
                PRIMARY KEY(date)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO workout_record (date)
            SELECT date FROM workout
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_record_exercise_category (
                workoutRecordDate TEXT NOT NULL,
                exerciseCategoryId INTEGER NOT NULL,
                PRIMARY KEY(workoutRecordDate, exerciseCategoryId),
                FOREIGN KEY(workoutRecordDate) REFERENCES workout_record(date) ON DELETE CASCADE,
                FOREIGN KEY(exerciseCategoryId) REFERENCES exercise_category(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO workout_record_exercise_category (workoutRecordDate, exerciseCategoryId)
            SELECT workoutDate, exerciseCategoryId FROM workout_category
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_record_exercise (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                workoutRecordDate TEXT NOT NULL,
                exerciseId INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL,
                recordSchema TEXT NOT NULL DEFAULT 'WEIGHT_REPS',
                FOREIGN KEY(workoutRecordDate) REFERENCES workout_record(date) ON DELETE CASCADE,
                FOREIGN KEY(exerciseId) REFERENCES exercise(id) ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO workout_record_exercise (id, workoutRecordDate, exerciseId, orderIndex)
            SELECT id, workoutDate, exerciseId, orderIndex FROM workout_exercise
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_record_exercise_workoutRecordDate
            ON workout_record_exercise(workoutRecordDate)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_record_exercise_exerciseId
            ON workout_record_exercise(exerciseId)
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_record_exercise_set (
                workoutRecordExerciseId INTEGER NOT NULL,
                setIndex INTEGER NOT NULL,
                PRIMARY KEY(workoutRecordExerciseId, setIndex),
                FOREIGN KEY(workoutRecordExerciseId) REFERENCES workout_record_exercise(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO workout_record_exercise_set (workoutRecordExerciseId, setIndex)
            SELECT workoutExerciseId, setIndex FROM workout_set
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_record_exercise_set_workoutRecordExerciseId
            ON workout_record_exercise_set(workoutRecordExerciseId)
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_record_exercise_set_metric (
                workoutRecordExerciseId INTEGER NOT NULL,
                setIndex INTEGER NOT NULL,
                metricType TEXT NOT NULL,
                value REAL NOT NULL,
                PRIMARY KEY(workoutRecordExerciseId, setIndex, metricType),
                FOREIGN KEY(workoutRecordExerciseId, setIndex)
                    REFERENCES workout_record_exercise_set(workoutRecordExerciseId, setIndex)
                    ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_record_exercise_set_metric_workoutRecordExerciseId
            ON workout_record_exercise_set_metric(workoutRecordExerciseId)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_workout_record_exercise_set_metric_workoutRecordExerciseId_setIndex
            ON workout_record_exercise_set_metric(workoutRecordExerciseId, setIndex)
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE IF EXISTS workout_set")
        db.execSQL("DROP TABLE IF EXISTS workout_exercise")
        db.execSQL("DROP TABLE IF EXISTS workout_category")
        db.execSQL("DROP TABLE IF EXISTS workout")

        db.execSQL(
            """
            ALTER TABLE exercise
            ADD COLUMN recordSchema TEXT NOT NULL DEFAULT 'WEIGHT_REPS'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE exercise
            SET recordSchema = 'DISTANCE_DURATION'
            WHERE name IN ('러닝', '사이클')
            """.trimIndent(),
        )

        db.execSQL(
            """
            UPDATE workout_record_exercise
            SET recordSchema = CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM workout_record_exercise_set_metric metric
                    WHERE metric.workoutRecordExerciseId = workout_record_exercise.id
                      AND metric.metricType = 'DISTANCE_KM'
                )
                AND EXISTS (
                    SELECT 1
                    FROM workout_record_exercise_set_metric metric
                    WHERE metric.workoutRecordExerciseId = workout_record_exercise.id
                      AND metric.metricType = 'DURATION_SEC'
                ) THEN 'DISTANCE_DURATION'
                ELSE 'WEIGHT_REPS'
            END
            """.trimIndent(),
        )

        db.execSQL(
            """
            ALTER TABLE exercise
            ADD COLUMN equipmentType TEXT NOT NULL DEFAULT 'OTHER'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE exercise
            SET equipmentType = CASE
                WHEN name LIKE '%머신%' THEN 'MACHINE'
                WHEN name LIKE '%바벨%' THEN 'BARBELL'
                WHEN name LIKE '%덤벨%' THEN 'DUMBBELL'
                WHEN name LIKE '%케틀벨%' THEN 'KETTLEBELL'
                WHEN name IN ('푸시업', '딥스', '크런치', '레그 레이즈', '플랭크', '바이시클 크런치', '러닝', '줄넘기') THEN 'BODYWEIGHT'
                ELSE 'OTHER'
            END
            """.trimIndent(),
        )

        db.execSQL("PRAGMA foreign_keys=ON")
    }
}
