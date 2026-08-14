package com.android.purebilibili.feature.video.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

/**
 * Semantic shapes for the video detail surface.
 *
 * The single Miuix theme retains the existing app container tokens and is
 * therefore unaffected by Material-specific tuning.
 */
object VideoDetailShapes {

    @Composable
    fun contentCard(): Shape = AppShapes.container(ContainerLevel.Card)

    @Composable
    fun media(): Shape = AppShapes.container(ContainerLevel.Field)

    @Composable
    fun field(): Shape = AppShapes.container(ContainerLevel.Field)

    @Composable
    fun leadingIcon(): Shape = AppShapes.container(ContainerLevel.Chip)

    @Composable
    fun compactIcon(): Shape = AppShapes.container(ContainerLevel.Chip)

    @Composable
    fun action(): Shape = AppShapes.container(ContainerLevel.Card)
}
