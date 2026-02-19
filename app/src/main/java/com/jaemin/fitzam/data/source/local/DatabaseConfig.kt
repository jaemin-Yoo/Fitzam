package com.jaemin.fitzam.data.source.local

import com.jaemin.fitzam.BuildConfig

object DatabaseConfig {
    private const val BASE_NAME = "fitzam"
    private const val LOCAL_DB_NAME = "$BASE_NAME.db"

    private fun flavorSuffix(): String {
        val flavor = BuildConfig.FLAVOR
        return if (flavor.isBlank()) "" else "_$flavor"
    }

    fun localDbFileName(): String = LOCAL_DB_NAME

    fun driveDbFileName(): String = "$BASE_NAME${flavorSuffix()}.db"

    fun tempBackupFileName(): String = "${BASE_NAME}${flavorSuffix()}_backup.db"

    fun tempRestoreFileName(): String = "${BASE_NAME}${flavorSuffix()}_restore.db"
}
