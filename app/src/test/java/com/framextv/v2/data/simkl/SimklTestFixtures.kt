package com.framextv.v2.data.simkl

internal fun testSimklAuthorization(
    token: String = "token",
    profileId: Int = 1,
    generation: Long = 0L
) = SimklAuthorization(
    scope = SimklAuthScope(profileId = profileId, generation = generation),
    accessToken = token
)
