@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.framextv.v2.ui.screens.stream

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.framextv.v2.data.framex.FrameXProvider
import com.framextv.v2.data.framex.FrameXProviderStatus
import com.framextv.v2.data.framex.FrameXProviderTag
private val PanelBg = Color(0xF0121620)
private val PanelBorder = Color(0x33FFFFFF)
private val RowBg = Color(0xFF1A2230)
private val RowFocused = Color(0xFF243044)
private val TagFhd = Color(0xFF2F6FED)
private val SoftWhite = Color(0xFFF5F7FA)
private val Muted = Color(0xFF9AA3B2)
private val AvailableGreen = Color(0xFF3DDC97)
private val OfflineRed = Color(0xFFE35D6A)
private val Amber = Color(0xFFF2A72E)

@Composable
fun FrameXServerSelectScreen(
    viewModel: FrameXServerSelectViewModel = hiltViewModel(),
    onBackPress: () -> Unit,
    onServerSelected: (ServerSelectResult) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onBackPress)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        val artwork = uiState.backdrop ?: uiState.poster
        if (!artwork.isNullOrBlank()) {
            AsyncImage(
                model = artwork,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.92f),
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.82f),
                            ),
                        ),
                    ),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.42f)
                .padding(start = 48.dp, top = 40.dp, bottom = 40.dp, end = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PanelBg)
                .border(1.dp, PanelBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            Text(
                text = "SELECT SERVER",
                color = SoftWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = uiState.title,
                color = Muted,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Text("Loading servers…", color = Amber)
                }
                uiState.errorMessage != null -> {
                    Text(uiState.errorMessage.orEmpty(), color = OfflineRed)
                    Spacer(Modifier.height(12.dp))
                    ServerActionRow(
                        label = "Retry",
                        enabled = true,
                        focusedByDefault = true,
                        onClick = viewModel::refresh,
                    )
                }
                else -> {
                    if (!uiState.connectErrorMessage.isNullOrBlank()) {
                        Text(uiState.connectErrorMessage.orEmpty(), color = OfflineRed)
                        Spacer(Modifier.height(10.dp))
                    }
                    val firstFocus = remember { FocusRequester() }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(
                            items = uiState.providers,
                            key = { _, item -> item.id },
                        ) { index, provider ->
                            ServerRow(
                                provider = provider,
                                connecting = uiState.connectingProviderId == provider.id,
                                focusRequester = firstFocus.takeIf { index == 0 },
                                enabled = provider.isSelectable && uiState.connectingProviderId == null,
                                onClick = {
                                    viewModel.selectProvider(provider, onServerSelected)
                                },
                            )
                        }
                    }
                    LaunchedEffect(uiState.providers) {
                        if (uiState.providers.isNotEmpty()) {
                            withFrameNanos { }
                            runCatching { firstFocus.requestFocus() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerRow(
    provider: FrameXProvider,
    connecting: Boolean,
    enabled: Boolean,
    focusRequester: FocusRequester?,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    val offline = provider.status == FrameXProviderStatus.OFFLINE
    val alpha = if (offline) 0.55f else 1f
    Box(
        Modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (!enabled) return@onPreviewKeyEvent false
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .clip(shape)
            .background(if (focused) RowFocused else RowBg)
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) SoftWhite.copy(alpha = 0.85f) else PanelBorder,
                shape = shape,
            )
            .focusable(enabled)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        androidx.compose.foundation.layout.Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = provider.name,
                    color = SoftWhite.copy(alpha = alpha),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                StatusPill(
                    connecting = connecting,
                    status = provider.status,
                )
            }
            if (provider.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    provider.tags.forEach { tag ->
                        TagChip(tag = tag, dimmed = offline)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    connecting: Boolean,
    status: FrameXProviderStatus,
) {
    val (label, color) = when {
        connecting -> "Connecting" to Amber
        status == FrameXProviderStatus.AVAILABLE -> "Available" to AvailableGreen
        status == FrameXProviderStatus.OFFLINE -> "Offline" to OfflineRed
        else -> "Unknown" to Muted
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TagChip(
    tag: FrameXProviderTag,
    dimmed: Boolean,
) {
    val (label, color) = when (tag) {
        FrameXProviderTag.FHD -> "FHD" to TagFhd
        FrameXProviderTag.UHD_4K -> "4K" to Color(0xFFE67E22)
        FrameXProviderTag.MULTI_LANG -> "Multi-Lang" to Color(0xFF8E44AD)
        FrameXProviderTag.MULTI_AUDIO -> "Multi-Audio" to Color(0xFF8E44AD)
        FrameXProviderTag.EMBED -> "Embed" to Color(0xFF1ECBEC)
    }
    Text(
        text = label,
        color = SoftWhite.copy(alpha = if (dimmed) 0.7f else 1f),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = if (dimmed) 0.45f else 0.85f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun ServerActionRow(
    label: String,
    enabled: Boolean,
    focusedByDefault: Boolean,
    onClick: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .clip(shape)
            .background(if (focused) Amber else RowBg)
            .border(1.dp, if (focused) SoftWhite else PanelBorder, shape)
            .focusable(enabled)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            color = if (focused) Color.Black else SoftWhite,
            fontWeight = FontWeight.SemiBold,
        )
    }
    if (focusedByDefault) {
        LaunchedEffect(Unit) {
            withFrameNanos { }
            runCatching { focusRequester.requestFocus() }
        }
    }
}
