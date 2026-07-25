package com.android.purebilibili.feature.video.ui.section

import android.view.Surface
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView

/**
 * 将播放器的唯一视频输出绑定集中到这里。
 * 直出模式保留 PlayerView 原有行为；Anime4K 只在输入 surface 就绪后接管输出。
 */
internal class VideoOutputRouter(
    private val player: Player
) {
    private var directPlayerView: PlayerView? = null
    private var anime4kInputSurface: Surface? = null
    private var boundAnime4kSurface: Surface? = null
    private var shouldBindDirectPlayerView = false
    private var shouldUseAnime4K = false
    private var usingAnime4K = false

    fun update(
        playerView: PlayerView?,
        inputSurface: Surface?,
        shouldBindDirectPlayerView: Boolean,
        shouldUseAnime4K: Boolean
    ) {
        directPlayerView = playerView
        anime4kInputSurface = inputSurface
        this.shouldBindDirectPlayerView = shouldBindDirectPlayerView
        this.shouldUseAnime4K = shouldUseAnime4K
        applyRoute()
    }

    fun rebindDirectSurfaceIfNeeded() {
        if (!usingAnime4K && shouldBindDirectPlayerView) {
            directPlayerView?.let { rebindPlayerSurfaceIfNeeded(it, player) }
        }
    }

    fun release() {
        if (usingAnime4K) player.clearVideoSurface()
        usingAnime4K = false
        anime4kInputSurface = null
        boundAnime4kSurface = null
        directPlayerView?.takeIf { it.player === player }?.player = null
        directPlayerView = null
    }

    private fun applyRoute() {
        val inputSurface = anime4kInputSurface
        if (shouldUseAnime4K && inputSurface != null) {
            if (usingAnime4K && boundAnime4kSurface === inputSurface) return
            directPlayerView?.takeIf { it.player === player }?.player = null
            if (usingAnime4K) player.clearVideoSurface()
            player.setVideoSurface(inputSurface)
            usingAnime4K = true
            boundAnime4kSurface = inputSurface
            return
        }

        val wasUsingAnime4K = usingAnime4K
        if (usingAnime4K) {
            player.clearVideoSurface()
            usingAnime4K = false
            boundAnime4kSurface = null
        }
        val view = directPlayerView ?: return
        if (shouldBindDirectPlayerView) {
            if (wasUsingAnime4K) {
                // clearVideoSurface() 会清掉同一帧内 PlayerView 刚绑定的 Surface，必须显式重绑。
                rebindPlayerSurfaceIfNeeded(view, player)
            } else if (view.player !== player) {
                view.player = player
            }
        } else if (view.player === player) {
            view.player = null
        }
    }
}
