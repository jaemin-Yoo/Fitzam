package com.jaemin.fitzam.data.source.local.seed

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase

class DefaultExerciseSeedManager(
    context: Context,
) {
    private val jsonLoader = DefaultExerciseSeedJsonLoader(context)

    fun seedIfNeeded(db: SupportSQLiteDatabase) {
        db.execSQL(CREATE_SEED_META_TABLE_SQL)

        val currentVersion = queryCurrentVersion(db)
        if (currentVersion >= EXERCISE_SEED_VERSION) {
            return
        }

        val seedData = jsonLoader.load()

        db.beginTransaction()
        try {
            seedData.categories.forEach { category ->
                db.execSQL(
                    INSERT_EXERCISE_CATEGORY_IF_NOT_EXISTS_SQL,
                    arrayOf(
                        category.id,
                        category.name,
                        category.imageName,
                        category.colorHex,
                        category.colorDarkHex,
                    )
                )
            }

            seedData.exercises.forEach { exercise ->
                db.execSQL(
                    INSERT_EXERCISE_IF_NOT_EXISTS_SQL,
                    arrayOf(
                        exercise.id,
                        exercise.name,
                        exercise.categoryId,
                        exercise.imageName,
                        resolveRecordSchema(exercise.name),
                    )
                )
            }

            db.execSQL(
                UPSERT_SEED_VERSION_SQL,
                arrayOf(SEED_KEY_EXERCISE_DEFAULT_DATA, EXERCISE_SEED_VERSION.toString())
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun queryCurrentVersion(db: SupportSQLiteDatabase): Int {
        db.query(
            QUERY_SEED_VERSION_SQL,
            arrayOf(SEED_KEY_EXERCISE_DEFAULT_DATA)
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return 0
            }
            return cursor.getString(0)?.toIntOrNull() ?: 0
        }
    }

    companion object {
        private const val EXERCISE_SEED_VERSION = 2
        private const val SEED_KEY_EXERCISE_DEFAULT_DATA = "exercise_default_data"

        private val CREATE_SEED_META_TABLE_SQL =
            """
            CREATE TABLE IF NOT EXISTS seed_meta (
                key TEXT NOT NULL PRIMARY KEY,
                value TEXT NOT NULL
            )
            """.trimIndent()

        private const val QUERY_SEED_VERSION_SQL =
            "SELECT value FROM seed_meta WHERE key = ? LIMIT 1"

        private val UPSERT_SEED_VERSION_SQL =
            """
            INSERT INTO seed_meta(key, value) VALUES (?, ?)
            ON CONFLICT(key) DO UPDATE SET value = excluded.value
            """.trimIndent()

        private const val INSERT_EXERCISE_CATEGORY_IF_NOT_EXISTS_SQL =
            "INSERT OR IGNORE INTO exercise_category (id, name, imageName, colorHex, colorDarkHex) VALUES (?, ?, ?, ?, ?)"

        private const val INSERT_EXERCISE_IF_NOT_EXISTS_SQL =
            "INSERT OR IGNORE INTO exercise (id, name, categoryId, imageName, recordSchema) VALUES (?, ?, ?, ?, ?)"
    }

    private fun resolveRecordSchema(exerciseName: String): String {
        return when (exerciseName) {
            "러닝", "사이클" -> "DISTANCE_DURATION"
            else -> "WEIGHT_REPS"
        }
    }
}
