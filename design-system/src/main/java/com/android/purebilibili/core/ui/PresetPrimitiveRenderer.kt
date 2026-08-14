package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Canonical decision for which preset's renderer a shared adaptive primitive
 * should dispatch to. The runtime only supports the Miuix style, so every
 * preset-aware primitive resolves to [MIUIX_BRIDGED].
 */
enum class PresetPrimitiveRenderer {
    /** Miuix native components. MIUIX style. */
    MIUIX_BRIDGED
}

fun resolvePresetPrimitiveRenderer(): PresetPrimitiveRenderer =
    PresetPrimitiveRenderer.MIUIX_BRIDGED

@Composable
@ReadOnlyComposable
fun rememberPresetPrimitiveRenderer(): PresetPrimitiveRenderer =
    PresetPrimitiveRenderer.MIUIX_BRIDGED
