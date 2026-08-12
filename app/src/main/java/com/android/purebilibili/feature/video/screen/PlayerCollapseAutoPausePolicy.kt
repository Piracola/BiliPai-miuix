package com.android.purebilibili.feature.video.screen

/**
 * When the detail player is swipe-collapsed (e.g. browsing related), optionally auto-pause
 * so audio does not keep playing under the list.
 */
internal fun shouldAutoPauseOnPlayerCollapse(
    autoPauseEnabled: Boolean,
    isPlayerCollapsed: Boolean,
    isPlaying: Boolean,
): Boolean = autoPauseEnabled && isPlayerCollapsed && isPlaying

/**
 * Resume only if this feature previously auto-paused — never override a manual pause.
 */
internal fun shouldAutoResumeOnPlayerExpand(
    autoPauseEnabled: Boolean,
    isPlayerCollapsed: Boolean,
    wasAutoPausedByCollapse: Boolean,
): Boolean = autoPauseEnabled && !isPlayerCollapsed && wasAutoPausedByCollapse
