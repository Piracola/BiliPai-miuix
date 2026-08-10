package com.android.purebilibili.core.store.navigation

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.purebilibili.core.store.AppNavigationSettings
import com.android.purebilibili.core.store.mapAppNavigationSettingsFromPreferences
import com.android.purebilibili.core.store.resolveListenVideoBottomTabMigration
import com.android.purebilibili.core.store.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

object NavigationSettingsStore {
    private val keyTabletUseSidebar = booleanPreferencesKey("tablet_use_sidebar")
    private val keySidebarAccountSwitcherEnabled =
        booleanPreferencesKey("sidebar_account_switcher_enabled")
    private val keyPredictiveBackEnabled = booleanPreferencesKey("predictive_back_enabled")
    private val keyPredictiveBackAnimationStyle = stringPreferencesKey("predictive_back_animation_style")
    private val keyPredictiveBackExitDirection = stringPreferencesKey("predictive_back_exit_direction")
    private val keyFullScreenSwipeBackEnabled =
        booleanPreferencesKey("full_screen_swipe_back_enabled")
    private val keyBottomBarOrder = stringPreferencesKey("bottom_bar_order")
    private val keyBottomBarVisibleTabs = stringPreferencesKey("bottom_bar_visible_tabs")
    private val keyListenVideoMigrationComplete = booleanPreferencesKey("listen_video_bottom_tab_migration_complete")

    internal fun mapFromPreferences(
        preferences: Preferences,
        defaultTabletUseSidebar: Boolean = false
    ): AppNavigationSettings {
        return mapAppNavigationSettingsFromPreferences(
            preferences = preferences,
            defaultTabletUseSidebar = defaultTabletUseSidebar
        )
    }

    fun observe(context: Context): Flow<AppNavigationSettings> {
        val defaultTabletUseSidebar =
            context.resources.configuration.smallestScreenWidthDp >= 600
        return context.settingsDataStore.data
            .map { preferences ->
                mapFromPreferences(
                    preferences = preferences,
                    defaultTabletUseSidebar = defaultTabletUseSidebar
                )
            }
            .distinctUntilChanged()
    }

    suspend fun ensureListenVideoBottomTabMigration(context: Context) {
        context.settingsDataStore.edit { preferences ->
            val order = (preferences[keyBottomBarOrder]
                ?: "HOME,DYNAMIC,HISTORY,LISTEN_VIDEO,PROFILE")
                .split(',')
                .filter(String::isNotBlank)
            val visible = (preferences[keyBottomBarVisibleTabs]
                ?: "HOME,DYNAMIC,HISTORY,LISTEN_VIDEO,PROFILE")
                .split(',')
                .filter(String::isNotBlank)
                .toSet()
            val migration = resolveListenVideoBottomTabMigration(
                order = order,
                visible = visible,
                migrationComplete = preferences[keyListenVideoMigrationComplete] ?: false
            )
            if (migration.order != order) {
                preferences[keyBottomBarOrder] = migration.order.joinToString(",")
            }
            if (migration.visible != visible) {
                preferences[keyBottomBarVisibleTabs] = migration.visible.joinToString(",")
            }
            if (migration.markComplete) {
                preferences[keyListenVideoMigrationComplete] = true
            }
        }
    }

    suspend fun setTabletUseSidebar(context: Context, useSidebar: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[keyTabletUseSidebar] = useSidebar
        }
    }

    suspend fun setSidebarAccountSwitcherEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[keySidebarAccountSwitcherEnabled] = enabled
        }
    }

    suspend fun setPredictiveBackEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[keyPredictiveBackEnabled] = enabled
        }
    }

    suspend fun setPredictiveBackAnimationStyle(context: Context, style: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[keyPredictiveBackAnimationStyle] = style
        }
    }

    suspend fun setPredictiveBackExitDirection(context: Context, direction: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[keyPredictiveBackExitDirection] = direction
        }
    }

    /** Miuix 全屏滑动返回（默认关闭，仅保留系统边缘预测返回）。 */
    fun getFullScreenSwipeBackEnabled(context: Context): Flow<Boolean> =
        context.settingsDataStore.data
            .map { preferences -> preferences[keyFullScreenSwipeBackEnabled] ?: false }

    suspend fun setFullScreenSwipeBackEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[keyFullScreenSwipeBackEnabled] = enabled
        }
    }
}
