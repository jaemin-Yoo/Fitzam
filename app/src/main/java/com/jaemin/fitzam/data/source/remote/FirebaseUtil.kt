package com.jaemin.fitzam.data.source.remote

import com.google.firebase.Firebase
import com.google.firebase.storage.storage

object FirebaseUtil {

    fun getImageUrl(
        path: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val storageRef = Firebase.storage.reference.child(path)

        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}