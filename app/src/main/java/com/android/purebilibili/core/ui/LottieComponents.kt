// 文件路径: core/ui/LottieComponents.kt
package com.android.purebilibili.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 *  加载动画组件（按 UI 预设分发：MD3 LoadingIndicator / Miuix 进度环）
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    text: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AdaptiveLoadingIndicator(
            size = size,
            strokeWidth = 2.4.dp,
        )
        if (text != null) {
            Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Theme-aware loading indicator entry used across feature screens.
 *
 * Routes through [AdaptiveLoadingIndicator]: MD3 uses the official morphing
 * [androidx.compose.material3.LoadingIndicator] and Miuix uses native progress chrome.
 *
 * @param size optional visual size. Prefer this over [Modifier.size] so compact
 *   slots (≤ 32.dp) can select the compact circular recipe on MD3/Miuix.
 */
@Composable
fun CutePersonLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    strokeWidth: Dp = 2.dp,
    size: Dp? = null,
) {
    AdaptiveLoadingIndicator(
        modifier = modifier,
        size = size,
        color = color,
        strokeWidth = strokeWidth,
    )
}

/**
 *  空状态组件
 */
@Composable
fun EmptyState(
    message: String = "暂无内容",
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacingTokens.DoubleExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(AppSpacingTokens.Large))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

/**
 *  错误状态组件
 */
@Composable
fun ErrorState(
    message: String = "加载失败",
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacingTokens.DoubleExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(AppSpacingTokens.Large))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
            Text(
                text = "点击重试",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onRetry() }
            )
        }
    }
}
