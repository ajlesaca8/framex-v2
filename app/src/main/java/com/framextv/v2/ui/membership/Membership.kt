package com.framextv.v2.ui.membership

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.framextv.v2.domain.model.MemberAccess
import com.framextv.v2.domain.model.MemberTier

val LocalMemberAccess = staticCompositionLocalOf { MemberAccess.None }

object Membership {
    val access: MemberAccess
        @Composable
        @ReadOnlyComposable
        get() = LocalMemberAccess.current

    val tier: MemberTier?
        @Composable
        @ReadOnlyComposable
        get() = access.tier
}
