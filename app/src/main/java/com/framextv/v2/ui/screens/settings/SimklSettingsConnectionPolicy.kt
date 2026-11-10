package com.framextv.v2.ui.screens.settings

import com.framextv.v2.data.simkl.SimklAuthState
import com.framextv.v2.data.simkl.SimklConnectionMode

internal fun shouldRefreshMissingSimklIdentity(
    state: SimklAuthState,
    isRefreshBlocked: Boolean
): Boolean = state.isAuthenticated && state.username.isNullOrBlank() && !isRefreshBlocked

internal fun shouldCancelSimklPolling(mode: SimklConnectionMode): Boolean =
    mode == SimklConnectionMode.DISCONNECTED
