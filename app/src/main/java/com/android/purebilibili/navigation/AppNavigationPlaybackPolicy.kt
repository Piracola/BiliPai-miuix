package com.android.purebilibili.navigation

internal fun shouldStopPlaybackEagerlyOnVideoRouteExit(
    fromRoute: String?,
    toRoute: String?
): Boolean {
    if (toRoute.isNullOrBlank()) return false
    val toRouteBase = toRoute.substringBefore("?")
    return isVideoDetailRoute(fromRoute) &&
        !isVideoDetailRoute(toRouteBase) &&
        !toRouteBase.startsWith("space/")
}

internal fun shouldDeferBottomBarRevealOnVideoReturn(
    isReturningFromDetail: Boolean,
    activeBottomTabRoute: String?,
    cardTransitionEnabled: Boolean
): Boolean {
    return false
}

internal fun shouldDelayBottomBarRevealAfterVideoReturn(
    isReturningFromDetail: Boolean,
    isBottomBarDestination: Boolean,
    cardTransitionEnabled: Boolean
): Boolean {
    return isReturningFromDetail &&
        isBottomBarDestination &&
        cardTransitionEnabled
}

internal fun resolveVideoReturnBottomBarRevealDelayMs(
    cardTransitionEnabled: Boolean,
    isQuickReturnFromDetail: Boolean
): Long {
    if (!cardTransitionEnabled) return 0L
    return if (isQuickReturnFromDetail) 120L else 160L
}

internal fun shouldClearReturningStateWhenDisposingVideoDestination(
    stillInVideoRoute: Boolean
): Boolean {
    return stillInVideoRoute
}

internal fun isVideoCardReturnTargetRoute(route: String?): Boolean {
    val routeBase = route?.substringBefore("?") ?: return false
    return isVideoDetailRoute(routeBase) ||
        routeBase == "main_host" ||
        routeBase == ScreenRoutes.Home.route ||
        routeBase == ScreenRoutes.History.route ||
        routeBase == ScreenRoutes.Favorite.route ||
        routeBase == ScreenRoutes.LikedVideos.route ||
        routeBase == ScreenRoutes.WatchLater.route ||
        routeBase == ScreenRoutes.Search.route ||
        routeBase == ScreenRoutes.Dynamic.route ||
        routeBase.startsWith("dynamic_detail/") ||
        routeBase == ScreenRoutes.Partition.route ||
        routeBase.startsWith("category/") ||
        routeBase.startsWith("season_series_detail/") ||
        routeBase.startsWith("space/")
}

internal fun shouldMarkNavigationLeaveBeforeVideoExit(
    isMiniMode: Boolean
): Boolean = !isMiniMode

internal fun isVideoDetailRoute(route: String?): Boolean {
    return route?.startsWith("${VideoRoute.base}/") == true
}

internal fun shouldEnableVideoDetailSharedTransition(
    cardTransitionEnabled: Boolean
): Boolean {
    return cardTransitionEnabled
}
