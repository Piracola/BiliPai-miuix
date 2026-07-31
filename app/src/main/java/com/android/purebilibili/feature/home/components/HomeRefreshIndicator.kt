// 文件路径: feature/home/components/HomeRefreshIndicator.kt
package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.ui.AppPullRefreshLoadingIndicator
import com.android.purebilibili.core.ui.AdaptiveLoadingIndicator
import com.android.purebilibili.feature.home.resolvePullRefreshHintText

/**
 * Renderer kind for [HomeRefreshIndicator]. iOS keeps its Cupertino spinner;
 * MD3 uses the official loading indicator;
 * the shared App indicator chooses the native loading control for each style
 * when this composable is still mounted (home Miuix uses native pull-to-refresh).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun Md3ScreenshotRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    indicatorHeight: Dp,
    modifier: Modifier = Modifier
) {
    val progress = state.distanceFraction
    val hintText = resolvePullRefreshHintText(
        progress = progress,
        isRefreshing = isRefreshing,
        isStateAnimating = state.isAnimating
    )
    val alpha = if (progress > 0.08f || isRefreshing) 1f else 0f
    val indicatorScale = 1f
    val strokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                scaleX = indicatorScale
                scaleY = indicatorScale
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(top = AppSpacingTokens.Small + AppSpacingTokens.Micro, bottom = AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isRefreshing) {
                // Official M3 morphing LoadingIndicator (dynamic primary).
                AdaptiveLoadingIndicator(
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = AppSpacingTokens.ExtraLarge + AppSpacingTokens.Micro, height = indicatorHeight)
                        .clip(AppShapes.container(ContainerLevel.Pill))
                        .background(Color.Transparent)
                        .border(
                            width = AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2,
                            color = strokeColor,
                            shape = AppShapes.container(ContainerLevel.Pill)
                        )
                )
            }

            if (hintText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacingTokens.Small + AppSpacingTokens.Micro))
                AppText(
                    text = if (hintText == "松手刷新") "松开刷新" else hintText,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

/**
 *  iOS 风格下拉刷新指示器
 * 
 * 特点：
 * - 下拉时显示"下拉刷新..."
 * - 达到阈值时显示"松手刷新"  
 * - 刷新中显示加载指示器
 * - 刷新完成显示"刷新成功"
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    //  进度值（0.0 ~ 1.0+）
    val progress = state.distanceFraction
    
    //  是否达到刷新阈值
    val isOverThreshold = progress >= 1f && !state.isAnimating
    
    //  提示文字
    val hintText = resolvePullRefreshHintText(
        progress = progress,
        isRefreshing = isRefreshing,
        isStateAnimating = state.isAnimating
    )
    
    //  箭头仅表达阈值状态，状态变化时立即切换。
    val arrowRotation = if (isOverThreshold) 180f else 0f
    
    //  透明度直接跟随可见状态。
    val alpha = if (progress > 0.1f || isRefreshing) 1f else 0f
    
    //  固定尺寸，避免手势过程中的缩放。
    val scale = 1f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacingTokens.Medium)
        ) {
            if (isRefreshing) {
                AppPullRefreshLoadingIndicator()
            } else if (progress > 0.1f) {
                //  箭头图标（旋转表示状态变化）
                AppText(
                    text = "↓",
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }
            
            if (hintText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                
                AppText(
                    text = hintText,
                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
