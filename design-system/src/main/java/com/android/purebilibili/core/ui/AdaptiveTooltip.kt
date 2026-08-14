package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.TooltipAnchorPosition
import top.yukonga.miuix.kmp.basic.TooltipBox as MiuixTooltipBox

/**
 * Long-press / hover tooltip bridge.
 *
 * Uses the official [MiuixTooltipBox] plain-text convenience API.
 */
@Composable
fun AdaptivePlainTooltipBox(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    MiuixTooltipBox(
        text = text,
        modifier = modifier,
        enabled = enabled && text.isNotBlank(),
        positioning = TooltipAnchorPosition.Below,
        content = content
    )
}
