package io.vonley.mi.common.base

import io.vonley.mi.extensions.e
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

interface BaseClient {

    val http: OkHttpClient

    val TAG: String
        get() = BaseClient::class.qualifiedName ?: BaseClient::javaClass.name

    /** Blocking POST. The caller owns the returned [Response] and must close it. */
    fun post(url: String, body: RequestBody, headers: Headers = Headers.headersOf()): Response? {
        val req = Request.Builder()
            .url(url)
            .headers(headers)
            .post(body)
            .build()
        return try {
            http.newCall(req).execute()
        } catch (e: Throwable) {
            "POST $url failed: ${e.message}".e(TAG, e)
            null
        }
    }

    /** Blocking GET. The caller owns the returned [Response] and must close it. */
    fun getRequest(url: String): Response? {
        val req = Request.Builder()
            .url(url)
            .get()
            .build()
        return try {
            http.newCall(req).execute()
        } catch (e: Throwable) {
            "GET $url failed: ${e.message}".e(TAG, e)
            null
        }
    }

    /** GET and read the whole body, always releasing the connection. */
    fun fetch(url: String): String? = getRequest(url)?.use { response ->
        if (response.isSuccessful) response.body.string() else null
    }
}
