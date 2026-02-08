package com.jaemin.fitzam.data.source.remote.drive

data class DriveAuthSession(
    val email: String,
    val accessToken: String,
    val grantedScopes: List<String>,
)

