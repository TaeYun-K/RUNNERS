package com.runners.app.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object PresignedUploadClient {
    fun put(uploadUrl: String, contentType: String, bytes: ByteArray) {
        val mediaType = contentType.toMediaType()
        val request = Request.Builder()
            .url(uploadUrl)
            .put(bytes.toRequestBody(mediaType))
            .header("Content-Type", contentType)
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Upload failed: HTTP ${response.code} ${responseBody.take(300)}")
            }
        }
    }
}

