package com.android.purebilibili.feature.home.components.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 源卡信息区（标题/UP 等）在 shell morph 时的 chrome 视觉。
 * 封面保持可见；返回末段按景深进度淡入字，避免叠实时画面又落后封面；
 * 横卡可选择随主进度短距离移动；快速返回不藏字。
 * 所有进度都在绘制阶段读取，避免整卡重组。
 */
@Composable
@Suppress("UNUSED_PARAMETER")
internal fun Modifier.videoCardShellReturnChromeAlpha(
    enabled: Boolean,
    bvid: String,
    sourceRoute: String?,
    isReturningFromDetail: Boolean = false,
    isQuickReturnFromDetail: Boolean = false,
    followShellMotion: Boolean = false,
): Modifier {
    return this
}
