@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.store.theme.AppLanguage
import com.android.purebilibili.core.store.theme.AppThemeMode
import com.android.purebilibili.core.store.theme.DarkThemeStyle
import com.android.purebilibili.core.store.theme.persistAndApplyAppLanguageBeforeRestart
import com.android.purebilibili.core.store.theme.resolveAppLanguageLocaleTags
import com.android.purebilibili.core.store.theme.shouldPromptAppRestartForLanguageChange
import com.android.purebilibili.core.ui.AppListItemStyle
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.core.ui.components.AppSegmentOption

import android.os.Build
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
import androidx.compose.animation.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
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
import coil.compose.AsyncImage
import com.android.purebilibili.core.theme.deleteStoredAppFont
import com.android.purebilibili.core.theme.importAppFontFromUri
import com.android.purebilibili.core.theme.*
import com.android.purebilibili.core.ui.adaptive.resolveDeviceUiProfile
import com.android.purebilibili.core.ui.adaptive.resolveEffectiveMotionTier
import com.android.purebilibili.core.ui.blur.BlurIntensity
import com.android.purebilibili.core.ui.getWindowNavigationBarColor
import com.android.purebilibili.core.ui.rememberAppSparklesIcon
import com.android.purebilibili.core.ui.setWindowNavigationBarColor
import com.android.purebilibili.feature.settings.ui.SettingsPageScaffold
import com.android.purebilibili.core.util.LocalWindowSizeClass
import kotlinx.coroutines.launch
import com.android.purebilibili.core.ui.components.*
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
    val appearanceAnimationSpeed = 1f
    
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
        AppearanceSettingsContent(
            state = state,
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
                                apply = ::applyAppLanguage,
                                appLanguage = pendingLanguage,
                                persist = viewModel::setAppLanguage,
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
    contentMode: AppearanceSettingsContentMode,
    viewModel: SettingsViewModel,
    context: android.content.Context,
    onAppLanguageChange: (AppLanguage) -> Unit
) {
    // 阶段 4 切片：外观页直读收拢到 SettingsViewModel.state
    val singleChoicePresentation = state.singleChoicePresentation
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
    val themeModeTitle = stringResource(R.string.appearance_theme_mode_title)
    val themeModeSubtitle = stringResource(R.string.appearance_theme_mode_subtitle)
    val themeModeFollowSystemLabel = stringResource(R.string.theme_mode_follow_system)
    val themeModeLightLabel = stringResource(R.string.theme_mode_light)
    val themeModeDarkLabel = stringResource(R.string.theme_mode_dark)
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
    val selectedThemeModeLabel = remember(
        state.themeMode,
        themeModeFollowSystemLabel,
        themeModeLightLabel,
        themeModeDarkLabel
    ) {
        when (state.themeMode) {
            AppThemeMode.FOLLOW_SYSTEM -> themeModeFollowSystemLabel
            AppThemeMode.LIGHT -> themeModeLightLabel
            AppThemeMode.DARK -> themeModeDarkLabel
        }
    }
    val darkThemeStyleTitle = stringResource(R.string.appearance_dark_theme_style_title)
    val darkThemeStyleSubtitle = stringResource(R.string.appearance_dark_theme_style_subtitle)
    val darkThemeStyleDefaultLabel = stringResource(R.string.dark_theme_style_default)
    val darkThemeStyleAmoledLabel = stringResource(R.string.dark_theme_style_amoled)
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
    val selectedDarkThemeStyleLabel = remember(
        state.darkThemeStyle,
        darkThemeStyleDefaultLabel,
        darkThemeStyleAmoledLabel
    ) {
        when (state.darkThemeStyle) {
            DarkThemeStyle.DEFAULT -> darkThemeStyleDefaultLabel
            DarkThemeStyle.AMOLED -> darkThemeStyleAmoledLabel
        }
    }
    val appLanguageTitle = stringResource(R.string.appearance_app_language_title)
    val appLanguageSubtitle = stringResource(R.string.appearance_app_language_subtitle)
    val appLanguageFollowSystemLabel = stringResource(R.string.app_language_follow_system)
    val appLanguageSimplifiedLabel = stringResource(R.string.app_language_simplified_chinese)
    val appLanguageTraditionalLabel = stringResource(R.string.app_language_traditional_chinese)
    val appLanguageEnglishLabel = stringResource(R.string.app_language_english)
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
    val selectedAppLanguageLabel = remember(
        state.appLanguage,
        appLanguageFollowSystemLabel,
        appLanguageSimplifiedLabel,
        appLanguageTraditionalLabel,
        appLanguageEnglishLabel
    ) {
        when (state.appLanguage) {
            AppLanguage.FOLLOW_SYSTEM -> appLanguageFollowSystemLabel
            AppLanguage.SIMPLIFIED_CHINESE -> appLanguageSimplifiedLabel
            AppLanguage.TRADITIONAL_CHINESE_TAIWAN -> appLanguageTraditionalLabel
            AppLanguage.ENGLISH -> appLanguageEnglishLabel
        }
    }
    val navigationBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentBottomPadding = resolveAppearanceBottomPadding(
        navigationBarsBottom = navigationBarBottomPadding,
        expandableSectionEnabled = true
    )
    val compactVideoStatsOnCover = state.compactVideoStatsOnCover
    val dedicatedHomeWallpaperUri = state.homeWallpaperUri
    val splashWallpaperFallbackUri = state.splashWallpaperUri
    val resolvedHomeWallpaperUri = remember(dedicatedHomeWallpaperUri, splashWallpaperFallbackUri) {
        dedicatedHomeWallpaperUri.ifBlank { splashWallpaperFallbackUri }.trim()
    }
    val homeWallpaperFollowsSplash = dedicatedHomeWallpaperUri.isBlank() && splashWallpaperFallbackUri.isNotBlank()
    val homeWallpaperEffectMode = state.homeWallpaperEffectMode
    val homeWallpaperEffectScope = state.homeWallpaperEffectScope
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
    val homeUpBadgesVisible = state.homeUpBadgesVisible
    val homeUpAvatarsVisible = state.homeUpAvatarsVisible
    val homeDurationStyle = state.homeDurationStyle
    val homeFeedCardStyle = state.homeFeedCardStyle
    val homeHeroCarouselEnabled = state.homeHeroCarouselEnabled
    val homeHeroCarouselAutoplayEnabled = state.homeHeroCarouselAutoplayEnabled
    val commonListHeaderCollapseMode = state.commonListHeaderCollapseMode
    val commonListHeaderCollapseOptions = remember {
        CommonListHeaderCollapseMode.entries.map { mode ->
            AppSegmentOption(mode, mode.label)
        }
    }
    val showOnlineCount = state.showOnlineCount
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

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize(),
        // [Fix] 为可展开配置项增加安全底部留白，避免“小屏+展开”时显示不全
        contentPadding = PaddingValues(bottom = contentBottomPadding)
    ) {
        if (contentMode == AppearanceSettingsContentMode.APPEARANCE) {

        //  主题与颜色
        item(key = AppearanceSettingsGroupKeys.UI_AND_DARK) { 
            Box(modifier = Modifier) {
                AppPreferenceSectionTitle("显示模式")
            }
        }
        item {
            Box(modifier = Modifier) {
                AppPreferenceGroup {
                    // 主题模式选择 (横向卡片)
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingsSingleChoicePreference(
                            title = "${themeModeTitle}：$selectedThemeModeLabel",
                            subtitle = themeModeSubtitle,
                            options = themeModeOptions,
                            selectedValue = state.themeMode,
                            onSelectionChange = { mode ->
                                viewModel.setThemeMode(mode)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsSingleChoicePreference(
                            title = "列表条目样式",
                            subtitle = "自定义条目：圆角图标容器；原生组件：Miuix 原生条目",
                            options = resolveAppListItemStyleOptions(),
                            selectedValue = state.appListItemStyle,
                            onSelectionChange = { style ->
                                viewModel.setAppListItemStyle(style)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsSingleChoicePreference(
                            title = "单选项展示方式",
                            subtitle = "跟随选项弹出与截图一致；也可切回居中弹窗",
                            options = singleChoicePresentationOptions,
                            selectedValue = singleChoicePresentation,
                            onSelectionChange = viewModel::setSingleChoicePresentation,
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
                                    title = "${darkThemeStyleTitle}：$selectedDarkThemeStyleLabel",
                                    subtitle = darkThemeStyleSubtitle,
                                    options = darkThemeStyleOptions,
                                    selectedValue = state.darkThemeStyle,
                                    onSelectionChange = { style ->
                                        viewModel.setDarkThemeStyle(style)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsSingleChoicePreference(
                            title = "${appLanguageTitle}：$selectedAppLanguageLabel",
                            subtitle = appLanguageSubtitle,
                            options = appLanguageOptions,
                            selectedValue = state.appLanguage,
                            onSelectionChange = { language ->
                                onAppLanguageChange(language)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                    }
                }
            }
        }

        item(key = AppearanceSettingsGroupKeys.TEXT_AND_DISPLAY) {
            Box(modifier = Modifier) {
                AppPreferenceSectionTitle("字体与密度")
            }
        }
        item {
            Box(modifier = Modifier) {
                AppPreferenceGroup {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingsSingleChoicePreference(
                            title = "字体大小：${state.appFontSizePreset.label}",
                            subtitle = "仅调整应用内文字比例",
                            options = resolveAppFontSizeSegmentOptions(),
                            selectedValue = state.appFontSizePreset,
                            onSelectionChange = { preset ->
                                viewModel.setAppFontSizePreset(preset)
                            }
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
                            title = "界面缩放：${state.appUiScalePreset.label}",
                            subtitle = "调整列表、卡片与控件的整体密度",
                            options = resolveAppUiScaleSegmentOptions(),
                            selectedValue = state.appUiScalePreset,
                            onSelectionChange = { preset ->
                                viewModel.setAppUiScalePreset(preset)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AppPreferenceDivider()
                        Spacer(modifier = Modifier.height(8.dp))

	                        AppSwitchPreference(
	                            icon = rememberSettingsSemanticIcon(SettingsIconRole.DISPLAY_SCALE),
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
                                    title = "显示缩放：${resolveDisplayedAppDpiPercent(state.appDpiOverridePercent)}%",
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
                    }
                }
            }
        }
        
        //  启动画面
        item(key = AppearanceSettingsGroupKeys.SPLASH) { 
            Box(modifier = Modifier) {
                AppPreferenceSectionTitle("启动画面")
            }
        }
        item {
            Box(modifier = Modifier) {
                AppPreferenceGroup {
                    // 阶段 4 切片：splash 状态收拢到 VM
                    val isSplashEnabled = state.splashEnabled
                    val splashRandomEnabled = state.splashRandomEnabled
                    val splashRandomPoolUris = state.splashRandomPoolUris
                    val splashIconAnimationEnabled = state.splashIconAnimationEnabled
                    val splashWallpaperUri = state.splashWallpaperUri.takeIf { it.isNotBlank() }
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
        
        }
        
        if (contentMode == AppearanceSettingsContentMode.HOME) {
            //  首页与列表
            item(key = AppearanceSettingsGroupKeys.HOME_OVERVIEW) { 
                Box(modifier = Modifier) {
                    AppPreferenceSectionTitle("首页与列表")
                }
            }
            item {
                Box(modifier = Modifier) {
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
                            title = "列表顶部栏：${commonListHeaderCollapseMode.label}",
                            subtitle = commonListHeaderCollapseMode.description,
                            options = commonListHeaderCollapseOptions,
                            selectedValue = commonListHeaderCollapseMode,
                            onSelectionChange = viewModel::setCommonListHeaderCollapseMode
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
                            onCheckedChange = viewModel::setCompactVideoStatsOnCover,
                            iconTint = iOSTeal
                        )

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.HOME_HERO_CAROUSEL),
                            title = "首页顶部轮播封面",
                            subtitle = if (homeHeroCarouselEnabled) {
                                "推荐页顶部显示官方比例的视频封面轮播"
                            } else {
                                "推荐页直接显示普通视频流"
                            },
                            checked = homeHeroCarouselEnabled,
                            onCheckedChange = viewModel::setHomeHeroCarouselEnabled,
                            iconTint = iOSBlue
                        )

                        AnimatedVisibility(visible = homeHeroCarouselEnabled) {
                            Column {
                                AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                                AppSwitchPreference(
                                    icon = rememberSettingsSemanticIcon(SettingsIconRole.HOME_HERO_AUTOPLAY),
                                    title = "轮播默认播放",
                                    subtitle = if (homeHeroCarouselAutoplayEnabled) {
                                        "当前轮播项进入视野后静音循环播放"
                                    } else {
                                        "默认只展示封面，点开后进入视频详情"
                                    },
                                    checked = homeHeroCarouselAutoplayEnabled,
                                    onCheckedChange = viewModel::setHomeHeroCarouselAutoplayEnabled,
                                    iconTint = iOSBlue
                                )
                            }
                        }

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        SettingsSingleChoicePreference(
                            title = "卡片封面比例：${homeFeedCardStyle.label}",
                            subtitle = homeFeedCardStyle.subtitle + "（首页、搜索、列表、相关推荐等同步）",
                            options = HomeFeedCardStyle.entries.map {
                                AppSegmentOption(it, it.label)
                            },
                            selectedValue = homeFeedCardStyle,
                            onSelectionChange = viewModel::setHomeFeedCardStyle
                        )
                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
                        SettingsSingleChoicePreference(
                            title = "首页视频时长：${homeDurationStyle.label}",
                            subtitle = "可显示在统计行、仅显示无底色文字或完全隐藏",
                            options = HomeDurationStyle.entries.map {
                                AppSegmentOption(it, it.label)
                            },
                            selectedValue = homeDurationStyle,
                            onSelectionChange = viewModel::setHomeDurationStyle
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
                            onSelectionChange = viewModel::setHomeWallpaperEffectMode
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
                                    onSelectionChange = viewModel::setHomeWallpaperEffectScope
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
                            onCheckedChange = viewModel::setHomeUpBadgesVisible,
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
                            onCheckedChange = viewModel::setHomeUpAvatarsVisible,
                            iconTint = com.android.purebilibili.core.theme.iOSPurple
                        )

                        AppPreferenceDivider(modifier = Modifier.padding(start = 16.dp))
	                        AppSwitchPreference(
	                            icon = rememberSettingsSemanticIcon(SettingsIconRole.HOME_ONLINE_COUNT),
                            title = "卡片与视频页观看人数",
                            subtitle = if (showOnlineCount) {
                                "首页、搜索等视频卡片和视频页显示“xx人正在看”"
                            } else {
                                "关闭后隐藏卡片和视频页的同时观看人数"
                            },
                            checked = showOnlineCount,
                            onCheckedChange = viewModel::setShowOnlineCount,
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

internal fun restartApp(context: android.content.Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
    launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
    (context as? android.app.Activity)?.finishAffinity()
    context.startActivity(launchIntent)
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

private const val APPEARANCE_FOCUS_PROBE_PAGE_LIMIT = 64

/**
 * 按稳定 item key 定位滚动。搜索定位只依赖外观页组 key，与组顺序、
 * 条件项显隐和 840dp 双栏详情面板无关（详情面板下 key 不变）。
 */
private suspend fun LazyListState.animateScrollToItemByKey(key: Any) {
    scrollToItem(0)
    repeat(APPEARANCE_FOCUS_PROBE_PAGE_LIMIT) {
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
