package com.jaemin.fitzam.data.source.remote.drive

import com.google.api.services.drive.Drive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveServiceFactory @Inject constructor() {

    fun createDriveService(session: DriveAuthSession): Drive {
        throw UnsupportedOperationException("Drive service not configured yet")
    }
}

