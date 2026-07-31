package com.android.purebilibili.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.purebilibili.core.theme.iOSBlue
import com.android.purebilibili.core.theme.iOSGreen
import com.android.purebilibili.core.theme.iOSSystemGray
import com.android.purebilibili.core.ui.components.AppCheckbox
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.util.CacheClearTarget

data class CacheClearProgress(
    val current: Long,
    val total: Long,
    val isComplete: Boolean = false,
    val clearedSize: String = ""
)

private object CacheClearColors {
    val progress = iOSBlue
    val success = iOSGreen
}

/**
 * 缓存清理确认对话框。选项保持可点，清理中的视觉变化统一放到真实进度上。
 */
@Composable
internal fun CacheClearConfirmDialog(
    selectedCacheSizeSummary: String,
    options: List<CacheClearOptionUiModel>,
    selectedTargets: Set<CacheClearTarget>,
    onTargetToggle: (CacheClearTarget, Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    com.android.purebilibili.core.ui.AppAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText(
                "清除缓存",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                AppText(
                    resolveCacheClearConfirmationMessage(selectedTargets),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppText(
                    selectedCacheSizeSummary,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onTargetToggle(option.target, option.target !in selectedTargets)
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        AppCheckbox(
                            checked = option.target in selectedTargets,
                            onCheckedChange = { checked -> onTargetToggle(option.target, checked) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            AppText(
                                option.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            AppText(
                                option.description,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            com.android.purebilibili.core.ui.AppDialogAction(onClick = onConfirm) {
                AppText(
                    "确认清除",
                    color = if (selectedTargets.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        },
        dismissButton = {
            com.android.purebilibili.core.ui.AppDialogAction(onClick = onDismiss) {
                AppText("取消", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

/**
 * 显示实际缓存清理进度。这里刻意不用补间、粒子或自动关闭计时器，进度值每次更新都直接呈现。
 */
@Composable
fun CacheClearAnimationDialog(
    progress: CacheClearProgress,
    onDismiss: () -> Unit
) {
    val progressValue = if (progress.total > 0L) {
        (progress.current.toFloat() / progress.total.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Dialog(
        onDismissRequest = {
            if (progress.isComplete) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = progress.isComplete,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.52f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(48.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CacheClearProgressRing(
                    progress = progressValue,
                    isComplete = progress.isComplete
                )

                AppText(
                    text = if (progress.isComplete) "清理完成" else "正在清理",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (progress.isComplete) {
                        CacheClearColors.success
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                AppText(
                    text = when {
                        progress.clearedSize.isNotEmpty() && progress.isComplete -> "共释放 ${progress.clearedSize}"
                        progress.clearedSize.isNotEmpty() -> "已清理 ${progress.clearedSize}"
                        else -> "准备中..."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!progress.isComplete && progressValue > 0f) {
                    AppText(
                        text = "${(progressValue * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = CacheClearColors.progress
                    )
                }

                if (progress.isComplete) {
                    TextButton(onClick = onDismiss) {
                        AppText("关闭")
                    }
                }
            }
        }
    }
}

@Composable
private fun CacheClearProgressRing(
    progress: Float,
    isComplete: Boolean,
    size: Dp = 160.dp,
    strokeWidth: Dp = 8.dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = this.size.minDimension
        val stroke = strokeWidth.toPx()
        val radius = (canvasSize / 2f) - stroke
        val center = Offset(canvasSize / 2f, canvasSize / 2f)
        val color = if (isComplete) CacheClearColors.success else CacheClearColors.progress

        drawCircle(
            color = iOSSystemGray.copy(alpha = 0.18f),
            radius = radius,
            center = center,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = (if (isComplete) 1f else progress) * 360f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
