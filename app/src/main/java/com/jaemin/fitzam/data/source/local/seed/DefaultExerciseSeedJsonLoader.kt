package com.jaemin.fitzam.data.source.local.seed

import android.content.Context
import org.json.JSONArray

class DefaultExerciseSeedJsonLoader(
    private val context: Context,
) {

    fun load(): DefaultExerciseSeedData {
        val categoriesJson = readAsset(EXERCISE_CATEGORY_JSON_PATH)
        val exercisesJson = readAsset(EXERCISE_JSON_PATH)

        return DefaultExerciseSeedData(
            categories = parseCategories(categoriesJson),
            exercises = parseExercises(exercisesJson),
        )
    }

    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    }

    private fun parseCategories(json: String): List<SeedExerciseCategory> {
        val jsonArray = JSONArray(json)
        return buildList(capacity = jsonArray.length()) {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                add(
                    SeedExerciseCategory(
                        id = item.getLong("id"),
                        name = item.getString("name"),
                        imageName = item.getString("imageName"),
                        colorHex = parseColorHex(item.getString("colorHex")),
                        colorDarkHex = parseColorHex(item.getString("colorDarkHex")),
                    )
                )
            }
        }
    }

    private fun parseExercises(json: String): List<SeedExercise> {
        val sanitizedJson = sanitizeExerciseJson(json)
        val jsonArray = JSONArray(sanitizedJson)
        return buildList(capacity = jsonArray.length()) {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                add(
                    SeedExercise(
                        id = item.getLong("id"),
                        name = item.getString("name"),
                        categoryId = item.getLong("categoryId"),
                        imageName = item.getString("imageName"),
                        equipmentType = if (item.has("equipmentType")) {
                            item.getString("equipmentType")
                        } else {
                            null
                        },
                    )
                )
            }
        }
    }

    private fun sanitizeExerciseJson(json: String): String {
        return json.replace(Regex("(?m)^\\s*//.*$"), "")
    }

    private fun parseColorHex(value: String): Long {
        return value.removePrefix("0x").toLong(radix = 16)
    }

    companion object {
        private const val EXERCISE_CATEGORY_JSON_PATH =
            "default_data/exercise_categories.json"

        private const val EXERCISE_JSON_PATH =
            "default_data/exercises.json"
    }
}

data class DefaultExerciseSeedData(
    val categories: List<SeedExerciseCategory>,
    val exercises: List<SeedExercise>,
)

data class SeedExerciseCategory(
    val id: Long,
    val name: String,
    val imageName: String,
    val colorHex: Long,
    val colorDarkHex: Long,
)

data class SeedExercise(
    val id: Long,
    val name: String,
    val categoryId: Long,
    val imageName: String,
    val equipmentType: String?,
)
