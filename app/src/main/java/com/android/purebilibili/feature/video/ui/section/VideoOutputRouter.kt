package com.android.purebilibili.feature.video.ui.section

import androidx.media3.common.Player
import androidx.media3.ui.PlayerView

/**
 * 将播放器的唯一视频输出绑定集中到这里。
 * 直出模式保留 PlayerView 原有行为。
 */
internal class VideoOutputRouter(
    private val player: Player
) {
    private var directPlayerView: PlayerView? = null
    private var shouldBindDirectPlayerView = false

    fun update(
        playerView: PlayerView?,
        shouldBindDirectPlayerView: Boolean
    ) {
        directPlayerView = playerView
        this.shouldBindDirectPlayerView = shouldBindDirectPlayerView
    }

    fun rebindDirectSurfaceIfNeeded() {
        if (shouldBindDirectPlayerView) {
            directPlayerView?.let { rebindPlayerSurfaceIfNeeded(it, player) }
        }
    }

    fun release() {
        directPlayerView = null
    }
}
