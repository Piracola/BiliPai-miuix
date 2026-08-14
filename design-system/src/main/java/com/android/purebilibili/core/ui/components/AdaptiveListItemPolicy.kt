package com.android.purebilibili.core.ui.components

enum class AppClickableItemRenderer {
    CUPERTINO,
    MIUIX_ARROW,
    MIUIX_BASIC,
}

fun resolveAppClickableItemRenderer(
    onClick: (() -> Unit)?,
    showChevron: Boolean,
    centered: Boolean,
): AppClickableItemRenderer = when {
    centered -> AppClickableItemRenderer.CUPERTINO
    onClick != null && showChevron -> AppClickableItemRenderer.MIUIX_ARROW
    else -> AppClickableItemRenderer.MIUIX_BASIC
}

fun shouldRouteClickableItemToMiuixArrowPreference(
    onClick: (() -> Unit)?,
    showChevron: Boolean,
    centered: Boolean,
): Boolean = resolveAppClickableItemRenderer(
    onClick = onClick,
    showChevron = showChevron,
    centered = centered
) == AppClickableItemRenderer.MIUIX_ARROW

fun shouldRouteSwitchItemToMiuixSwitchPreference(): Boolean = true

fun shouldRouteSliderPreferenceToMiuixSliderPreference(): Boolean = true
