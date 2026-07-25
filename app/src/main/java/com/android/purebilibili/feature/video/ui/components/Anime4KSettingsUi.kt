package com.android.purebilibili.feature.video.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.purebilibili.feature.anime4k.Anime4KBypassReason
import com.android.purebilibili.feature.anime4k.Anime4KPreset
import com.android.purebilibili.feature.anime4k.resolveAnime4KPresetLabel

@Composable
internal fun Anime4KPresetOptions(
    preset: Anime4KPreset,
    onPresetChange: (Anime4KPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        Anime4KPreset.FAST,
        Anime4KPreset.QUALITY
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { value ->
            FilterChip(
                selected = preset == value,
                onClick = { onPresetChange(value) },
                label = { Text(resolveAnime4KPresetLabel(value)) }
            )
        }
    }
}

internal fun resolveAnime4KSettingsSubtitle(
    enabled: Boolean,
    available: Boolean,
    bypassReason: Anime4KBypassReason
): String {
    if (!available) return "当前设备不支持 OpenGL ES 3.0"
    if (!enabled) return "关闭"
    return when (bypassReason) {
        Anime4KBypassReason.NONE -> "已启用"
        Anime4KBypassReason.HDR_OR_DOLBY_VISION -> "HDR / 杜比视界使用原始输出"
        Anime4KBypassReason.PICTURE_IN_PICTURE -> "小窗模式使用原始输出"
        Anime4KBypassReason.AUDIO_ONLY -> "音频模式使用原始输出"
        Anime4KBypassReason.HOST_NOT_STARTED -> "后台时使用原始输出"
        Anime4KBypassReason.GL_UNAVAILABLE -> "渲染管线不可用"
        Anime4KBypassReason.DISABLED -> "关闭"
    }
}
