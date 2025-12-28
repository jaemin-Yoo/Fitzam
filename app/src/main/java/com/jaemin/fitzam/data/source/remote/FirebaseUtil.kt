package com.jaemin.fitzam.data.source.remote

import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

object FirebaseUtil {

    suspend fun getImageUrl(path: String): String = suspendCancellableCoroutine { cont ->
        val storageRef = Firebase.storage.reference.child(path)

        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                cont.resume(uri.toString())
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }
}
