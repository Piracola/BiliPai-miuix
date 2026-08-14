package com.android.purebilibili.core.ui

enum class AdaptiveTooltipRenderer {
    MIUIX_TOOLTIP_BOX
}

fun resolveAdaptiveTooltipRenderer(): AdaptiveTooltipRenderer =
    AdaptiveTooltipRenderer.MIUIX_TOOLTIP_BOX
