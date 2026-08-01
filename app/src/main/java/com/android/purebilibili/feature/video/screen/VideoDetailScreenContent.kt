package com.android.purebilibili.feature.video.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun VideoDetailScreenContent(
    isFullscreenMode: Boolean,
    backgroundColor: Color,
    modifier: Modifier,
    mainContent: @Composable BoxScope.() -> Unit,
    overlayContent: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isFullscreenMode) Color.Black else backgroundColor)
    ) {
        mainContent()
        overlayContent()
    }
}
