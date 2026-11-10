package com.framextv.v2.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.framextv.v2.ui.theme.NuvioTheme
import com.framextv.v2.ui.theme.brandWordmarkResource

@Composable
fun BrandWordmark(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1f,
    drawableOverride: Int? = null
) {
    Image(
        painter = painterResource(id = drawableOverride ?: NuvioTheme.currentTheme.brandWordmarkResource),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alpha = alpha
    )
}
