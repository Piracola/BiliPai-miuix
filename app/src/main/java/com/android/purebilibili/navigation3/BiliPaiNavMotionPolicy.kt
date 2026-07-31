package com.android.purebilibili.navigation3

import com.android.purebilibili.feature.settings.resolveSettingsNavPopTransition
import com.android.purebilibili.navigation.AppSystemBackAction
import com.android.purebilibili.navigation.shouldInterceptSystemBackForAppAction

internal enum class BiliPaiNavMotionMode {
    CARD_DISABLED,
    CLASSIC_CARD
}

internal enum class BiliPaiNavRouteTransition {
    NO_OP_SHARED_ELEMENT,
    REDUCED_MOTION_FADE,
    CARD_DISABLED_VIDEO_FORWARD_FROM_LEFT,
    CARD_DISABLED_VIDEO_FORWARD_FROM_RIGHT,
    CARD_DISABLED_VIDEO_RETURN_TO_LEFT,
    CARD_DISABLED_VIDEO_RETURN_TO_RIGHT,
    SPACE_FORWARD,
    LIGHT_SIBLING_FORWARD,
    LIGHT_SIBLING_POP,
    BOTTOM_BAR_SIBLING_FORWARD,
    BOTTOM_BAR_SIBLING_POP,
    SETTINGS_IOS_PUSH_FORWARD,
    SETTINGS_IOS_PUSH_POP,
    CLASSIC_CARD,
    FALLBACK
}

internal data class BiliPaiNavMotionDecision(
    val mode: BiliPaiNavMotionMode,
    val routeTransition: BiliPaiNavRouteTransition,
    val interceptSystemBack: Boolean
)

internal data class BiliPaiBackGestureDecision(
    val routeTransition: BiliPaiNavRouteTransition,
    val interceptSystemBack: Boolean
)

internal fun resolveBiliPaiNavMotionMode(
    cardTransitionEnabled: Boolean
): BiliPaiNavMotionMode {
    return if (cardTransitionEnabled) {
        BiliPaiNavMotionMode.CLASSIC_CARD
    } else {
        BiliPaiNavMotionMode.CARD_DISABLED
    }
}

internal fun resolveBiliPaiNavMotionDecision(
    fromKey: BiliPaiNavKey?,
    toKey: BiliPaiNavKey?,
    cardTransitionEnabled: Boolean,
    sharedTransitionReady: Boolean,
    appBackActionRequiresInterception: Boolean = false
): BiliPaiNavMotionDecision {
    val mode = resolveBiliPaiNavMotionMode(cardTransitionEnabled = cardTransitionEnabled)

    return BiliPaiNavMotionDecision(
        mode = mode,
        routeTransition = BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT,
        interceptSystemBack = shouldInterceptSystemBackForNavigation3(
            mode = mode,
            appBackActionRequiresInterception = appBackActionRequiresInterception
        )
    )
}

internal fun resolveBiliPaiBackGestureDecision(
    cardTransitionEnabled: Boolean,
    systemBackAction: AppSystemBackAction,
    currentKey: BiliPaiNavKey?,
    previousKey: BiliPaiNavKey?,
    sourceMetadata: BiliPaiNavSourceMetadata,
    activeMainHostRoute: String? = null,
): BiliPaiBackGestureDecision {
    return BiliPaiBackGestureDecision(
        routeTransition = BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT,
        interceptSystemBack = shouldInterceptSystemBackForAppAction(systemBackAction)
    )
}

internal fun shouldBindVideoDetailBackPreviewPlayer(
    currentKey: BiliPaiNavKey?,
    previewKey: BiliPaiNavKey?
): Boolean {
    return previewKey is BiliPaiNavKey.VideoDetail && currentKey !is BiliPaiNavKey.VideoDetail
}

internal fun shouldActivateVideoDetailPlaybackSession(
    currentKey: BiliPaiNavKey?,
    detailKey: BiliPaiNavKey.VideoDetail,
    isImmediateBackPreview: Boolean
): Boolean {
    return currentKey == detailKey ||
        (isImmediateBackPreview && currentKey !is BiliPaiNavKey.VideoDetail)
}

internal fun shouldRecoverVideoPlayerAfterBackCancellation(
    currentKey: BiliPaiNavKey?,
    targetKey: BiliPaiNavKey?
): Boolean {
    return currentKey is BiliPaiNavKey.VideoDetail && targetKey is BiliPaiNavKey.VideoDetail
}

/**
 * 解析 [BiliPaiNavDisplayHost] 全局 `popTransitionSpec` / `predictivePopTransitionSpec` 使用的过渡。
 *
 * 实际生效场景：
 *   - **预测式返回手势**（Android 13+ swipe-back）：entry metadata 不注入 PREDICTIVE_POP_TRANSITION_SPEC，
 *     所以此函数的输出是唯一来源；
 *   - 普通 pop：entry metadata 会注入 POP_TRANSITION_SPEC 并优先生效（见
 *     [resolveBiliPaiNavEntryPopRouteTransition]），此函数仅作兜底。
 *
 * 两条路径需要保持视觉一致——任何对此函数的逻辑修改都应同步检查
 * [resolveBiliPaiNavEntryPopRouteTransition]，反之亦然。
 */
internal fun resolveBiliPaiNavDisplayPopRouteTransition(
    cardTransitionEnabled: Boolean = true,
    sourceMetadata: BiliPaiNavSourceMetadata,
    fromKey: BiliPaiNavKey?,
    toKey: BiliPaiNavKey?,
    activeMainHostRoute: String? = null,
): BiliPaiNavRouteTransition {
    return BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT
}

internal fun isRelatedVideoDetailEntry(
    key: BiliPaiNavKey,
    sourceMetadata: BiliPaiNavSourceMetadata
): Boolean {
    val videoKey = key as? BiliPaiNavKey.VideoDetail ?: return false
    val sourceRoute = sourceMetadata.sourceRoute?.substringBefore("?") ?: return false
    return sourceRoute.startsWith("video/") &&
        videoKey.sourceRoute?.substringBefore("?") == sourceRoute &&
        sourceMetadata.sourceKey == "$sourceRoute:${videoKey.bvid}"
}

internal fun isRelatedVideoDetailReturn(
    fromKey: BiliPaiNavKey.VideoDetail?,
    toKey: BiliPaiNavKey?
): Boolean {
    val targetKey = toKey as? BiliPaiNavKey.VideoDetail ?: return false
    return fromKey?.sourceRoute?.substringBefore("?") == "video/${targetKey.bvid}"
}

internal fun shouldInterceptSystemBackForNavigation3(
    mode: BiliPaiNavMotionMode,
    appBackActionRequiresInterception: Boolean
): Boolean {
    return appBackActionRequiresInterception
}

internal fun resolveCardDisabledReturnTransition(
    sourceDirection: BiliPaiNavCardSourceDirection
): BiliPaiNavRouteTransition = BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT
