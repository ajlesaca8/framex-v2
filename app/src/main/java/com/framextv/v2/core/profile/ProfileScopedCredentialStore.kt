package com.framextv.v2.core.profile

interface ProfileScopedCredentialStore {
    fun removeProfile(profileId: Int)
    fun clearAllProfiles()
}
