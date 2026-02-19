package com.jaemin.fitzam.data.source.remote.drive

import com.google.api.services.drive.Drive
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveServiceFactory @Inject constructor() {

    fun createDriveService(session: DriveAuthSession): Drive {
        val accessToken = AccessToken(session.accessToken, null)
        val credentials = GoogleCredentials.create(accessToken)
        val requestInitializer = HttpCredentialsAdapter(credentials)
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            requestInitializer,
        )
            .setApplicationName("Fitzam")
            .build()
    }
}
