package com.runners.app.network

/**
 * Thrown when backend returns a non-2xx HTTP response.
 *
 * - `message` is intended to be user-facing (already localized when possible).
 * - `statusCode` is the HTTP status.
 */
class BackendApiException(
    val statusCode: Int,
    message: String,
) : IllegalStateException(message)

