// 文件路径: app/src/main/java/com/android/purebilibili/CrashLogPromptPolicy.kt
package com.android.purebilibili

/**
 * 阶段 5 拆分：崩溃日志提示判定纯函数。
 */
internal enum class CrashLogPromptAction {
    SHARE,
    DISMISS,
    IGNORE
}

internal fun shouldShowPendingCrashLogPrompt(
    hasPendingCrashSnapshot: Boolean,
    hasPromptBeenHandled: Boolean
): Boolean = hasPendingCrashSnapshot && !hasPromptBeenHandled

internal fun shouldClearPendingCrashLogAfterAction(
    action: CrashLogPromptAction
): Boolean = action != CrashLogPromptAction.IGNORE
