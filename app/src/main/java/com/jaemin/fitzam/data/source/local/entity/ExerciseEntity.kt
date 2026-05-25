package com.jaemin.fitzam.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val imageName: String,
    val equipmentType: String,
    val recordSchema: String,
    @ColumnInfo(defaultValue = "0")
    val isCustom: Boolean = false,
)
