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
    HomeNavigationIconRole.FAVORITE,
    HomeNavigationIconRole.WATCH_LATER,
    HomeNavigationIconRole.SETTINGS,
    HomeNavigationIconRole.PARTITION -> HomeNavigationIconSource.MIUIX
}

/**
 * 首页底栏、侧栏和顶部分区的唯一图标入口。
 *
 * 选中/未选中使用同一字形，选中态由主题色与移动胶囊指示器承载
 * （Miuix 原生底栏契约），避免字形切换造成的视觉错位。
 */
@Composable
internal fun resolveMiuixPreferredHomeNavigationIcon(
    tabId: String,
    selected: Boolean = false,
): ImageVector {
    val role = resolveHomeNavigationIconRole(tabId)
    return when (resolveMiuixPreferredHomeNavigationIconSource(tabId)) {
        HomeNavigationIconSource.MIUIX -> resolveMiuixHomeNavigationIcon(role)
        HomeNavigationIconSource.LOCAL_DYNAMIC -> ImageVector.vectorResource(R.drawable.ic_home_nav_dynamic)
        HomeNavigationIconSource.LOCAL_LIVE -> ImageVector.vectorResource(R.drawable.ic_home_nav_live)
        HomeNavigationIconSource.LOCAL_GAME -> ImageVector.vectorResource(R.drawable.ic_home_nav_game)
    }
}

private fun resolveMiuixHomeNavigationIcon(
    role: HomeNavigationIconRole,
): ImageVector = when (role) {
    HomeNavigationIconRole.HOME -> MiuixIcons.Home
    HomeNavigationIconRole.HISTORY,
    HomeNavigationIconRole.WATCH_LATER -> MiuixIcons.WorldClock
    HomeNavigationIconRole.FAVORITE -> MiuixIcons.Favorites
    HomeNavigationIconRole.SETTINGS -> MiuixIcons.Settings
    HomeNavigationIconRole.PARTITION -> MiuixIcons.GridView
    HomeNavigationIconRole.PROFILE -> MiuixIcons.ContactsCircle
    HomeNavigationIconRole.PLUGINS -> MiuixIcons.Folder
    HomeNavigationIconRole.FOLLOW -> MiuixIcons.Contacts
    HomeNavigationIconRole.POPULAR -> MiuixIcons.TopDownloads
    HomeNavigationIconRole.ANIME -> MiuixIcons.Play
    HomeNavigationIconRole.KNOWLEDGE -> MiuixIcons.Notes
    HomeNavigationIconRole.TECH -> MiuixIcons.Theme
    HomeNavigationIconRole.DYNAMIC,
    HomeNavigationIconRole.LIVE,
    HomeNavigationIconRole.GAME -> error("Local icon requested through the Miuix branch: $role")
}
