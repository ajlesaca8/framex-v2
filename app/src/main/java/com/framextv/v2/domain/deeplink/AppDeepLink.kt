package com.framextv.v2.domain.deeplink

sealed interface AppDeepLink {
    data class Meta(
        val type: String,
        val id: String
    ) : AppDeepLink

    data class AddonInstall(
        val manifestUrl: String
    ) : AppDeepLink
}
