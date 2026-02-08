package com.jaemin.fitzam.data.source.remote.drive

import com.google.api.services.drive.Drive
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveServiceFactory @Inject constructor() {

    fun createDriveService(session: DriveAuthSession): Drive {
        val credential = GoogleCredential().setAccessToken(session.accessToken)
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential,
        )
            .setApplicationName("Fitzam")
            .build()
    }
}

