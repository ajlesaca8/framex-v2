package com.framextv.v2.data.framex

data class FrameXProvider(
    val id: String,
    val name: String,
    val tagline: String?,
    val category: String?,
    val status: FrameXProviderStatus,
) {
    val isSelectable: Boolean
        get() = status == FrameXProviderStatus.AVAILABLE

    val tags: List<FrameXProviderTag>
        get() = FrameXProviderTags.from(tagline)
}

enum class FrameXProviderStatus {
    AVAILABLE,
    OFFLINE,
    UNKNOWN,
}

enum class FrameXProviderTag {
    FHD,
    UHD_4K,
    MULTI_LANG,
    MULTI_AUDIO,
    EMBED,
}

object FrameXProviderTags {
    fun from(tagline: String?): List<FrameXProviderTag> {
        val text = tagline.orEmpty().lowercase()
        if (text.isBlank()) return emptyList()
        val tags = linkedSetOf<FrameXProviderTag>()
        if (text.contains("embed") || text.contains("webview") || text.contains("web player")) {
            tags += FrameXProviderTag.EMBED
        }
        if (text.contains("4k") || text.contains("2160") || text.contains("ultra")) {
            tags += FrameXProviderTag.UHD_4K
        } else if (text.contains("1080") || text.contains("fhd") || text.contains("hd")) {
            tags += FrameXProviderTag.FHD
        }
        if (text.contains("multi-language") || text.contains("multi language") || text.contains("multi-lang")) {
            tags += FrameXProviderTag.MULTI_LANG
        }
        if (text.contains("multi-audio") || text.contains("multi audio")) {
            tags += FrameXProviderTag.MULTI_AUDIO
        }
        return tags.toList()
    }
}

fun ProviderDto.toDomain(): FrameXProvider? {
    val providerId = id?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val providerName = name?.trim()?.takeIf { it.isNotEmpty() } ?: providerId
    val normalized = status?.trim()?.lowercase().orEmpty()
    val mappedStatus = when (normalized) {
        "available", "up", "online", "ok" -> FrameXProviderStatus.AVAILABLE
        "down", "offline", "unavailable", "error" -> FrameXProviderStatus.OFFLINE
        else -> FrameXProviderStatus.UNKNOWN
    }
    return FrameXProvider(
        id = providerId,
        name = providerName,
        tagline = tagline?.trim()?.takeIf { it.isNotEmpty() },
        category = category?.trim()?.takeIf { it.isNotEmpty() },
        status = mappedStatus,
    )
}
