package com.framextv.v2.data.framex

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object FrameXHosts {
    val Default: List<HttpUrl> = listOf(
        "https://api.framextv.cc/".toHttpUrl(),
        "https://api.framextv.xyz/".toHttpUrl(),
        "https://api.framextv.tech/".toHttpUrl(),
        "https://api.framextv.pro/".toHttpUrl(),
    )
}

object TransportPolicy {
    fun shouldTryNextHost(status: Int): Boolean = status == 408 || status == 429 || status >= 500
}
