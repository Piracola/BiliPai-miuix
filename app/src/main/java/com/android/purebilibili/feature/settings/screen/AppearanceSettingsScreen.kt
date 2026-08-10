@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.android.purebilibili.feature.settings
import com.android.purebilibili.core.ui.AppIconStyle
import com.android.purebilibili.core.ui.AppListItemStyle
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.core.ui.components.AppSegmentOption

import android.os.Build
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.*
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.ui.AdaptivePlainTooltipBox
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.R
import com.android.purebilibili.core.store.CommonListHeaderCollapseMode
import com.android.purebilibili.core.store.HomeDurationStyle
import com.android.purebilibili.core.store.HomeFeedCardStyle
import com.android.purebilibili.core.store.HomeWallpaperEffectMode
import com.android.purebilibili.core.store.HomeWallpaperEffectScope
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.store.ThemeModeRoleOverrides
import com.android.purebilibili.core.store.ThemeRoleOverrides
import coil.compose.AsyncImage
import com.android.purebilibili.core.theme.deleteStoredAppFont
import com.android.purebilibili.core.theme.importAppFontFromUri
import com.android.purebilibili.core.theme.*
import com.android.purebilibili.core.ui.adaptive.resolveDeviceUiProfile
import com.android.purebilibili.core.ui.adaptive.resolveEffectiveMotionTier
import com.android.purebilibili.core.ui.blur.BlurIntensity
import com.android.purebilibili.core.ui.blur.shouldAllowHomeChromeLiquidGlass
import com.android.purebilibili.core.ui.getWindowNavigationBarColor
import com.android.purebilibili.core.ui.rememberAppSparklesIcon
import com.android.purebilibili.core.ui.setWindowNavigationBarColor
import com.android.purebilibili.feature.settings.ui.SettingsPageScaffold
import com.android.purebilibili.core.util.HapticType
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.core.util.rememberHapticFeedback
import kotlinx.coroutines.launch
import com.android.purebilibili.core.ui.components.*
import com.android.purebilibili.core.ui.animation.EntranceGroup
import com.android.purebilibili.core.ui.animation.entrance
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.HueSlider
import com.github.skydoves.colorpicker.compose.SaturationSlider
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 *  外观设置二级页面
 * iOS 风格设计
 */
enum class AppearanceSettingsContentMode {
    APPEARANCE,
    HOME,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AppearanceSettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit,
    onNavigateToIconSettings: () -> Unit = {},
    contentMode: AppearanceSettingsContentMode = AppearanceSettingsContentMode.APPEARANCE,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var pendingLanguageRestart by remember { mutableStateOf<AppLanguage?>(null) }
    val backLabel = stringResource(R.string.common_back)
    val screenTitle = when (contentMode) {
        AppearanceSettingsContentMode.APPEARANCE -> stringResource(R.string.appearance_settings_title)
        AppearanceSettingsContentMode.HOME -> "首页设置"
    }
    val restartDialogTitle = stringResource(R.string.app_language_restart_dialog_title)
    val restartDialogMessage = stringResource(R.string.app_language_restart_dialog_message)
    val restartDialogConfirm = stringResource(R.string.app_language_restart_dialog_confirm)
    val displayLevel = when (state.displayMode) {
        0 -> 0.35f
        1 -> 0.6f
        else -> 0.85f
    }
    val appearanceInteractionLevel = (
        displayLevel +
            if (state.headerBlurEnabled) 0.1f else 0f +
            if (state.isBottomBarFloating) 0.1f else 0f
        ).coerceIn(0f, 1f)
    val appearanceAnimationSpeed = if (state.dynamicColor) 1.1f else 1f
    
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    //  [修复] 设置导航栏透明，确保底部手势栏沉浸式效果
    androidx.compose.runtime.DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val originalNavBarColor = window?.let(::getWindowNavigationBarColor)
            ?: android.graphics.Color.TRANSPARENT

        if (window != null) {
            setWindowNavigationBarColor(window, android.graphics.Color.TRANSPARENT)
        }

        onDispose {
            if (window != null) {
                setWindowNavigationBarColor(window, originalNavBarColor)
            }
        }
    }

    SettingsPageScaffold(
        title = screenTitle,
        onBack = onBack,
        backContentDescription = backLabel,
        bottomContentPadding = bottomContentPadding,
        scrollHost = SettingsPageScrollHost.External,
        topBarBlurEnabled = state.headerBlurEnabled,
    ) {
        CompositionLocalProvider(LocalSettingsLiquidGlassEnabled provides state.isLiquidGlassEnabled) {
            AppearanceSettingsContent(
                state = state,
                onNavigateToIconSettings = onNavigateToIconSettings,
                contentMode = contentMode,
                viewModel = viewModel,
                context = context,
                onAppLanguageChange = { language ->
                    if (shouldPromptAppRestartForLanguageChange(state.appLanguage, language)) {
                        pendingLanguageRestart = language
                    }
                },
            )
        }
    }

    pendingLanguageRestart?.let { pendingLanguage ->
        AppAlertDialog(
            onDismissRequest = { pendingLanguageRestart = null },
            title = { AppText(restartDialogTitle) },
            text = { AppText(restartDialogMessage) },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        pendingLanguageRestart = null
                        coroutineScope.launch {
                            persistAndApplyAppLanguageBeforeRestart(
                                appLanguage = pendingLanguage,
                                persist = { SettingsManager.setAppLanguage(context, it) },
                                restart = { restartApp(context) }
                            )
                        }
                    }
                ) {
                    AppText(restartDialogConfirm)
                }
            },
            dismissButton = {
                AppTextButton(onClick = { pendingLanguageRestart = null }) {
                    AppText(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun HomeSettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit,
) {
    AppearanceSettingsScreen(
        viewModel = viewModel,
        onBack = onBack,
        contentMode = AppearanceSettingsContentMode.HOME,
    )
}

@Composable
fun AppearanceSettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onNavigateToIconSettings: () -> Unit,
    contentMode: AppearanceSettingsContentMode,
    viewModel: SettingsViewModel,
    context: android.content.Context,
    onAppLanguageChange: (AppLanguage) -> Unit
) {
    val singleChoicePresentation by SettingsManager
        .getSingleChoicePresentation(context)
        .collectAsStateWithLifecycle(AppSingleChoicePresentation.WINDOW_POPUP)
    val singleChoicePresentationOptions = remember {
        listOf(
            AppSegmentOption(AppSingleChoicePresentation.WINDOW_POPUP, "跟随选项弹出"),
            AppSegmentOption(AppSingleChoicePresentation.CENTERED_DIALOG, "居中弹窗"),
        )
    }
    val listState = rememberLazyListState()
    val focusRequest by SettingsSearchFocusController.request.collectAsStateWithLifecycle()
    // Animation Trigger
    val displayModeTint = rememberAdaptiveSemanticIconTint(iOSBlue)

    val configuration = LocalConfiguration.current
    val displayMetricsSnapshot = LocalDisplayMetricsSnapshot.current
    val isTablet = configuration.screenWidthDp >= 600 // Material Design 3 中型屏幕断点
    LaunchedEffect(focusRequest?.token) {
        val request = focusRequest ?: return@LaunchedEffect
        val expectedTarget = when (contentMode) {
            AppearanceSettingsContentMode.APPEARANCE -> SettingsSearchTarget.APPEARANCE
            AppearanceSettingsContentMode.HOME -> SettingsSearchTarget.HOME_FEED
        }
        if (request.target != expectedTarget) return@LaunchedEffect
        val focusKey = when (contentMode) {
            AppearanceSettingsContentMode.APPEARANCE ->
                resolveAppearanceSettingsFocusKey(request.focusId)
            AppearanceSettingsContentMode.HOME -> resolveHomeSettingsFocusKey(request.focusId)
        } ?: return@LaunchedEffect
        listState.animateScrollToItemByKey(focusKey)
        SettingsSearchFocusController.clear(request.token)
    }
    val windowSizeClass = LocalWindowSizeClass.current
    val deviceUiProfile = remember(windowSizeClass.widthSizeClass) {
        resolveDeviceUiProfile(
            widthSizeClass = windowSizeClass.widthSizeClass
        )
    }
    val scope = rememberCoroutineScope()
    val themeSectionTitle = stringResource(R.string.appearance_theme_color_section)
    val uiPresetTitle = stringResource(R.string.appearance_ui_preset_title)
    val uiPresetSubtitle = stringResource(R.string.appearance_ui_preset_subtitle)
    val uiStyleMaterialLabel = stringResource(R.string.appearance_android_native_variant_material3)
    val uiStyleMiuixLabel = stringResource(R.string.appearance_android_native_variant_miuix)
    val uiStyleOptions = remember(uiStyleMaterialLabel, uiStyleMiuixLabel) {
        resolveThemeSelectionOptions(
            material3Label = uiStyleMaterialLabel,
            miuixLabel = uiStyleMiuixLabel,
        )
    }
    val uiPresetAndroidMaterialTitle = stringResource(R.string.appearance_ui_preset_android_material_title)
    val uiPresetAndroidMaterialSummary = stringResource(R.string.appearance_ui_preset_android_material_summary)
    val uiPresetAndroidMiuixTitle = stringResource(R.string.appearance_ui_preset_android_miuix_title)
    val uiPresetAndroidMiuixSummary = stringResource(R.string.appearance_ui_preset_android_miuix_summary)
    val uiPresetDescription = remember(
        state.themeSelection,
        uiPresetAndroidMaterialTitle,
        uiPresetAndroidMaterialSummary,
        uiPresetAndroidMiuixTitle,
        uiPresetAndroidMiuixSummary
    ) {
        resolveAppearanceUiPresetDescription(
            selection = state.themeSelection,
            materialTitle = uiPresetAndroidMaterialTitle,
            materialSummary = uiPresetAndroidMaterialSummary,
            miuixTitle = uiPresetAndroidMiuixTitle,
            miuixSummary = uiPresetAndroidMiuixSummary
        )
    }
    val themeModeTitle = stringResource(R.string.appearance_theme_mode_title)
    val themeModeSubtitle = stringResource(R.string.appearance_theme_mode_subtitle)
    val themeModeFollowSystemShortLabel = stringResource(R.string.theme_mode_follow_system_short)
    val themeModeLightShortLabel = stringResource(R.string.theme_mode_light_short)
    val themeModeDarkShortLabel = stringResource(R.string.theme_mode_dark_short)
    val themeModeOptions = remember(
        themeModeFollowSystemShortLabel,
        themeModeLightShortLabel,
        themeModeDarkShortLabel
    ) {
        resolveThemeModeSegmentOptions(
            followSystemLabel = themeModeFollowSystemShortLabel,
            lightLabel = themeModeLightShortLabel,
            darkLabel = themeModeDarkShortLabel
        )
    }
    val darkThemeStyleTitle = stringResource(R.string.appearance_dark_theme_style_title)
    val darkThemeStyleSubtitle = stringResource(R.string.appearance_dark_theme_style_subtitle)
    val darkThemeStyleDefaultShortLabel = stringResource(R.string.dark_theme_style_default_short)
    val darkThemeStyleAmoledShortLabel = stringResource(R.string.dark_theme_style_amoled_short)
    val darkThemeStyleOptions = remember(
        darkThemeStyleDefaultShortLabel,
        darkThemeStyleAmoledShortLabel
    ) {
        resolveDarkThemeStyleSegmentOptions(
            defaultLabel = darkThemeStyleDefaultShortLabel,
            amoledLabel = darkThemeStyleAmoledShortLabel
        )
    }
    val appLanguageTitle = stringResource(R.string.appearance_app_language_title)
    val appLanguageSubtitle = stringResource(R.string.appearance_app_language_subtitle)
    val appLanguageFollowSystemShortLabel = stringResource(R.string.app_language_follow_system_short)
    val appLanguageSimplifiedShortLabel = stringResource(R.string.app_language_simplified_chinese_short)
    val appLanguageTraditionalShortLabel = stringResource(R.string.app_language_traditional_chinese_short)
    val appLanguageEnglishShortLabel = stringResource(R.string.app_language_english_short)
    val appLanguageOptions = remember(
        appLanguageFollowSystemShortLabel,
        appLanguageSimplifiedShortLabel,
        appLanguageTraditionalShortLabel,
        appLanguageEnglishShortLabel
    ) {
        resolveAppLanguageSegmentOptions(
            followSystemLabel = appLanguageFollowSystemShortLabel,
            simplifiedChineseLabel = appLanguageSimplifiedShortLabel,
            traditionalChineseLabel = appLanguageTraditionalShortLabel,
            englishLabel = appLanguageEnglishShortLabel
        )
    }
    val navigationBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentBottomPadding = resolveAppearanceBottomPadding(
        navigationBarsBottom = navigationBarBottomPadding,
        expandableSectionEnabled = true
    )
    val compactVideoStatsOnCover by SettingsManager
        .getCompactVideoStatsOnCover(context)
        .collectAsStateWithLifecycle(initialValue = true)
    val dedicatedHomeWallpaperUri by SettingsManager
        .getHomeWallpaperUri(context)
        .collectAsStateWithLifecycle(initialValue = "")
    val splashWallpaperFallbackUri by SettingsManager
        .getSplashWallpaperUri(context)
        .collectAsStateWithLifecycle(initialValue = "")
    val resolvedHomeWallpaperUri = remember(dedicatedHomeWallpaperUri, splashWallpaperFallbackUri) {
        dedicatedHomeWallpaperUri.ifBlank { splashWallpaperFallbackUri }.trim()
    }
    val homeWallpaperFollowsSplash = dedicatedHomeWallpaperUri.isBlank() && splashWallpaperFallbackUri.isNotBlank()
    val homeWallpaperEffectMode by SettingsManager
        .getHomeWallpaperEffectMode(context)
        .collectAsStateWithLifecycle(initialValue = HomeWallpaperEffectMode.SOFT_BLUR)
    val homeWallpaperEffectScope by SettingsManager
        .getHomeWallpaperEffectScope(context)
        .collectAsStateWithLifecycle(initialValue = HomeWallpaperEffectScope.HOME_ONLY)
    val homeWallpaperEffectOptions = remember {
        listOf(
            AppSegmentOption(HomeWallpaperEffectMode.OFF, "关闭"),
            AppSegmentOption(HomeWallpaperEffectMode.SOFT_BLUR, "柔和"),
            AppSegmentOption(HomeWallpaperEffectMode.STRONG_BLUR, "强模糊"),
            AppSegmentOption(HomeWallpaperEffectMode.ORIGINAL, "原图")
        )
    }
    val homeWallpaperEffectScopeOptions = remember {
        listOf(
            AppSegmentOption(HomeWallpaperEffectScope.HOME_ONLY, "仅首页"),
            AppSegmentOption(HomeWallpaperEffectScope.GLOBAL, "全局")
        )
    }
    val homeUpBadgesVisible by SettingsManager
        .getHomeUpBadgesVisible(context)
        .collectAsStateWithLifecycle(initialValue = true)
    val homeUpAvatarsVisible by SettingsManager
        .getHomeUpAvatarsVisible(context)
        .collectAsStateWithLifecycle(initialValue = true)
    val homeDurationStyle by SettingsManager
        .getHomeDurationStyle(context)
        .collectAsStateWithLifecycle(initialValue = HomeDurationStyle.OUTSIDE_COVER)
    val homeFeedCardStyle by SettingsManager
        .getHomeFeedCardStyle(context)
        .collectAsStateWithLifecycle(initialValue = HomeFeedCardStyle.CURRENT)
    val homeHeroCarouselEnabled by SettingsManager
        .getHomeHeroCarouselEnabled(context)
        .collectAsStateWithLifecycle(initialValue = true)
    val homeHeroCarouselAutoplayEnabled by SettingsManager
        .getHomeHeroCarouselAutoplayEnabled(context)
        .collectAsStateWithLifecycle(initialValue = false)
    val commonListHeaderCollapseMode by SettingsManager
        .getCommonListHeaderCollapseMode(context)
        .collectAsStateWithLifecycle(
            initialValue = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL
        )
    val commonListHeaderCollapseOptions = remember {
        CommonListHeaderCollapseMode.entries.map { mode ->
            AppSegmentOption(mode, mode.label)
        }
    }
    val themeRoleOverrides by SettingsManager
        .getThemeRoleOverrides(context)
        .collectAsStateWithLifecycle(initialValue = ThemeRoleOverrides())
    val baseThemeRoleOverrides = LocalBaseThemeRoleOverrides.current
    val showOnlineCount by SettingsManager
        .getShowOnlineCount(context)
        .collectAsStateWithLifecycle(initialValue = false)
    val isLiquidGlassAvailable = shouldAllowHomeChromeLiquidGlass(Build.VERSION.SDK_INT)
    val showThemeColorPicker = state.md3ColorSource == Md3ColorSource.CUSTOM
    var showMd3ColorPickerDialog by remember { mutableStateOf(false) }
    var roleColorTarget by remember { mutableStateOf<ThemeRoleColorTarget?>(null) }
    val md3ColorSourceOptions = remember { resolveMd3ColorSourceOptions() }
    // D2：是否存在视觉覆盖（图标样式/列表条目/单选呈现/高级配色），
    // 决定"恢复主题推荐"入口是否可见。
    val hasAppearanceOverrides = remember(
        state.appIconStyle,
        state.appListItemStyle,
        singleChoicePresentation,
        themeRoleOverrides.enabled,
    ) {
        state.appIconStyle != AppIconStyle.AUTO ||
            state.appListItemStyle != AppListItemStyle.AUTO ||
            singleChoicePresentation != AppSingleChoicePresentation.WINDOW_POPUP ||
            themeRoleOverrides.enabled
    }
    val selectedCustomThemeColor = remember(state.md3CustomColorHex) {
        parseMd3CustomColorHex(state.md3CustomColorHex)
    }
    val selectedThemeColorName = remember(selectedCustomThemeColor) {
        val selectedIndex = ThemeColors.indexOf(selectedCustomThemeColor)
        ThemeColorNames.getOrNull(selectedIndex) ?: "自定义"
    }
    var themeColorPaletteExpanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showThemeColorPicker) {
        if (!showThemeColorPicker) themeColorPaletteExpanded = false
    }
    val colorStyleOptions = remember { resolveColorStyleOptions() }
    val colorSpecOptions = remember { resolveColorSpecOptions() }
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        importAppFontFromUri(context, uri)
            .onSuccess { imported ->
                viewModel.setAppFontFile(imported.fileName, imported.displayName)
                Toast.makeText(context, "已导入字体：${imported.displayName}", Toast.LENGTH_SHORT).show()
            }
            .onFailure { error ->
                Toast.makeText(
                    context,
                    error.message ?: "字体导入失败，请选择 .ttf / .otf / .ttc 文件",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    EntranceGroup {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize(),
        // [Fix] 为可展开配置项增加安全底部留白，避免“小屏+展开”时显示不全
        contentPadding = PaddingValues(bottom = contentBottomPadding)
    ) {
        if (contentMode == AppearanceSettingsContentMode.APPEARANCE) {

        // ========== 界面与明暗 ==========
        item(key = "appearance_group_ui_dark_title") {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceSectionTitle("界面与明暗")
            }
        }
        item(key = AppearanceSettingsGroupKeys.UI_AND_DARK) {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceGroup {
                    Column {
                        // 界面预设（标题不再拼接当前值，当前值由 trailing/选项弹层呈现）
                        SettingsSingleChoicePreference(
                            title = uiPresetTitle,
                            subtitle = uiPresetSubtitle,
                            options = uiStyleOptions,
                            selectedValue = state.themeSelection,
                            onSelectionChange = viewModel::setThemeSelection,
                        )

                        // D3：说明卡降级为小字说明，只承担一个影响，不占视觉层级
                        Spacer(modifier = Modifier.height(8.dp))
                        AppText(
                            text = uiPresetDescription.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsSingleChoicePreference(
                            title = themeModeTitle,
                            subtitle = themeModeSubtitle,
                            options = themeModeOptions,
                            selectedValue = state.themeMode,
                            onSelectionChange = viewModel::setThemeMode,
                        )

                        androidx.compose.animation.AnimatedVisibility(
                            visible = state.themeMode != AppThemeMode.LIGHT,
                            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                AppPreferenceDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                SettingsSingleChoicePreference(
                                    title = darkThemeStyleTitle,
                                    subtitle = darkThemeStyleSubtitle,
                                    options = darkThemeStyleOptions,
                                    selectedValue = state.darkThemeStyle,
                                    onSelectionChange = viewModel::setDarkThemeStyle,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ========== 颜色 ==========
        item(key = "appearance_group_color_title") {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceSectionTitle("颜色")
            }
        }
        item(key = AppearanceSettingsGroupKeys.COLOR) {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceGroup {
                    Column {
                        SettingsSingleChoicePreference(
                            title = "MD3 颜色来源",
                            subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                "可跟随系统壁纸，也可使用自定义主题色"
                            } else {
                                "当前系统不支持 Monet 壁纸取色，可使用自定义主题色"
                            },
                            options = md3ColorSourceOptions,
                            selectedValue = state.md3ColorSource,
                            onSelectionChange = viewModel::setMd3ColorSource
                        )

                        AnimatedVisibility(
                            visible = state.md3ColorSource == Md3ColorSource.FOLLOW_WALLPAPER,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                DynamicColorPreview()
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        AppPreferenceDivider()
                        AppPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.DYNAMIC_COLOR),
                            title = "自定义 MD3 颜色",
                            subtitle = if (state.md3ColorSource == Md3ColorSource.CUSTOM) {
                                "可直接使用取色器，也可输入 #RRGGBB 色值"
                            } else {
                                "当前跟随系统壁纸；确认后切换为自定义颜色"
                            },
                            value = state.md3CustomColorHex,
                            onClick = { showMd3ColorPickerDialog = true },
                            iconTint = selectedCustomThemeColor
                        )

                        // 主题色选择 (仅当自定义颜色时显示)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showThemeColorPicker,
                            enter =   androidx.compose.animation.expandVertically() +   androidx.compose.animation.fadeIn(),
                            exit =   androidx.compose.animation.shrinkVertically() +   androidx.compose.animation.fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                AppPreferenceDivider()
                                AppPreference(
                                    icon = rememberSettingsSemanticIcon(SettingsIconRole.THEME_COLOR_PICKER),
                                    title = "主题色",
                                    subtitle = if (themeColorPaletteExpanded) {
                                        "当前 ${state.md3CustomColorHex}；点按收起色板"
                                    } else {
                                        "当前 ${state.md3CustomColorHex}；点按展开预设色板"
                                    },
                                    value = if (themeColorPaletteExpanded) "收起" else "展开",
                                    onClick = { themeColorPaletteExpanded = !themeColorPaletteExpanded },
                                    iconTint = selectedCustomThemeColor,
                                )

                                AnimatedVisibility(
                                    visible = themeColorPaletteExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut(),
                                ) {
                                    Column(modifier = Modifier.padding(top = 12.dp)) {
                                        // 实时主题色预览
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 24.dp)
                                                .height(140.dp)
                                                .clip(AppShapes.container(ContainerLevel.Sheet))
                                                .background(
                                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        colors = listOf(
                                                            selectedCustomThemeColor.copy(alpha = 0.15f),
                                                            selectedCustomThemeColor.copy(alpha = 0.05f)
                                                        )
                                                    )
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = selectedCustomThemeColor.copy(alpha = 0.3f),
                                                    shape = AppShapes.borderedContainer(ContainerLevel.Sheet)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .padding(bottom = 12.dp)
                                                        .background(
                                                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                                colors = listOf(
                                                                    selectedCustomThemeColor,
                                                                    selectedCustomThemeColor.copy(alpha = 0.8f)
                                                                )
                                                            ),
                                                            shape = AppShapes.container(ContainerLevel.Dialog)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    AppIcon(
                                                        Icons.Filled.PlayArrow,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                                AppText(
                                                    text = state.md3CustomColorHex,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                AppText(
                                                    text = "正在预览自定义 MD3 主题色",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Theme Color Grid - Strict 2 Rows x 5 Columns
                                        val spacing = 12.dp
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            ThemeColors.chunked(5).forEach { rowColors ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(spacing)
                                                ) {
                                                    rowColors.forEach { color ->
                                                        val index = ThemeColors.indexOf(color)
                                                        val isSelected = selectedCustomThemeColor == color
                                                        Column(
                                                            modifier = Modifier.weight(1f),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            val scale by androidx.compose.animation.core.animateFloatAsState(
                                                                targetValue = if (isSelected) 1.1f else 1.0f,
                                                                label = "scale",
                                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .aspectRatio(1f)
                                                                    .graphicsLayer {
                                                                        scaleX = scale
                                                                        scaleY = scale
                                                                    }
                                                                    .border(
                                                                        width = if (isSelected) 2.dp else 0.dp,
                                                                        color = if (isSelected) color.copy(alpha = 0.5f) else Color.Transparent,
                                                                        shape = CircleShape
                                                                    )
                                                                    .padding(3.dp)
                                                                    .clip(CircleShape)
                                                                    .background(
                                                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                                                            colors = listOf(
                                                                                color.copy(alpha = 0.9f),
                                                                                color
                                                                            ),
                                                                            center = androidx.compose.ui.geometry.Offset.Unspecified,
                                                                            radius = Float.POSITIVE_INFINITY
                                                                        )
                                                                    )
                                                                    .background(
                                                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                                            colors = listOf(
                                                                                Color.White.copy(alpha = 0.2f),
                                                                                Color.Transparent
                                                                            ),
                                                                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                                                            end = androidx.compose.ui.geometry.Offset(100f, 100f)
                                                                        )
                                                                    )
                                                                    .clickable {
                                                                        viewModel.setThemeColorIndex(index)
                                                                        viewModel.setMd3CustomColorHex(formatMd3CustomColorHex(color))
                                                                    },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                androidx.compose.animation.AnimatedVisibility(
                                                                    visible = isSelected,
                                                                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                                                                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                                                                ) {
                                                                    AppIcon(
                                                                        Icons.Outlined.Check,
                                                                        contentDescription = null,
                                                                        tint = Color.White,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            AppText(
                                                                text = ThemeColorNames.getOrElse(index) { "" },
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                    if (rowColors.size < 5) {
                                                        repeat(5 - rowColors.size) {
                                                            Spacer(modifier = Modifier.weight(1f))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        AppPreferenceDivider()
                        ThemePresetChoiceSetting(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.COLOR_STYLE),
                            title = "色彩风格",
                            selectedValue = state.colorStyle,
                            options = colorStyleOptions,
                            onSelectionChange = viewModel::setThemeColorStyle,
                            iconTint = iOSPurple
                        )

                        AppPreferenceDivider()
                        ThemePresetChoiceSetting(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.COLOR_SPEC),
                            title = "色彩标准",
                            selectedValue = state.colorSpec,
                            options = colorSpecOptions,
                            onSelectionChange = viewModel::setThemeColorSpec,
                            iconTint = iOSBlue
                        )
                    }
                }
            }
        }

        // ========== 表面与效果 ==========
        item(key = "appearance_group_surface_effects_title") {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceSectionTitle("表面与效果")
            }
        }
        item(key = AppearanceSettingsGroupKeys.SURFACE_AND_EFFECTS) {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceGroup {
                    AppSwitchPreference(
                        icon = rememberSettingsSemanticIcon(SettingsIconRole.ANDROID_LIQUID_GLASS),
                        title = "安卓原生液态玻璃",
                        subtitle = if (isLiquidGlassAvailable) {
                            "全局开启后，顶部 Dock、搜索框、底栏、分段控件与评论区统一复用底栏液态玻璃材质"
                        } else {
                            "当前 Android 版本暂不支持液态玻璃效果"
                        },
                        checked = state.androidNativeLiquidGlassEnabled,
                        onCheckedChange = { viewModel.toggleAndroidNativeLiquidGlass(it) },
                        enabled = isLiquidGlassAvailable,
                        iconTint = iOSBlue
                    )
                }
            }
        }

        // ========== 文字与显示 ==========
        item(key = "appearance_group_text_display_title") {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceSectionTitle("文字与显示")
            }
        }
        item(key = AppearanceSettingsGroupKeys.TEXT_AND_DISPLAY) {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceGroup {
                    Column {
                        SettingsSingleChoicePreference(
                            title = "字体大小",
                            subtitle = "仅调整应用内文字比例",
                            options = resolveAppFontSizeSegmentOptions(),
                            selectedValue = state.appFontSizePreset,
                            onSelectionChange = viewModel::setAppFontSizePreset,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        AppPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.FONT_FILE),
                            title = "应用字体",
                            subtitle = if (state.appFontDisplayName.isBlank()) {
                                "使用系统默认字体，或从本地导入 .ttf / .otf / .ttc"
                            } else {
                                "当前：${state.appFontDisplayName}"
                            },
                            value = if (state.appFontDisplayName.isBlank()) "默认" else "更换",
                            onClick = {
                                fontPickerLauncher.launch(arrayOf("*/*"))
                            },
                            iconTint = iOSPurple
                        )

                        AnimatedVisibility(
                            visible = state.appFontFileName.isNotBlank(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                AppPreferenceDivider()
                                AppPreference(
                                    icon = rememberSettingsSemanticIcon(SettingsIconRole.REPLAY_ONBOARDING),
                                    title = "恢复默认字体",
                                    subtitle = "移除已导入字体文件，立即回到系统字体",
                                    onClick = {
                                        deleteStoredAppFont(context, state.appFontFileName)
                                        viewModel.clearAppFontFile()
                                        Toast.makeText(context, "已恢复默认字体", Toast.LENGTH_SHORT).show()
                                    },
                                    iconTint = iOSOrange,
                                    showChevron = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsSingleChoicePreference(
                            title = "界面缩放",
                            subtitle = "调整列表、卡片与控件的整体密度",
                            options = resolveAppUiScaleSegmentOptions(),
                            selectedValue = state.appUiScalePreset,
                            onSelectionChange = viewModel::setAppUiScalePreset,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.DISPLAY_STYLE),
                            title = "应用显示缩放（高级）",
                            subtitle = resolveDpiOverrideSubtitle(
                                systemDensityDpi = displayMetricsSnapshot.systemDensityDpi,
                                systemSmallestWidthDp = displayMetricsSnapshot.systemSmallestWidthDp,
                                currentOverridePercent = state.appDpiOverridePercent
                            ),
                            checked = state.appDpiOverridePercent > 0,
                            onCheckedChange = { enabled ->
                                viewModel.setAppDpiOverridePercent(
                                    if (enabled) DEFAULT_APP_DPI_OVERRIDE_PERCENT else 0
                                )
                            },
                            iconTint = iOSTeal
                        )

                        AnimatedVisibility(
                            visible = state.appDpiOverridePercent > 0,
                            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                SettingsSingleChoicePreference(
                                    title = "显示缩放",
                                    subtitle = "只调整 BiliPai 内文字和控件的整体大小，不修改系统显示设置",
                                    options = resolveAppDpiOverrideSegmentOptions(),
                                    selectedValue = resolveDisplayedAppDpiPercent(state.appDpiOverridePercent),
                                    onSelectionChange = { percent ->
                                        viewModel.setAppDpiOverridePercent(percent)
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                AppText(
                                    text = resolveDisplayMetricsSummary(displayMetricsSnapshot),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsSingleChoicePreference(
                            title = appLanguageTitle,
                            subtitle = appLanguageSubtitle,
                            options = appLanguageOptions,
                            selectedValue = state.appLanguage,
                            onSelectionChange = onAppLanguageChange,
                        )
                    }
                }
            }
        }

        // ========== 高级外观覆盖 ==========
        item(key = "appearance_group_advanced_overrides_title") {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceSectionTitle("高级外观覆盖")
            }
        }
        item(key = AppearanceSettingsGroupKeys.ADVANCED_OVERRIDES) {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceGroup {
                    Column {
                        SettingsSingleChoicePreference(
                            title = "图标样式",
                            subtitle = "主题色容器：图标置于主题色圆角容器内；MD3 官方推荐：onSurfaceVariant 单色图标（全局生效）",
                            options = resolveAppIconStyleOptions(),
                            selectedValue = state.appIconStyle,
                            onSelectionChange = viewModel::setAppIconStyle,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsSingleChoicePreference(
                            title = "列表条目样式",
                            subtitle = "自定义条目：圆角图标容器；原生组件：各预设原生条目（MIUIX/MD3 均可选用）",
                            options = resolveAppListItemStyleOptions(),
                            selectedValue = state.appListItemStyle,
                            onSelectionChange = viewModel::setAppListItemStyle,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsSingleChoicePreference(
                            title = "单选项展示方式",
                            subtitle = "跟随选项弹出与截图一致；也可切回居中弹窗",
                            options = singleChoicePresentationOptions,
                            selectedValue = singleChoicePresentation,
                            onSelectionChange = { presentation ->
                                scope.launch {
                                    SettingsManager.setSingleChoicePresentation(
                                        context = context,
                                        presentation = presentation,
                                    )
                                }
                            },
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.ADVANCED_COLOR),
                            title = "高级配色",
                            subtitle = "分别覆盖明暗模式的背景、文字与控件色",
                            checked = themeRoleOverrides.enabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    SettingsManager.setThemeRoleOverrides(
                                        context,
                                        if (enabled) {
                                            baseThemeRoleOverrides.copy(enabled = true)
                                        } else {
                                            themeRoleOverrides.copy(enabled = false)
                                        }
                                    )
                                }
                            },
                            iconTint = MaterialTheme.colorScheme.primary
                        )

                        AnimatedVisibility(
                            visible = themeRoleOverrides.enabled,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            ThemeRoleOverrideEditor(
                                overrides = themeRoleOverrides,
                                onColorClick = { roleColorTarget = it }
                            )
                        }

                        // D2：恢复主题推荐——用户主动操作，仅清除视觉覆盖字段并恢复 AUTO，
                        // 不改变主题选择、明暗模式、语言、字号等业务偏好；无覆盖时不显示。
                        AnimatedVisibility(
                            visible = hasAppearanceOverrides,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                AppPreferenceDivider()
                                AppPreference(
                                    icon = Icons.Outlined.Restore,
                                    title = "恢复主题推荐",
                                    subtitle = "仅清除图标样式、列表条目、单选呈现与高级配色覆盖，不影响主题、明暗、语言与字体",
                                    showChevron = false,
                                    onClick = {
                                        viewModel.setAppIconStyle(AppIconStyle.AUTO)
                                        viewModel.setAppListItemStyle(AppListItemStyle.AUTO)
                                        scope.launch {
                                            SettingsManager.setSingleChoicePresentation(
                                                context,
                                                AppSingleChoicePresentation.WINDOW_POPUP,
                                            )
                                            SettingsManager.setThemeRoleOverrides(context, ThemeRoleOverrides())
                                        }
                                        Toast.makeText(context, "已恢复主题推荐", Toast.LENGTH_SHORT).show()
                                    },
                                    iconTint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }

        //  启动画面
        item(key = "appearance_group_splash_title") { 
            Box(modifier = Modifier.entrance()) {
                AppPreferenceSectionTitle("启动画面")
            }
        }
        item(key = AppearanceSettingsGroupKeys.SPLASH) {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceGroup {
                    val isSplashEnabled by com.android.purebilibili.core.store.SettingsManager.isSplashEnabled(context).collectAsStateWithLifecycle(initialValue = false)
                    val splashRandomEnabled by com.android.purebilibili.core.store.SettingsManager.getSplashRandomEnabled(context).collectAsStateWithLifecycle(initialValue = false)
                    val splashRandomPoolUris by com.android.purebilibili.core.store.SettingsManager.getSplashRandomPoolUris(context).collectAsStateWithLifecycle(initialValue = emptyList())
                    val splashIconAnimationEnabled by com.android.purebilibili.core.store.SettingsManager.getSplashIconAnimationEnabled(context).collectAsStateWithLifecycle(initialValue = true)
                    val splashWallpaperUri by com.android.purebilibili.core.store.SettingsManager.getSplashWallpaperUri(context).collectAsStateWithLifecycle(initialValue = null)
                    val hasSplashWallpaper = !splashWallpaperUri.isNullOrBlank()
                    val splashRandomPoolPreview = remember(splashRandomPoolUris) {
                        resolveSplashRandomPoolPreviewState(poolUris = splashRandomPoolUris)
                    }
                    
                    // 开关项
	                    AppSwitchPreference(
	                        icon = rememberSettingsSemanticIcon(SettingsIconRole.SPLASH_WALLPAPER),
                        title = "使用开屏壁纸",
                        subtitle = "应用启动时显示官方或相册壁纸",
                        checked = isSplashEnabled,
                        onCheckedChange = { viewModel.toggleSplashEnabled(it) },
                        iconTint = com.android.purebilibili.core.theme.iOSBlue
                    )

                    AppPreferenceDivider()
	                    AppSwitchPreference(
	                        icon = rememberSettingsSemanticIcon(SettingsIconRole.RANDOM_WALLPAPER),
                        title = "随机展示开屏壁纸",
                        subtitle = "启动时从可见官方壁纸中随机展示",
                        checked = splashRandomEnabled,
                        onCheckedChange = { viewModel.toggleSplashRandomEnabled(it) },
                        iconTint = com.android.purebilibili.core.theme.iOSGreen
                    )

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSplashEnabled && splashRandomEnabled,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText(
                                    text = "随机池预览",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                AppText(
                                    text = "${splashRandomPoolPreview.totalCount} 张",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (splashRandomPoolPreview.previewUris.isEmpty()) {
                                AppText(
                                    text = "暂无可见壁纸，请先进入“选择开屏壁纸”加载列表",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    splashRandomPoolPreview.previewUris.forEach { previewUri ->
                                        AsyncImage(
                                            model = coil.request.ImageRequest.Builder(context)
                                                .data(previewUri)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 42.dp, height = 72.dp)
                                                .clip(AppShapes.container(ContainerLevel.Field))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        )
                                    }
                                }
                                if (splashRandomPoolPreview.totalCount > splashRandomPoolPreview.previewUris.size) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AppText(
                                        text = "还有 ${splashRandomPoolPreview.totalCount - splashRandomPoolPreview.previewUris.size} 张",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    AppPreferenceDivider()
	                    AppSwitchPreference(
	                        icon = rememberSettingsSemanticIcon(SettingsIconRole.ANIMATION),
                        title = "开屏图标遮罩动画",
                        subtitle = "关闭后不保留图标页，不播放遮罩和飞出动画",
                        checked = splashIconAnimationEnabled,
                        onCheckedChange = { viewModel.toggleSplashIconAnimationEnabled(it) },
                        iconTint = com.android.purebilibili.core.theme.iOSPink
                    )
                    
                    // 当开启时，显示选择壁纸入口
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSplashEnabled,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Column {
                            AppPreferenceDivider()
                            
                            var showWallpaperPicker by remember { mutableStateOf(false) }
                            
                            // 选择壁纸按钮
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showWallpaperPicker = true }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 壁纸缩略图预览
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(AppShapes.container(ContainerLevel.Field))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    if (hasSplashWallpaper) {
                                        AsyncImage(
                                            model = coil.request.ImageRequest.Builder(context)
                                                .data(splashWallpaperUri)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppIcon(
                                                Icons.Outlined.Photo,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    AppText(
                                        text = "选择开屏壁纸",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    AppText(
                                        text = if (hasSplashWallpaper) "已设置壁纸，可从官方库或相册更换" else "从官方壁纸库或相册选择",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                AppIcon(
                                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            // 壁纸选择 Sheet
                            if (showWallpaperPicker) {
                                com.android.purebilibili.feature.profile.SplashWallpaperPickerSheet(
                                    onDismiss = { showWallpaperPicker = false }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        //  个性化
        item(key = "appearance_group_personalization_title") { 
            Box(modifier = Modifier.entrance()) {
                AppPreferenceSectionTitle("开屏与图标")
            }
        }
        item(key = AppearanceSettingsGroupKeys.PERSONALIZATION) {
            Box(modifier = Modifier.entrance()) {
                AppPreferenceGroup {
                    // 图标设置
	                    AppPreference(
	                        icon = rememberSettingsSemanticIcon(SettingsIconRole.APP_ICON),
                        title = "应用图标",
                        value = when(state.appIcon) {
                            "Blue Snow Maid", "蓝雪女仆", "icon_blue_snow_maid" -> "蓝雪女仆"
                            "Blue Snow Maid Front", "蓝雪女仆·正面", "icon_blue_snow_maid_front" -> "蓝雪女仆·正面"
                            // 🎀 二次元少女系列
                            "BiliPai", "icon_bilipai" -> "BiliPai"
                            "BiliPai Pink", "icon_bilipai_pink" -> "BiliPai 粉"
                            "BiliPai White", "icon_bilipai_white" -> "BiliPai 白"
                            "BiliPai Monet", "icon_bilipai_monet" -> "BiliPai Monet"
                            "Yuki" -> "比心少女"
                            "Anime", "icon_anime" -> "蓝发电视"
                            "Headphone" -> "耳机少女"
                            // 经典系列
                            "3D", "icon_3d" -> "3D立体"
                            "Flat", "icon_flat" -> "扁平现代"
                            "Telegram Blue", "icon_telegram_blue" -> "纸飞机蓝"
                            "Dark", "icon_telegram_dark" -> "暗夜蓝"
                            else -> "蓝雪女仆"
                        },
                        onClick = onNavigateToIconSettings,
                        iconTint = iOSPurple
                    )
                }
            }
        } // End of Personalization item
        }

        if (contentMode == AppearanceSettingsContentMode.HOME) {
            //  首页与列表
            item(key = "appearance_group_home_overview_title") { 
                Box(modifier = Modifier.entrance()) {
                    AppPreferenceSectionTitle("首页与列表")
                }
            }
            item(key = AppearanceSettingsGroupKeys.HOME_OVERVIEW) {
                Box(modifier = Modifier.entrance()) {
                    AppPreferenceGroup {
                        val displayMode = state.displayMode
                        val currentDisplayMode = DisplayMode.entries
                            .firstOrNull { it.value == displayMode }
                        SettingsSingleChoicePreference(
                            title = "展示样式",
                            subtitle = currentDisplayMode?.description ?: "首页视频流布局",
                            options = DisplayMode.entries.map { mode ->
                                AppSegmentOption(mode.value, mode.title)
                            },
                            selectedValue = displayMode,
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.HOME_FEED),
                            iconTint = displayModeTint,
                            onSelectionChange = viewModel::setDisplayMode,
                        )
                        
                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        SettingsSingleChoicePreference(
                            title = "列表顶部栏",
                            subtitle = commonListHeaderCollapseMode.description,
                            options = commonListHeaderCollapseOptions,
                            selectedValue = commonListHeaderCollapseMode,
                            onSelectionChange = { mode ->
                                scope.launch {
                                    SettingsManager.setCommonListHeaderCollapseMode(context, mode)
                                }
                            }
                        )

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.HOME_CARD_STATS_COMPACT),
                            title = "统计信息贴封面（紧凑）",
                            subtitle = if (compactVideoStatsOnCover) {
                                "播放量和评论数显示在封面底部，缩小卡片间距"
                            } else {
                                "播放量和评论数显示在封面外部"
                            },
                            checked = compactVideoStatsOnCover,
                            onCheckedChange = {
                                scope.launch {
                                    SettingsManager.setCompactVideoStatsOnCover(context, it)
                                }
                            },
                            iconTint = iOSTeal
                        )

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.DISPLAY_STYLE),
                            title = "首页顶部轮播封面",
                            subtitle = if (homeHeroCarouselEnabled) {
                                "推荐页顶部显示官方比例的视频封面轮播"
                            } else {
                                "推荐页直接显示普通视频流"
                            },
                            checked = homeHeroCarouselEnabled,
                            onCheckedChange = {
                                scope.launch {
                                    SettingsManager.setHomeHeroCarouselEnabled(context, it)
                                }
                            },
                            iconTint = iOSBlue
                        )

                        AnimatedVisibility(visible = homeHeroCarouselEnabled) {
                            Column {
                                AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                                AppSwitchPreference(
                                    icon = rememberSettingsSemanticIcon(SettingsIconRole.AUTO_PLAY_ON_OPEN),
                                    title = "轮播默认播放",
                                    subtitle = if (homeHeroCarouselAutoplayEnabled) {
                                        "当前轮播项进入视野后静音循环播放"
                                    } else {
                                        "默认只展示封面，点开后进入视频详情"
                                    },
                                    checked = homeHeroCarouselAutoplayEnabled,
                                    onCheckedChange = {
                                        scope.launch {
                                            SettingsManager.setHomeHeroCarouselAutoplayEnabled(context, it)
                                        }
                                    },
                                    iconTint = iOSBlue
                                )
                            }
                        }

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        SettingsSingleChoicePreference(
                            title = "卡片封面比例",
                            subtitle = homeFeedCardStyle.subtitle + "（首页、搜索、列表、相关推荐等同步）",
                            options = HomeFeedCardStyle.entries.map {
                                AppSegmentOption(it, it.label)
                            },
                            selectedValue = homeFeedCardStyle,
                            onSelectionChange = {
                                scope.launch {
                                    SettingsManager.setHomeFeedCardStyle(context, it)
                                }
                            }
                        )
                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        SettingsSingleChoicePreference(
                            title = "首页视频时长",
                            subtitle = "可显示在统计行、仅显示无底色文字或完全隐藏",
                            options = HomeDurationStyle.entries.map {
                                AppSegmentOption(it, it.label)
                            },
                            selectedValue = homeDurationStyle,
                            onSelectionChange = {
                                scope.launch {
                                    SettingsManager.setHomeDurationStyle(context, it)
                                }
                            }
                        )

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        var showHomeWallpaperPicker by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHomeWallpaperPicker = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(AppShapes.container(ContainerLevel.Field))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                if (resolvedHomeWallpaperUri.isNotBlank()) {
                                    AsyncImage(
                                        model = coil.request.ImageRequest.Builder(context)
                                            .data(resolvedHomeWallpaperUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
	                                        AppIcon(
	                                            rememberSettingsSemanticIcon(SettingsIconRole.HOME_WALLPAPER),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                AppText(
                                    text = "选择首页壁纸",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                AppText(
                                    text = when {
                                        dedicatedHomeWallpaperUri.isNotBlank() -> "已单独设置首页壁纸"
                                        homeWallpaperFollowsSplash -> "未单独设置，当前跟随开屏壁纸"
                                        else -> "从官方壁纸库或相册选择"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AppIcon(
                                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (showHomeWallpaperPicker) {
                            com.android.purebilibili.feature.profile.SplashWallpaperPickerSheet(
                                target = com.android.purebilibili.feature.profile.WallpaperPickerTarget.HOME,
                                onDismiss = { showHomeWallpaperPicker = false }
                            )
                        }

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        SettingsSingleChoicePreference(
                            title = "首页壁纸效果",
                            subtitle = when (homeWallpaperEffectMode) {
                                HomeWallpaperEffectMode.OFF -> "首页不使用开屏壁纸作为背景"
                                HomeWallpaperEffectMode.SOFT_BLUR -> "真实壁纸轻微模糊，卡片信息区半透明接入壁纸"
                                HomeWallpaperEffectMode.STRONG_BLUR -> "更强模糊和更稳遮罩，保留壁纸色彩但降低细节干扰"
                                HomeWallpaperEffectMode.ORIGINAL -> "直接接入真实壁纸，文字区使用更轻的保护层"
                            },
                            options = homeWallpaperEffectOptions,
                            selectedValue = homeWallpaperEffectMode,
                            onSelectionChange = { mode ->
                                scope.launch {
                                    SettingsManager.setHomeWallpaperEffectMode(context, mode)
                                }
                            }
                        )

                        AnimatedVisibility(
                            visible = homeWallpaperEffectMode != HomeWallpaperEffectMode.OFF,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                                SettingsSingleChoicePreference(
                                    title = "壁纸作用范围",
                                    subtitle = when (homeWallpaperEffectScope) {
                                        HomeWallpaperEffectScope.HOME_ONLY -> "仅首页使用该壁纸背景效果"
                                        HomeWallpaperEffectScope.GLOBAL -> "全局页面复用同一壁纸背景，默认背景层会半透明保护文字"
                                    },
                                    options = homeWallpaperEffectScopeOptions,
                                    selectedValue = homeWallpaperEffectScope,
                                    onSelectionChange = { scopeValue ->
                                        scope.launch {
                                            SettingsManager.setHomeWallpaperEffectScope(context, scopeValue)
                                        }
                                    }
                                )
                            }
                        }

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.HEADER_COLLAPSE),
                            title = "下滑自动隐藏顶部栏",
                            subtitle = "首页下滑时自动隐藏顶部栏,回顶时重新显示",
                            checked = state.isHeaderCollapseEnabled,
                            onCheckedChange = { value ->
                                viewModel.toggleHeaderCollapse(value)
                            },
                            iconTint = com.android.purebilibili.core.theme.iOSTeal
                        )

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.HOME_UP_BADGES),
                            title = "UP主标识",
                            subtitle = if (homeUpBadgesVisible) {
                                "首页和相关推荐显示 UP 标识"
                            } else {
                                "首页和相关推荐隐藏 UP 标识"
                            },
                            checked = homeUpBadgesVisible,
                            onCheckedChange = {
                                scope.launch {
                                    SettingsManager.setHomeUpBadgesVisible(context, it)
                                }
                            },
                            iconTint = com.android.purebilibili.core.theme.iOSBlue
                        )

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.HOME_UP_AVATAR),
                            title = "UP主头像",
                            subtitle = if (homeUpAvatarsVisible) {
                                "首页视频卡片显示 UP 主头像"
                            } else {
                                "隐藏头像，为 UP 主名称留出更多空间"
                            },
                            checked = homeUpAvatarsVisible,
                            onCheckedChange = {
                                scope.launch {
                                    SettingsManager.setHomeUpAvatarsVisible(context, it)
                                }
                            },
                            iconTint = com.android.purebilibili.core.theme.iOSPurple
                        )

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
	                        AppSwitchPreference(
	                            icon = rememberSettingsSemanticIcon(SettingsIconRole.ONLINE_COUNT),
                            title = "卡片与视频页观看人数",
                            subtitle = if (showOnlineCount) {
                                "首页、搜索等视频卡片和视频页显示“xx人正在看”"
                            } else {
                                "关闭后隐藏卡片和视频页的同时观看人数"
                            },
                            checked = showOnlineCount,
                            onCheckedChange = {
                                scope.launch {
                                    SettingsManager.setShowOnlineCount(context, it)
                                }
                            },
                            iconTint = com.android.purebilibili.core.theme.iOSPurple
                        )
                        
                        // 网格列数设置 (仅在双列网格模式下显示)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isTablet && state.displayMode == 0,
                            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                        ) {
                            Column {
                                AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                                SettingsSingleChoicePreference(
                                    icon = Icons.Outlined.ViewList,
                                    iconTint = com.android.purebilibili.core.theme.iOSBlue,
                                    title = "网格列数",
                                    subtitle = if (state.gridColumnCount == 0) {
                                        "自适应（默认）"
                                    } else {
                                        "固定 ${state.gridColumnCount} 列"
                                    },
                                    options = (0..6).map { count ->
                                        AppSegmentOption(
                                            value = count,
                                            label = if (count == 0) "自动" else "$count 列",
                                        )
                                    },
                                    selectedValue = state.gridColumnCount,
                                    onSelectionChange = viewModel::setGridColumnCount,
                                )
                                AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                                SettingsSingleChoicePreference(
                                    title = "推荐流卡片宽度",
                                    subtitle = if (state.gridColumnCount > 0) {
                                        "当前固定 ${state.gridColumnCount} 列优先生效，自动列数时使用该宽度"
                                    } else {
                                        "自动列数时控制首页推荐卡片的最小宽度"
                                    },
                                    options = resolveHomeFeedCardWidthPresetSegmentOptions(),
                                    selectedValue = state.homeFeedCardWidthPreset,
                                    onSelectionChange = viewModel::setHomeFeedCardWidthPreset,
                                )
                            }
                        }
                    }
                }
            }
        }

    }
    }

    if (showMd3ColorPickerDialog) {
        Md3CustomColorPickerDialog(
            initialHex = state.md3CustomColorHex,
            onDismiss = { showMd3ColorPickerDialog = false },
            onConfirm = { hex ->
                viewModel.applyMd3CustomColor(hex)
                showMd3ColorPickerDialog = false
            }
        )
    }

    roleColorTarget?.let { target ->
        Md3CustomColorPickerDialog(
            initialHex = target.read(themeRoleOverrides),
            onDismiss = { roleColorTarget = null },
            onConfirm = { hex ->
                scope.launch {
                    SettingsManager.setThemeRoleOverrides(
                        context,
                        target.write(themeRoleOverrides, hex)
                    )
                }
                roleColorTarget = null
            }
        )
    }
}

internal enum class ThemeRoleColorTarget(val label: String) {
    LIGHT_BACKGROUND("浅色背景"),
    LIGHT_PRIMARY_TEXT("浅色主要文字"),
    LIGHT_SECONDARY_TEXT("浅色次要文字"),
    LIGHT_CONTROL("浅色控件"),
    DARK_BACKGROUND("深色背景"),
    DARK_PRIMARY_TEXT("深色主要文字"),
    DARK_SECONDARY_TEXT("深色次要文字"),
    DARK_CONTROL("深色控件");

    fun read(overrides: ThemeRoleOverrides): String = when (this) {
        LIGHT_BACKGROUND -> overrides.light.backgroundHex
        LIGHT_PRIMARY_TEXT -> overrides.light.primaryTextHex
        LIGHT_SECONDARY_TEXT -> overrides.light.secondaryTextHex
        LIGHT_CONTROL -> overrides.light.controlAccentHex
        DARK_BACKGROUND -> overrides.dark.backgroundHex
        DARK_PRIMARY_TEXT -> overrides.dark.primaryTextHex
        DARK_SECONDARY_TEXT -> overrides.dark.secondaryTextHex
        DARK_CONTROL -> overrides.dark.controlAccentHex
    }

    fun write(overrides: ThemeRoleOverrides, hex: String): ThemeRoleOverrides {
        return when (this) {
            LIGHT_BACKGROUND -> overrides.copy(light = overrides.light.copy(backgroundHex = hex))
            LIGHT_PRIMARY_TEXT -> overrides.copy(light = overrides.light.copy(primaryTextHex = hex))
            LIGHT_SECONDARY_TEXT -> overrides.copy(light = overrides.light.copy(secondaryTextHex = hex))
            LIGHT_CONTROL -> overrides.copy(light = overrides.light.copy(controlAccentHex = hex))
            DARK_BACKGROUND -> overrides.copy(dark = overrides.dark.copy(backgroundHex = hex))
            DARK_PRIMARY_TEXT -> overrides.copy(dark = overrides.dark.copy(primaryTextHex = hex))
            DARK_SECONDARY_TEXT -> overrides.copy(dark = overrides.dark.copy(secondaryTextHex = hex))
            DARK_CONTROL -> overrides.copy(dark = overrides.dark.copy(controlAccentHex = hex))
        }
    }
}

@Composable
private fun ThemeRoleOverrideEditor(
    overrides: ThemeRoleOverrides,
    onColorClick: (ThemeRoleColorTarget) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        ThemeRoleModeEditor(
            title = "浅色模式",
            roles = overrides.light,
            targets = ThemeRoleColorTarget.entries.take(4),
            onColorClick = onColorClick
        )
        AppPreferenceDivider()
        ThemeRoleModeEditor(
            title = "深色模式",
            roles = overrides.dark,
            targets = ThemeRoleColorTarget.entries.takeLast(4),
            onColorClick = onColorClick
        )
    }
}

@Composable
internal fun ThemeRoleModeEditor(
    title: String,
    roles: ThemeModeRoleOverrides,
    targets: List<ThemeRoleColorTarget>,
    onColorClick: (ThemeRoleColorTarget) -> Unit
) {
    val warning = remember(roles) { hasThemeRoleContrastWarning(roles) }
    val primaryContrast = remember(roles) {
        themeRoleContrastRatio(roles.primaryTextHex, roles.backgroundHex)
    }
    val secondaryContrast = remember(roles) {
        themeRoleContrastRatio(roles.secondaryTextHex, roles.backgroundHex)
    }
    val colors = listOf(
        roles.backgroundHex,
        roles.primaryTextHex,
        roles.secondaryTextHex,
        roles.controlAccentHex
    )
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        AppText(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { hex ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(AppShapes.container(ContainerLevel.Field))
                        .background(parseMd3CustomColorHex(hex))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            AppShapes.container(ContainerLevel.Field)
                        )
                )
            }
        }
        targets.forEachIndexed { index, target ->
            AppPreference(
                icon = rememberSettingsSemanticIcon(SettingsIconRole.DYNAMIC_COLOR),
                title = target.label,
                subtitle = colors[index],
                value = colors[index],
                onClick = { onColorClick(target) },
                iconTint = parseMd3CustomColorHex(colors[index])
            )
        }
        AppText(
            text = "对比度：主要文字 %.2f:1，次要文字 %.2f:1".format(
                primaryContrast,
                secondaryContrast
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (warning) {
            AppText(
                text = "当前文字与背景对比度偏低，仍可按精确颜色保存。",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun Md3CustomColorPickerDialog(
    initialHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val controller = rememberColorPickerController()
    val haptic = rememberHapticFeedback()
    var pendingHex by remember(initialHex) { mutableStateOf(normalizeMd3CustomColorHex(initialHex)) }
    var lastValidHex by remember(initialHex) { mutableStateOf(normalizeMd3CustomColorHex(initialHex)) }
    var lastSelectionHapticAtMs by remember { mutableLongStateOf(0L) }
    val hasValidHex = isValidMd3CustomColorHex(pendingHex)
    val pendingColor = remember(lastValidHex) { parseMd3CustomColorHex(lastValidHex) }
    val invalidInput = pendingHex.isNotBlank() && !hasValidHex
    val sliderPositions = remember(pendingColor) { resolveMd3ColorPickerSliderPositions(pendingColor) }

    fun updatePendingHex(value: String) {
        val nextHex = value.uppercase().take(9)
        pendingHex = nextHex
        if (isValidMd3CustomColorHex(nextHex)) {
            lastValidHex = normalizeMd3CustomColorHex(nextHex)
        }
    }

    // HsvColorPicker only consumes initialColor during setup. Keep its controller in sync with
    // manual HEX edits and presets so the next slider gesture cannot restore the initial blue.
    LaunchedEffect(pendingColor) {
        controller.selectByColor(pendingColor, fromUser = false)
    }

    fun emitSelectionHapticIfNeeded() {
        val nowMs = SystemClock.elapsedRealtime()
        if (shouldEmitMd3ColorPickerSelectionHaptic(lastSelectionHapticAtMs, nowMs)) {
            haptic(HapticType.SELECTION)
            lastSelectionHapticAtMs = nowMs
        }
    }

    AppAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppTextButton(
                enabled = hasValidHex,
                onClick = {
                    haptic(HapticType.LIGHT)
                    onConfirm(lastValidHex)
                }
            ) {
                AppText("保存并应用")
            }
        },
        dismissButton = {
            AppTextButton(
                onClick = {
                    haptic(HapticType.LIGHT)
                    onDismiss()
                }
            ) {
                AppText("取消")
            }
        },
        title = { AppText("自定义 MD3 颜色") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(AppShapes.container(ContainerLevel.Dialog))
                        .background(pendingColor),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = if (hasValidHex) lastValidHex else pendingHex.ifBlank { "#RRGGBB" },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (pendingColor.luminance() < 0.5f) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    controller = controller,
                    initialColor = pendingColor,
                    onStart = { emitSelectionHapticIfNeeded() },
                    onColorChanged = { envelope ->
                        if (envelope.fromUser) {
                            val nextHex = formatMd3CustomColorHex(envelope.color)
                            if (nextHex != pendingHex) {
                                updatePendingHex(nextHex)
                                emitSelectionHapticIfNeeded()
                            }
                        }
                    }
                )

                Md3ColorPickerSliderFrame(position = sliderPositions.hue) {
                    HueSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(resolveMd3ColorPickerSliderLayout().trackHeight),
                        controller = controller,
                        wheelRadius = 0.dp,
                        wheelAlpha = 0f,
                        onStart = { emitSelectionHapticIfNeeded() }
                    )
                }
                Md3ColorPickerSliderFrame(position = sliderPositions.saturation) {
                    SaturationSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(resolveMd3ColorPickerSliderLayout().trackHeight),
                        controller = controller,
                        wheelRadius = 0.dp,
                        wheelAlpha = 0f,
                        onStart = { emitSelectionHapticIfNeeded() }
                    )
                }
                Md3ColorPickerSliderFrame(position = sliderPositions.brightness) {
                    BrightnessSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(resolveMd3ColorPickerSliderLayout().trackHeight),
                        controller = controller,
                        wheelRadius = 0.dp,
                        wheelAlpha = 0f,
                        onStart = { emitSelectionHapticIfNeeded() }
                    )
                }

                AppTextField(
                    value = pendingHex,
                    onValueChange = ::updatePendingHex,
                    label = "HEX（#RRGGBB）",
                    singleLine = true,
                    isError = invalidInput,
                    supportingText = {
                        if (invalidInput) {
                            AppText("请输入 6 位 RGB，例如 #BBCAAE")
                        } else {
                            AppText("点击“保存并应用”后会立即保存，下次启动仍生效")
                        }
                    }
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ThemeColors.size, key = { it }) { index ->
                        val color = ThemeColors[index]
                        val hex = formatMd3CustomColorHex(color)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (normalizeMd3CustomColorHex(pendingHex) == hex) 2.dp else 1.dp,
                                    color = if (normalizeMd3CustomColorHex(pendingHex) == hex) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = CircleShape
                                )
                                .clickable {
                                    updatePendingHex(hex)
                                    haptic(HapticType.SELECTION)
                                }
                        )
                    }
                }
            }
        }
    )
}

internal data class Md3ColorPickerSliderLayout(
    val trackHeight: Dp,
    val frameHeight: Dp,
    val thumbRadius: Dp,
    val horizontalPadding: Dp
)

internal fun resolveMd3ColorPickerSliderLayout(): Md3ColorPickerSliderLayout =
    Md3ColorPickerSliderLayout(
        trackHeight = 28.dp,
        frameHeight = 36.dp,
        thumbRadius = 14.dp,
        horizontalPadding = 14.dp
    )

private const val MD3_COLOR_PICKER_HAPTIC_MIN_INTERVAL_MS = 72L

internal fun shouldEmitMd3ColorPickerSelectionHaptic(
    lastFeedbackAtMs: Long,
    nowMs: Long,
    minIntervalMs: Long = MD3_COLOR_PICKER_HAPTIC_MIN_INTERVAL_MS
): Boolean {
    return lastFeedbackAtMs <= 0L || nowMs - lastFeedbackAtMs >= minIntervalMs
}

private data class Md3ColorPickerSliderPositions(
    val hue: Float,
    val saturation: Float,
    val brightness: Float
)

private fun resolveMd3ColorPickerSliderPositions(color: Color): Md3ColorPickerSliderPositions {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    return Md3ColorPickerSliderPositions(
        hue = (hsv[0] / 360f).coerceIn(0f, 1f),
        saturation = hsv[1].coerceIn(0f, 1f),
        brightness = hsv[2].coerceIn(0f, 1f)
    )
}

@Composable
private fun Md3ColorPickerSliderFrame(
    position: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val layout = resolveMd3ColorPickerSliderLayout()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(layout.frameHeight)
    ) {
        val thumbDiameter = layout.thumbRadius * 2
        val thumbTravelWidth = if (maxWidth > thumbDiameter) maxWidth - thumbDiameter else 0.dp
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = layout.horizontalPadding)
                .fillMaxWidth()
                .height(layout.trackHeight)
        ) {
            content()
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbTravelWidth * position.coerceIn(0f, 1f))
                .size(thumbDiameter)
                .background(Color.White, CircleShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
                    shape = CircleShape
                )
        )
    }
}

/**
 * 按稳定 item key 定位（P3 前置条件：替代固定 LazyColumn 索引表）。
 *
 * 从顶部逐屏探测 key 所在 index 后再动画滚动到位；内容数量有限（约 8-10 个
 * 顶层 item），探测次数有上限兜底。条件项显隐、HOME 内容模式与 840dp 双栏
 * 详情面板下 item key 不变，均定位到同一语义目标。
 */
private suspend fun LazyListState.animateScrollToItemByKey(key: Any) {
    scrollToItem(0)
    repeat(PROBE_PAGE_LIMIT) {
        val found = layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }
        if (found != null) {
            animateScrollToItem(found.index)
            return
        }
        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: return
        val lastIndex = layoutInfo.totalItemsCount - 1
        if (lastVisible.index >= lastIndex) return
        scrollToItem((lastVisible.index + 1).coerceAtMost(lastIndex))
    }
}

private const val PROBE_PAGE_LIMIT = 64

internal fun restartApp(context: android.content.Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
    launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
    (context as? android.app.Activity)?.finishAffinity()
    context.startActivity(launchIntent)
}


/**
 *  动态取色预览组件
 * 显示从壁纸提取的 Material You 颜色
 */


@Composable
fun DynamicColorPreview() {
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AppText(
            text = "当前取色预览",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Primary
            ColorPreviewItem(
                color = colorScheme.primary,
                label = "主色",
                modifier = Modifier.weight(1f)
            )
            // Secondary
            ColorPreviewItem(
                color = colorScheme.secondary,
                label = "辅色",
                modifier = Modifier.weight(1f)
            )
            // Tertiary
            ColorPreviewItem(
                color = colorScheme.tertiary,
                label = "第三色",
                modifier = Modifier.weight(1f)
            )
            // Primary Container
            ColorPreviewItem(
                color = colorScheme.primaryContainer,
                label = "容器",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ColorPreviewItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(AppShapes.container(ContainerLevel.Field))
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        AppText(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun <T> ThemePresetChoiceSetting(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    selectedValue: T,
    options: List<AppSegmentOption<T>>,
    onSelectionChange: (T) -> Unit,
    iconTint: Color
) {
    SettingsSingleChoicePreference(
        title = title,
        options = options,
        selectedValue = selectedValue,
        icon = icon,
        iconTint = iconTint,
        onSelectionChange = onSelectionChange,
    )
}

private const val DEFAULT_APP_DPI_OVERRIDE_PERCENT = 100

private fun resolveAppFontSizeSegmentOptions(): List<AppSegmentOption<AppFontSizePreset>> {
    return AppFontSizePreset.entries.map { preset ->
        AppSegmentOption(value = preset, label = preset.label)
    }
}

private fun resolveAppUiScaleSegmentOptions(): List<AppSegmentOption<AppUiScalePreset>> {
    return AppUiScalePreset.entries.map { preset ->
        AppSegmentOption(value = preset, label = preset.label)
    }
}

private fun resolveAppDpiOverrideSegmentOptions(): List<AppSegmentOption<Int>> {
    return listOf(90, 95, 100, 105, 110).map { percent ->
        AppSegmentOption(value = percent, label = "$percent%")
    }
}

private fun resolveDpiOverrideSubtitle(
    systemDensityDpi: Int,
    systemSmallestWidthDp: Int,
    currentOverridePercent: Int
): String {
    val modeLabel = if (currentOverridePercent > 0) {
        "当前 ${currentOverridePercent}%"
    } else {
        "当前跟随系统"
    }
    return "系统 ${systemDensityDpi}dpi / 最小宽度 ${systemSmallestWidthDp}dp，$modeLabel"
}

private fun resolveDisplayMetricsSummary(
    snapshot: DisplayMetricsSnapshot
): String {
    val dpiSuffix = snapshot.dpiOverridePercent?.let { "，覆盖 ${it}%" } ?: ""
    val narrowSuffix = if (snapshot.isNarrowWidth) "，已进入小屏紧凑适配" else ""
    return "应用生效后约 ${snapshot.effectiveDensityDpi}dpi / ${snapshot.effectiveSmallestWidthDp}dp$dpiSuffix$narrowSuffix"
}

internal fun resolveDisplayedAppDpiPercent(
    currentOverridePercent: Int
): Int {
    return if (currentOverridePercent > 0) {
        currentOverridePercent
    } else {
        DEFAULT_APP_DPI_OVERRIDE_PERCENT
    }
}
