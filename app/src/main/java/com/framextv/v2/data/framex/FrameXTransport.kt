package com.framextv.v2.data.framex

interface FrameXTransport {
    suspend fun get(
        endpoint: String,
        query: Map<String, String> = emptyMap(),
    ): FrameXResult<String>
}
