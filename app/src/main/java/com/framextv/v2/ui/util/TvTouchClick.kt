package com.framextv.v2.ui.util

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Adds pointer/touch tap handling for TV Material components.
 *
 * `androidx.tv.material3` Card/Surface `onClick` is wired through D-pad Enter only
 * (`handleDPadEnter`), so taps on phones do nothing. This uses [detectTapGestures]
 * (not [androidx.compose.foundation.clickable]) so TV remotes do not double-fire.
 */
fun Modifier.tvTouchClick(onClick: () -> Unit): Modifier = composed {
    val latestOnClick by rememberUpdatedState(onClick)
    pointerInput(Unit) {
        detectTapGestures(onTap = { latestOnClick() })
    }
}
