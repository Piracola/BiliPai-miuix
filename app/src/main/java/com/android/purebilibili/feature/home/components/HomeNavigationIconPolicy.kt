package com.android.purebilibili.feature.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.android.purebilibili.R
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.ContactsCircle
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.FolderFill
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.icon.extended.TopDownloads
import top.yukonga.miuix.kmp.icon.extended.WorldClock

private enum class HomeNavigationIconRole {
    HOME,
    DYNAMIC,
    HISTORY,
    LISTEN_VIDEO,
    PROFILE,
    FAVORITE,
    LIVE,
    WATCH_LATER,
    SETTINGS,
    PLUGINS,
    FOLLOW,
    POPULAR,
    ANIME,
    GAME,
    PARTITION,
    KNOWLEDGE,
    TECH,
}

/**
 * 首页导航统一采用 Miuix 图标；用户补充的 SVG 只用于 Miuix 没有等价符号的场景。
 */
internal enum class HomeNavigationIconSource {
    MIUIX,
    LOCAL_DYNAMIC,
    LOCAL_LIVE,
    LOCAL_GAME,
}

private fun resolveHomeNavigationIconRole(tabId: String): HomeNavigationIconRole = when (tabId.trim().uppercase()) {
    "HOME", "RECOMMEND" -> HomeNavigationIconRole.HOME
    "DYNAMIC" -> HomeNavigationIconRole.DYNAMIC
    "HISTORY" -> HomeNavigationIconRole.HISTORY
    "LISTEN_VIDEO" -> HomeNavigationIconRole.LISTEN_VIDEO
    "PROFILE" -> HomeNavigationIconRole.PROFILE
    "FAVORITE" -> HomeNavigationIconRole.FAVORITE
    "LIVE" -> HomeNavigationIconRole.LIVE
    "WATCHLATER", "WATCH_LATER" -> HomeNavigationIconRole.WATCH_LATER
    "SETTINGS" -> HomeNavigationIconRole.SETTINGS
    "PLUGINS" -> HomeNavigationIconRole.PLUGINS
    "FOLLOW" -> HomeNavigationIconRole.FOLLOW
    "POPULAR" -> HomeNavigationIconRole.POPULAR
    "ANIME" -> HomeNavigationIconRole.ANIME
    "GAME" -> HomeNavigationIconRole.GAME
    "PARTITION" -> HomeNavigationIconRole.PARTITION
    "KNOWLEDGE" -> HomeNavigationIconRole.KNOWLEDGE
    "TECH" -> HomeNavigationIconRole.TECH
    else -> HomeNavigationIconRole.HOME
}

internal fun resolveMiuixPreferredHomeNavigationIconSource(
    tabId: String,
): HomeNavigationIconSource = when (resolveHomeNavigationIconRole(tabId)) {
    HomeNavigationIconRole.DYNAMIC -> HomeNavigationIconSource.LOCAL_DYNAMIC
    HomeNavigationIconRole.LIVE -> HomeNavigationIconSource.LOCAL_LIVE
    HomeNavigationIconRole.GAME -> HomeNavigationIconSource.LOCAL_GAME
    HomeNavigationIconRole.PROFILE,
    HomeNavigationIconRole.PLUGINS,
    HomeNavigationIconRole.FOLLOW,
    HomeNavigationIconRole.POPULAR,
    HomeNavigationIconRole.ANIME,
    HomeNavigationIconRole.KNOWLEDGE,
    HomeNavigationIconRole.TECH,
    HomeNavigationIconRole.HOME,
    HomeNavigationIconRole.HISTORY,
    HomeNavigationIconRole.LISTEN_VIDEO,
    HomeNavigationIconRole.FAVORITE,
    HomeNavigationIconRole.WATCH_LATER,
    HomeNavigationIconRole.SETTINGS,
    HomeNavigationIconRole.PARTITION -> HomeNavigationIconSource.MIUIX
}

/**
 * 首页底栏、侧栏和顶部分区的唯一图标入口。
 */
@Composable
internal fun resolveMiuixPreferredHomeNavigationIcon(
    tabId: String,
    selected: Boolean = false,
): ImageVector {
    val role = resolveHomeNavigationIconRole(tabId)
    return when (resolveMiuixPreferredHomeNavigationIconSource(tabId)) {
        HomeNavigationIconSource.MIUIX -> resolveMiuixHomeNavigationIcon(role, selected)
        HomeNavigationIconSource.LOCAL_DYNAMIC -> ImageVector.vectorResource(
            if (selected) R.drawable.ic_home_nav_dynamic_filled else R.drawable.ic_home_nav_dynamic
        )
        HomeNavigationIconSource.LOCAL_LIVE -> ImageVector.vectorResource(
            if (selected) R.drawable.ic_home_nav_live_filled else R.drawable.ic_home_nav_live
        )
        HomeNavigationIconSource.LOCAL_GAME -> ImageVector.vectorResource(
            if (selected) R.drawable.ic_home_nav_game_filled else R.drawable.ic_home_nav_game
        )
    }
}

private fun resolveMiuixHomeNavigationIcon(
    role: HomeNavigationIconRole,
    selected: Boolean,
): ImageVector = when (role) {
    HomeNavigationIconRole.HOME -> if (selected) MiuixIcons.Medium.Home else MiuixIcons.Home
    HomeNavigationIconRole.HISTORY,
    HomeNavigationIconRole.WATCH_LATER -> if (selected) MiuixIcons.Medium.WorldClock else MiuixIcons.WorldClock
    HomeNavigationIconRole.LISTEN_VIDEO -> if (selected) MiuixIcons.Medium.Music else MiuixIcons.Music
    HomeNavigationIconRole.FAVORITE -> if (selected) MiuixIcons.FavoritesFill else MiuixIcons.Favorites
    HomeNavigationIconRole.SETTINGS -> if (selected) MiuixIcons.Medium.Settings else MiuixIcons.Settings
    HomeNavigationIconRole.PARTITION -> if (selected) MiuixIcons.Medium.GridView else MiuixIcons.GridView
    HomeNavigationIconRole.PROFILE -> MiuixIcons.ContactsCircle
    HomeNavigationIconRole.PLUGINS -> if (selected) MiuixIcons.FolderFill else MiuixIcons.Folder
    HomeNavigationIconRole.FOLLOW -> MiuixIcons.Contacts
    HomeNavigationIconRole.POPULAR -> MiuixIcons.TopDownloads
    HomeNavigationIconRole.ANIME -> MiuixIcons.Play
    HomeNavigationIconRole.KNOWLEDGE -> MiuixIcons.Notes
    HomeNavigationIconRole.TECH -> MiuixIcons.Theme
    HomeNavigationIconRole.DYNAMIC,
    HomeNavigationIconRole.LIVE,
    HomeNavigationIconRole.GAME -> error("Local icon requested through the Miuix branch: $role")
}
