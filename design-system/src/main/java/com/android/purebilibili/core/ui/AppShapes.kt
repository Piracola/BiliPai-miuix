package com.android.purebilibili.core.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens
import com.android.purebilibili.core.theme.resolveCornerRadiusScale

/** Semantic container categories shared across the three UI presets. */
enum class ContainerLevel {
    /** Progress tracks / hairline chips. base = 1.5dp. */
    Micro,
    /** Tiny tags / badges. iOS base = 4dp. */
    Tag,
    /** Small chips / micro-buttons. iOS base = 6dp. */
    Chip,
    /** Input fields, search bars, small chip-like containers. iOS base = 10dp. */
    Field,
    /** Standard surface cards. iOS base = 12dp. */
    Card,
    /** Alert / confirm dialog containers. iOS base = 14dp. */
    Dialog,
    /** Bottom sheet / modal sheet (top-rounded). iOS base = 20dp. */
    Sheet,
    /** Floating elements — FABs, floating bars. iOS base = 28dp. */
    Floating,
    /** Pill / segmented selectors — radius comes from chrome tokens directly. */
    Pill
}

/**
 * 单值 Miuix 主题的形状 tokens。使用 [AppShapes.container] 而非手写 `RoundedCornerShape(N.dp)`。
 * 基础值按 Miuix 主题风格缩放；Pill 级直接取 chrome tokens，使胶囊控件保留原生曲率。
 */
object AppShapes {

    private fun baseDp(level: ContainerLevel): Float = when (level) {
        ContainerLevel.Micro -> 1.5f
        ContainerLevel.Tag -> 4f
        ContainerLevel.Chip -> 6f
        ContainerLevel.Field -> 10f
        ContainerLevel.Card -> 12f
        ContainerLevel.Dialog -> 14f
        ContainerLevel.Sheet -> 20f
        ContainerLevel.Floating -> 28f
        ContainerLevel.Pill -> 0f
    }

    fun resolveContainerCornerDp(
        level: ContainerLevel
    ): Dp {
        if (level == ContainerLevel.Pill) {
            return resolveAndroidNativeChromeTokens().pillCornerRadiusDp.dp
        }
        val scale = resolveCornerRadiusScale()
        return (baseDp(level) * scale).dp
    }

    fun resolveContainerShape(
        level: ContainerLevel
    ): Shape {
        val dp = resolveContainerCornerDp(level)
        return if (level == ContainerLevel.Sheet) {
            topRounded(dp)
        } else {
            RoundedCornerShape(dp)
        }
    }

    /**
     * Shape for containers that draw a stroke via [androidx.compose.foundation.BorderStroke] or
     * [androidx.compose.foundation.border]. Material3 borders follow [RoundedCornerShape]
     * reliably; iOS continuous corners render as chamfered edges.
     */
    fun resolveBorderedContainerShape(
        level: ContainerLevel
    ): Shape {
        val dp = resolveContainerCornerDp(level)
        return if (level == ContainerLevel.Sheet) {
            topRounded(dp)
        } else {
            RoundedCornerShape(dp)
        }
    }

    /** Top corners only (sheet / stacked cover plate). Bottom stays square. */
    fun topRounded(radius: Dp): Shape =
        RoundedCornerShape(topStart = radius, topEnd = radius)

    /** Bottom corners only (stacked info plate under cover). Top stays square. */
    fun bottomRounded(radius: Dp): Shape =
        RoundedCornerShape(bottomStart = radius, bottomEnd = radius)

    /** Leading vertical edge rounded (e.g. mini-player stashed on the right). */
    fun startRounded(radius: Dp): Shape =
        RoundedCornerShape(topStart = radius, bottomStart = radius)

    /** Trailing vertical edge rounded (e.g. mini-player stashed on the left). */
    fun endRounded(radius: Dp): Shape =
        RoundedCornerShape(topEnd = radius, bottomEnd = radius)

    /** Single-corner badge (duration chip on cover). */
    fun topStartRounded(radius: Dp): Shape =
        RoundedCornerShape(topStart = radius)

    /** Diagonal corners (resize grip on mini-player). */
    fun diagonalTopStartBottomEnd(topStart: Dp, bottomEnd: Dp): Shape =
        RoundedCornerShape(topStart = topStart, bottomEnd = bottomEnd)

    /**
     * Chat / message bubble: large outer corners, small tail corner on the
     * near side of the speaker.
     */
    fun messageBubble(
        isOutgoing: Boolean,
        large: Dp,
        small: Dp,
    ): Shape = if (isOutgoing) {
        RoundedCornerShape(
            topStart = large,
            topEnd = large,
            bottomStart = large,
            bottomEnd = small,
        )
    } else {
        RoundedCornerShape(
            topStart = large,
            topEnd = large,
            bottomStart = small,
            bottomEnd = large,
        )
    }

    @Composable
    fun container(level: ContainerLevel): Shape = resolveContainerShape(
        level = level
    )

    @Composable
    fun borderedContainer(level: ContainerLevel): Shape = resolveBorderedContainerShape(
        level = level
    )

    @Composable
    fun containerCornerDp(level: ContainerLevel): Dp = resolveContainerCornerDp(
        level = level
    )

    /** Scale a semantic radius (e.g. long-press hint size multiplier). */
    @Composable
    fun scaledContainer(level: ContainerLevel, scale: Float): Shape {
        val base = containerCornerDp(level)
        return RoundedCornerShape(base * scale.coerceAtLeast(0.1f))
    }

    @Composable
    fun messageBubble(isOutgoing: Boolean): Shape = messageBubble(
        isOutgoing = isOutgoing,
        large = containerCornerDp(ContainerLevel.Card),
        small = containerCornerDp(ContainerLevel.Tag),
    )
}
