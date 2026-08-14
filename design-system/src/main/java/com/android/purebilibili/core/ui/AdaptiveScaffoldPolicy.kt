package com.android.purebilibili.core.ui

enum class AdaptiveScaffoldRenderer {
    MIUIX_SCAFFOLD_WITH_POPUP_HOST
}

fun resolveAdaptiveScaffoldRenderer(): AdaptiveScaffoldRenderer =
    AdaptiveScaffoldRenderer.MIUIX_SCAFFOLD_WITH_POPUP_HOST

fun shouldMountMiuixPopupHostOnAdaptiveScaffold(): Boolean = true
