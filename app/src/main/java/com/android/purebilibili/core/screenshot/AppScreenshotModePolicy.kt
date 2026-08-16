// 文件路径: core/screenshot/AppScreenshotModePolicy.kt
package com.android.purebilibili.core.screenshot

/**
 * 阶段 6 前置：从 feature/screenshot 抽出的截图模式枚举（纯值对象，
 * 零 feature 依赖），供设置存储层与截图功能共用。
 */
enum class AppScreenshotCaptureMode(val value: Int, val label: String, val description: String) {
    FULL_WINDOW(
        value = 0,
        label = "全屏",
        description = "直接保存当前 BiliPai 窗口"
    ),
    SELECT_REGION(
        value = 1,
        label = "手选区域",
        description = "先冻结预览，再拖拽选择保存区域"
    );

    companion object {
        fun fromValue(value: Int): AppScreenshotCaptureMode =
            entries.find { it.value == value } ?: FULL_WINDOW
    }
}

enum class AppScreenshotGestureMode(val value: Int, val label: String, val description: String) {
    TOP_RIGHT_TWO_FINGER_LONG_PRESS(
        value = 0,
        label = "右上角双指长按",
        description = "双指按住右上角约 0.6 秒，避免和系统三指截图冲突"
    ),
    THREE_FINGER_SWIPE_DOWN(
        value = 1,
        label = "三指下滑",
        description = "可能与部分国产系统截图手势冲突"
    ),
    DISABLED(
        value = 2,
        label = "关闭",
        description = "关闭手势触发，仅保留设置开关"
    );

    companion object {
        fun fromValue(value: Int): AppScreenshotGestureMode =
            entries.find { it.value == value } ?: TOP_RIGHT_TWO_FINGER_LONG_PRESS
    }
}
