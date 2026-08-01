package com.android.purebilibili.navigation3.predictiveback

internal fun resolveBiliPaiPredictiveBackExitDirection(
    storageValue: String?,
    autoDerived: BiliPaiPredictiveBackExitDirection,
): BiliPaiPredictiveBackExitDirection {
    return when (storageValue) {
        "follow_gesture" -> BiliPaiPredictiveBackExitDirection.FOLLOW_GESTURE
        "always_right" -> BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT
        "always_left" -> BiliPaiPredictiveBackExitDirection.ALWAYS_LEFT
        else -> autoDerived
    }
}
