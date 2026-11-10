package com.framextv.v2.ui.screens.addon

import com.framextv.v2.core.server.AddonWebConfigMode
import com.framextv.v2.domain.model.ExperienceMode
import com.framextv.v2.domain.model.UserProfile

internal object AddonManagementAccess {

    fun isReadOnly(profile: UserProfile?): Boolean {
        return profile?.let { !it.isPrimary && it.usesPrimaryAddons } == true
    }

    fun webConfigMode(
        profile: UserProfile?,
        experienceMode: ExperienceMode = ExperienceMode.ADVANCED
    ): AddonWebConfigMode {
        return when {
            isReadOnly(profile) -> AddonWebConfigMode.COLLECTIONS_ONLY
            experienceMode == ExperienceMode.ESSENTIAL -> AddonWebConfigMode.ADDONS_ONLY
            else -> AddonWebConfigMode.FULL
        }
    }
}
