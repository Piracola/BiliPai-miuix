package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.squircle.squircleBackground

@Composable
fun Modifier.adaptiveSquircleBackground(
    color: Color,
    cornerRadius: Dp
): Modifier {
    return squircleBackground(color = color, cornerRadius = cornerRadius)
}

internal fun shouldApplyMiuixSquircleBackground(): Boolean = true
