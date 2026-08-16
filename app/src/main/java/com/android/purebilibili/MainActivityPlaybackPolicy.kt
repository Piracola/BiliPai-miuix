// 文件路径: app/src/main/java/com/android/purebilibili/MainActivityPlaybackPolicy.kt
package com.android.purebilibili

/**
 * 阶段 5 拆分：MainActivity 的播放器/PiP 判定纯函数。
 * 与 Activity 生命周期解耦，可单元测试。
 */
internal fun shouldForceStopPlaybackOnUserLeaveHint(
    isInVideoDetail: Boolean,
    stopPlaybackOnExit: Boolean,
    shouldTriggerPip: Boolean
): Boolean {
    // `onUserLeaveHint()` also fires when the user temporarily switches apps.
    // "Stop playback when leaving the playback page" should only apply to
    // explicit in-app navigation, which is handled by dedicated navigation hooks.
    return false
}

internal fun shouldRestorePlaybackRouteStateOnResume(
    isPlaybackRouteActive: Boolean
): Boolean = isPlaybackRouteActive

internal fun shouldRestoreMutedPlaybackPlayerVolumeOnResume(
    playerVolume: Float
): Boolean = playerVolume <= 0f

internal fun isPlaybackRouteActive(
    isInVideoDetail: Boolean
): Boolean = isInVideoDetail

internal fun shouldTriggerPlaybackRoutePip(
    isInVideoDetail: Boolean,
    isInMiniMode: Boolean,
    shouldEnterPip: Boolean,
    isActuallyPlaying: Boolean
): Boolean {
    if (!shouldEnterPip || !isActuallyPlaying) return false
    return isInVideoDetail || isInMiniMode
}

internal data class MainActivityPlaybackOverlayState(
    val showMiniPlayerOverlay: Boolean,
    val showDedicatedPipPlayer: Boolean
)

internal fun resolveMainActivityPlaybackOverlayState(
    isInPipMode: Boolean,
    isMiniMode: Boolean
): MainActivityPlaybackOverlayState {
    return MainActivityPlaybackOverlayState(
        showMiniPlayerOverlay = !isInPipMode,
        // 从首页小窗进入系统 PiP 时，原详情页已销毁，需要独立渲染面承接同一个 Player。
        showDedicatedPipPlayer = isInPipMode && isMiniMode
    )
}
