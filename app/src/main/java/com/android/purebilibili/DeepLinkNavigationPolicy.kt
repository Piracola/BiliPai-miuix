// 文件路径: app/src/main/java/com/android/purebilibili/DeepLinkNavigationPolicy.kt
package com.android.purebilibili

import com.android.purebilibili.core.util.BilibiliNavigationTarget
import com.android.purebilibili.core.util.BilibiliUrlParser
import com.android.purebilibili.navigation.ScreenRoutes
import com.android.purebilibili.navigation.VideoRoute
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val PLUGIN_INSTALL_HTTPS_HOSTS = setOf(
    "bilipai.app",
    "www.bilipai.app",
    "plugins.bilipai.app"
)

/**
 * 阶段 5 拆分：MainActivity 的深链/路由解析纯函数。
 * 全部为 internal 顶层函数，与 MainActivity 状态解耦，可直接单元测试。
 */
internal fun resolveShortcutRoute(host: String): String? {
    return when (host) {
        "search" -> ScreenRoutes.Search.route
        "dynamic" -> ScreenRoutes.Dynamic.route
        "favorite" -> ScreenRoutes.Favorite.route
        "history" -> ScreenRoutes.History.route
        "login" -> ScreenRoutes.Login.route
        "playback" -> ScreenRoutes.PlaybackSettings.route
        "plugins" -> ScreenRoutes.PluginsSettings.createRoute()
        else -> null
    }
}

internal data class PluginInstallDeepLinkRequest(
    val pluginUrl: String
)

internal fun resolvePluginInstallDeepLink(rawDeepLink: String): PluginInstallDeepLinkRequest? {
    val uri = runCatching { URI(rawDeepLink) }.getOrNull() ?: return null
    val normalizedScheme = uri.scheme?.lowercase() ?: return null
    val normalizedHost = uri.host?.lowercase() ?: return null
    val normalizedPath = uri.path?.trim()?.trimEnd('/') ?: ""

    val installLinkMatched = when (normalizedScheme) {
        "bilipai" -> normalizedHost == "plugin" && normalizedPath == "/install"
        "https", "http" -> normalizedHost in PLUGIN_INSTALL_HTTPS_HOSTS &&
            normalizedPath in setOf("/plugin/install", "/plugins/install")
        else -> false
    }
    if (!installLinkMatched) return null

    val queryMap = uri.rawQuery
        ?.split("&")
        ?.mapNotNull { part ->
            if (part.isBlank()) return@mapNotNull null
            val pair = part.split("=", limit = 2)
            val key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8.name())
            val value = URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8.name())
            key to value
        }
        ?.toMap()
        ?: emptyMap()

    val rawUrl = queryMap["url"]?.trim().orEmpty()
    if (rawUrl.isBlank()) return null

    val targetUri = runCatching { URI(rawUrl) }.getOrNull() ?: return null
    val scheme = targetUri.scheme?.lowercase()
    if (scheme !in listOf("http", "https") || targetUri.host.isNullOrBlank()) {
        return null
    }
    return PluginInstallDeepLinkRequest(pluginUrl = rawUrl)
}

internal fun shouldNavigateToVideoFromNotification(
    currentRoute: String?,
    currentBvid: String?,
    targetBvid: String
): Boolean {
    val isInVideoRoute = currentRoute?.substringBefore("/") == VideoRoute.base
    return !(isInVideoRoute && currentBvid == targetBvid)
}

internal fun resolveMainActivityVideoRoute(
    bvid: String,
    cid: Long,
    startFullscreen: Boolean = false
): String {
    return VideoRoute.resolveVideoRoutePath(
        bvid = bvid,
        cid = cid,
        encodedCover = "",
        startAudio = false,
        autoPortrait = true,
        fullscreen = startFullscreen,
        resumePositionMs = 0L
    )
}

internal fun resolveMiniPlayerExpandVideoRoute(
    bvid: String,
    cid: Long
): String {
    return resolveMainActivityVideoRoute(
        bvid = bvid,
        cid = cid,
        startFullscreen = true
    )
}

internal fun resolveMainActivityDynamicRoute(dynamicId: String): String {
    val encodedDynamicId = URLEncoder.encode(dynamicId, StandardCharsets.UTF_8.toString())
    return "dynamic_detail/$encodedDynamicId"
}

internal fun resolveIntentLinkFallbackRoute(rawInput: String): String? {
    val fallbackUrl = resolveIntentLinkFallbackUrl(rawInput) ?: return null
    return ScreenRoutes.Web.createRoute(fallbackUrl)
}

internal fun resolveIntentLinkFallbackUrl(rawInput: String): String? {
    val directCandidate = normalizeIntentLinkWebCandidate(rawInput)
    if (directCandidate != null) return directCandidate

    return BilibiliUrlParser.extractUrls(rawInput)
        .firstNotNullOfOrNull(::normalizeIntentLinkWebCandidate)
}

private fun normalizeIntentLinkWebCandidate(rawInput: String): String? {
    val trimmed = rawInput.trim()
    if (trimmed.isBlank()) return null
    val candidate = when {
        trimmed.startsWith("//") -> "https:$trimmed"
        "://" in trimmed -> trimmed
        trimmed.startsWith("b23.tv/", ignoreCase = true) -> "https://$trimmed"
        trimmed.startsWith("www.bilibili.com/", ignoreCase = true) -> "https://$trimmed"
        trimmed.startsWith("m.bilibili.com/", ignoreCase = true) -> "https://$trimmed"
        trimmed.startsWith("bilibili.com/", ignoreCase = true) -> "https://$trimmed"
        else -> return null
    }
    val uri = runCatching { URI(candidate) }.getOrNull() ?: return null
    val scheme = uri.scheme?.lowercase().orEmpty()
    if (scheme !in setOf("http", "https")) return null
    val host = uri.host?.lowercase().orEmpty()
    if (!host.contains("b23.tv") && !host.contains("bilibili.com")) return null
    return candidate
}

internal data class MainActivityLinkNavigation(
    val pendingVideoId: String? = null,
    val pendingNavigationRoute: String? = null,
    val pendingSearchKeyword: String? = null
)

internal fun resolveMainActivityLinkNavigation(
    target: BilibiliNavigationTarget
): MainActivityLinkNavigation? {
    return when (target) {
        is BilibiliNavigationTarget.Video -> MainActivityLinkNavigation(
            pendingVideoId = target.videoId
        )

        is BilibiliNavigationTarget.Dynamic -> MainActivityLinkNavigation(
            pendingNavigationRoute = resolveMainActivityDynamicRoute(target.dynamicId)
        )

        is BilibiliNavigationTarget.Search -> MainActivityLinkNavigation(
            pendingNavigationRoute = ScreenRoutes.Search.route,
            pendingSearchKeyword = target.keyword
        )

        is BilibiliNavigationTarget.Space -> MainActivityLinkNavigation(
            pendingNavigationRoute = ScreenRoutes.Space.createRoute(target.mid)
        )

        is BilibiliNavigationTarget.Live -> MainActivityLinkNavigation(
            pendingNavigationRoute = ScreenRoutes.Live.createRoute(
                roomId = target.roomId,
                title = "",
                uname = ""
            )
        )

        is BilibiliNavigationTarget.BangumiSeason -> MainActivityLinkNavigation(
            pendingNavigationRoute = ScreenRoutes.BangumiDetail.createRoute(
                seasonId = target.seasonId
            )
        )

        is BilibiliNavigationTarget.BangumiEpisode -> MainActivityLinkNavigation(
            pendingNavigationRoute = ScreenRoutes.BangumiDetail.createRoute(
                seasonId = 0,
                epId = target.epId
            )
        )

        is BilibiliNavigationTarget.Article -> MainActivityLinkNavigation(
            pendingNavigationRoute = ScreenRoutes.ArticleDetail.createRoute(target.articleId)
        )

        is BilibiliNavigationTarget.Music -> {
            val auSid = target.musicId.removePrefix("au").removePrefix("AU").toLongOrNull() ?: return null
            MainActivityLinkNavigation(
                pendingNavigationRoute = ScreenRoutes.Web.createRoute(
                    url = "https://www.bilibili.com/audio/au$auSid"
                )
            )
        }
    }
}
