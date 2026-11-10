package com.framextv.v2.ui.screens.stream

sealed class ServerSelectResult {
    data class NativeStream(val playbackInfo: StreamPlaybackInfo) : ServerSelectResult()
    data class EmbedPlayer(
        val embedUrl: String,
        val title: String,
        val contentId: String?,
        val contentType: String?,
    ) : ServerSelectResult()
}
