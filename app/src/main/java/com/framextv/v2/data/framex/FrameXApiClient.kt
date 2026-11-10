package com.framextv.v2.data.framex

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrameXApiClient @Inject constructor() : FrameXTransport {
    private val hosts: List<HttpUrl> = FrameXHosts.Default
    private val callFactory: Call.Factory = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun get(
        endpoint: String,
        query: Map<String, String>,
    ): FrameXResult<String> {
        require(endpoint.startsWith('/') && "://" !in endpoint && ".." !in endpoint) {
            "Endpoint must be an absolute path on a configured frameX host"
        }

        hosts.forEachIndexed { index, host ->
            val request = Request.Builder()
                .url(buildUrl(host, endpoint, query))
                .get()
                .build()

            val response = try {
                execute(request)
            } catch (_: IOException) {
                if (index == hosts.lastIndex) {
                    return FrameXResult.Failure("All frameX hosts unavailable")
                }
                return@forEachIndexed
            }

            if (response.status in 200..299) {
                return FrameXResult.Success(response.body)
            }
            if (!TransportPolicy.shouldTryNextHost(response.status)) {
                return FrameXResult.Failure("HTTP ${response.status}")
            }
            if (index == hosts.lastIndex) {
                return FrameXResult.Failure("All frameX hosts unavailable")
            }
        }

        return FrameXResult.Failure("All frameX hosts unavailable")
    }

    private fun buildUrl(
        host: HttpUrl,
        endpoint: String,
        query: Map<String, String>,
    ): HttpUrl = host.newBuilder()
        .encodedPath(endpoint)
        .apply {
            query.forEach { (name, value) -> addQueryParameter(name, value) }
        }
        .build()

    private suspend fun execute(request: Request): TransportResponse = suspendCancellableCoroutine { continuation ->
        val call = callFactory.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    response.use {
                        val transportResponse = TransportResponse(
                            status = it.code,
                            body = it.body?.string().orEmpty(),
                        )
                        if (continuation.isActive) continuation.resume(transportResponse)
                    }
                } catch (exception: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
            }
        })
    }

    private data class TransportResponse(
        val status: Int,
        val body: String,
    )
}
