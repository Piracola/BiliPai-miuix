// 文件路径: core/store/gesture/TwoFingerSpeedTogglePolicy.kt
package com.android.purebilibili.core.store.gesture

/**
 * 阶段 6 前置：从 feature/video/ui/gesture/TwoFingerSpeedGesturePolicy.kt
 * 抽出的双指倍速开关纯状态与切换函数（零 feature 依赖），供设置存储层使用。
 */
data class TwoFingerSpeedToggleState(
    val verticalEnabled: Boolean = false,
    val horizontalEnabled: Boolean = false
)

fun applyVerticalTwoFingerSpeedToggle(
    current: TwoFingerSpeedToggleState,
    enabled: Boolean
): TwoFingerSpeedToggleState {
    return if (enabled) {
        TwoFingerSpeedToggleState(
            verticalEnabled = true,
            horizontalEnabled = false
        )
    } else {
        current.copy(verticalEnabled = false)
    }
}

fun applyHorizontalTwoFingerSpeedToggle(
    current: TwoFingerSpeedToggleState,
    enabled: Boolean
): TwoFingerSpeedToggleState {
    return if (enabled) {
        TwoFingerSpeedToggleState(
            verticalEnabled = false,
            horizontalEnabled = true
        )
    } else {
        current.copy(horizontalEnabled = false)
    }
}
