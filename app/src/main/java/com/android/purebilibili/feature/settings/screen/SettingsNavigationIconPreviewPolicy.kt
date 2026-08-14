package com.android.purebilibili.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*

private enum class SettingsNavigationIconRole {
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

/** Uses the active native icon family so the navigation preview matches the runtime chrome. */
internal fun resolveSettingsNavigationPreviewIcon(
    tabId: String,
    iconFamily: AppSemanticIconFamily,
    selected: Boolean = false,
): ImageVector {
    val role = when (tabId.trim().uppercase()) {
        "HOME", "RECOMMEND" -> SettingsNavigationIconRole.HOME
        "DYNAMIC" -> SettingsNavigationIconRole.DYNAMIC
        "HISTORY" -> SettingsNavigationIconRole.HISTORY
        "LISTEN_VIDEO" -> SettingsNavigationIconRole.LISTEN_VIDEO
        "PROFILE" -> SettingsNavigationIconRole.PROFILE
        "FAVORITE" -> SettingsNavigationIconRole.FAVORITE
        "LIVE" -> SettingsNavigationIconRole.LIVE
        "WATCHLATER", "WATCH_LATER" -> SettingsNavigationIconRole.WATCH_LATER
        "SETTINGS" -> SettingsNavigationIconRole.SETTINGS
        "PLUGINS" -> SettingsNavigationIconRole.PLUGINS
        "FOLLOW" -> SettingsNavigationIconRole.FOLLOW
        "POPULAR" -> SettingsNavigationIconRole.POPULAR
        "ANIME" -> SettingsNavigationIconRole.ANIME
        "GAME" -> SettingsNavigationIconRole.GAME
        "PARTITION" -> SettingsNavigationIconRole.PARTITION
        "KNOWLEDGE" -> SettingsNavigationIconRole.KNOWLEDGE
        "TECH" -> SettingsNavigationIconRole.TECH
        else -> SettingsNavigationIconRole.HOME
    }

    return when (iconFamily) {
        AppSemanticIconFamily.MIUIX -> when (role) {
            SettingsNavigationIconRole.HOME -> if (selected) MiuixIcons.Medium.Th1 else MiuixIcons.Th1
            SettingsNavigationIconRole.DYNAMIC -> if (selected) MiuixIcons.Medium.Messages else MiuixIcons.Messages
            SettingsNavigationIconRole.HISTORY -> if (selected) MiuixIcons.Medium.Recent else MiuixIcons.Recent
            SettingsNavigationIconRole.LISTEN_VIDEO -> if (selected) MiuixIcons.Medium.Music else MiuixIcons.Music
            SettingsNavigationIconRole.PROFILE -> MiuixIcons.ContactsCircle
            SettingsNavigationIconRole.FAVORITE -> if (selected) MiuixIcons.FavoritesFill else MiuixIcons.Favorites
            SettingsNavigationIconRole.LIVE -> if (selected) MiuixIcons.Medium.RecordingTape else MiuixIcons.RecordingTape
            SettingsNavigationIconRole.WATCH_LATER -> if (selected) MiuixIcons.Medium.WorldClock else MiuixIcons.WorldClock
            SettingsNavigationIconRole.SETTINGS -> if (selected) MiuixIcons.Medium.Settings else MiuixIcons.Settings
            SettingsNavigationIconRole.PLUGINS -> if (selected) MiuixIcons.FolderFill else MiuixIcons.Folder
            SettingsNavigationIconRole.FOLLOW -> MiuixIcons.Contacts
            SettingsNavigationIconRole.POPULAR -> MiuixIcons.TopDownloads
            SettingsNavigationIconRole.ANIME -> MiuixIcons.Play
            SettingsNavigationIconRole.GAME -> MiuixIcons.Store
            SettingsNavigationIconRole.PARTITION -> if (selected) MiuixIcons.Medium.GridView else MiuixIcons.GridView
            SettingsNavigationIconRole.KNOWLEDGE -> MiuixIcons.Notes
            SettingsNavigationIconRole.TECH -> MiuixIcons.Theme
        }
        AppSemanticIconFamily.MATERIAL -> when (role) {
            SettingsNavigationIconRole.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
            SettingsNavigationIconRole.DYNAMIC -> if (selected) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone
            SettingsNavigationIconRole.HISTORY -> if (selected) Icons.Filled.History else Icons.Outlined.History
            SettingsNavigationIconRole.LISTEN_VIDEO -> if (selected) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic
            SettingsNavigationIconRole.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
            SettingsNavigationIconRole.FAVORITE -> if (selected) Icons.Filled.CollectionsBookmark else Icons.Outlined.CollectionsBookmark
            SettingsNavigationIconRole.LIVE -> if (selected) Icons.Filled.LiveTv else Icons.Outlined.LiveTv
            SettingsNavigationIconRole.WATCH_LATER -> if (selected) Icons.Filled.WatchLater else Icons.Outlined.WatchLater
            SettingsNavigationIconRole.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
            SettingsNavigationIconRole.PLUGINS -> if (selected) Icons.Filled.Extension else Icons.Outlined.Extension
            SettingsNavigationIconRole.FOLLOW -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
            SettingsNavigationIconRole.POPULAR -> if (selected) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Outlined.TrendingUp
            SettingsNavigationIconRole.ANIME -> if (selected) Icons.Filled.Tv else Icons.Outlined.Tv
            SettingsNavigationIconRole.GAME -> if (selected) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports
            SettingsNavigationIconRole.PARTITION -> if (selected) Icons.Filled.GridView else Icons.Outlined.GridView
            SettingsNavigationIconRole.KNOWLEDGE -> if (selected) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb
            SettingsNavigationIconRole.TECH -> if (selected) Icons.Filled.SmartToy else Icons.Outlined.SmartToy
        }
    }
}
