package com.runners.app.network

/**
 * Thrown when backend returns a non-2xx HTTP response.
 */
class BackendApiException(
    val statusCode: Int,
    message: String,
) : IllegalStateException(message)

