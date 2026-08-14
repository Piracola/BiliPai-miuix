package com.android.purebilibili.core.ui

enum class AdaptiveSideNavigationRailRenderer {
    MIUIX
}

fun resolveAdaptiveSideNavigationRailRenderer(): AdaptiveSideNavigationRailRenderer =
    AdaptiveSideNavigationRailRenderer.MIUIX

fun shouldUseExpandableMiuixNavigationRail(isExpandedWidthClass: Boolean): Boolean =
    isExpandedWidthClass
