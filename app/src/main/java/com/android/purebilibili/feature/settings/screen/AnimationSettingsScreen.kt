// 文件路径: feature/settings/AnimationSettingsScreen.kt
package com.android.purebilibili.feature.settings
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.core.ui.components.AppSegmentOption
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.R
import com.android.purebilibili.core.theme.*
import com.android.purebilibili.core.ui.blur.BlurIntensity
import com.android.purebilibili.core.ui.blur.shouldAllowHomeChromeLiquidGlass
import com.android.purebilibili.core.store.LiquidGlassMode
import com.android.purebilibili.core.store.AppNavigationSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.adaptive.resolveDeviceUiProfile
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_TRANSITION_CUSTOM_MAX_MILLIS
import com.android.purebilibili.feature.settings.ui.SettingsPageScaffold
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_TRANSITION_CUSTOM_MIN_MILLIS
import com.android.purebilibili.core.ui.transition.VideoSharedTransitionSpeed
import com.android.purebilibili.core.ui.transition.normalizeVideoSharedTransitionCustomDurationMillis
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.feature.home.components.LiquidGlassTuning
import com.android.purebilibili.feature.home.components.resolveLiquidGlassTuning
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackAnimationStyle
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackExitDirection
import androidx.compose.material.icons.outlined.*
import com.android.purebilibili.core.ui.components.*
import com.android.purebilibili.core.ui.animation.EntranceGroup
import com.android.purebilibili.core.ui.animation.entrance
import com.android.purebilibili.core.ui.animation.rememberEffectiveEntranceMotionSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.os.Build
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

/**
 *  动画与效果设置二级页面
 * 管理卡片动画、过渡效果、磨砂效果等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationSettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val screenTitle = stringResource(R.string.animation_effects_title)
    val backLabel = stringResource(R.string.common_back)
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    SettingsPageScaffold(
        title = screenTitle,
        onBack = onBack,
        backContentDescription = backLabel,
        bottomContentPadding = bottomContentPadding,
        scrollHost = SettingsPageScrollHost.External,
        topBarBlurEnabled = state.headerBlurEnabled,
    ) {
        CompositionLocalProvider(LocalSettingsLiquidGlassEnabled provides state.isLiquidGlassEnabled) {
            AnimationSettingsContent(
                state = state,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
fun AnimationSettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val focusRequest by SettingsSearchFocusController.request.collectAsStateWithLifecycle()
    val windowSizeClass = LocalWindowSizeClass.current
    val warningTint = rememberAdaptiveSemanticIconTint(iOSOrange)
    val deviceUiProfile = remember(windowSizeClass.widthSizeClass) {
        resolveDeviceUiProfile(
            widthSizeClass = windowSizeClass.widthSizeClass
        )
    }
    val cardMotionTier = resolveAnimationSettingsCardMotionTier(
        baseTier = deviceUiProfile.motionTier,
        cardAnimationEnabled = state.cardAnimationEnabled
    )
    val motionTierLabel = remember(cardMotionTier) {
        when (cardMotionTier) {
            MotionTier.Reduced -> "低动效"
            MotionTier.Normal -> "标准"
            MotionTier.Enhanced -> "增强"
        }
    }
    val motionTierHint = remember(cardMotionTier) {
        when (cardMotionTier) {
            MotionTier.Reduced -> "更短延迟与更弱位移，优先稳定和性能"
            MotionTier.Normal -> "平衡性能与动效，适合大多数设备"
            MotionTier.Enhanced -> "更明显的层级与动势，适合大屏展示"
        }
    }
    val isLiquidGlassAvailable = shouldAllowHomeChromeLiquidGlass(Build.VERSION.SDK_INT)
    val bottomBarLiquidGlassEnabled = state.bottomBarLiquidGlassEnabled
    val uiEntranceAnimationEnabled by SettingsManager.getUiEntranceAnimationEnabled(context)
        .collectAsStateWithLifecycle(initialValue = true)
    val appNavigationSettings by SettingsManager.getAppNavigationSettings(context)
        .collectAsStateWithLifecycle(initialValue = AppNavigationSettings())
    val videoTransitionRealtimeBlurEnabled by SettingsManager
        .getVideoTransitionRealtimeBlurEnabled(context)
        .collectAsStateWithLifecycle(initialValue = false)
    val liveSurfaceCardTransitionEnabled by SettingsManager
        .getLiveSurfaceCardTransitionEnabled(context)
        .collectAsStateWithLifecycle(initialValue = false)
    val fullScreenSwipeBackEnabled by SettingsManager
        .getFullScreenSwipeBackEnabled(context)
        .collectAsStateWithLifecycle(initialValue = false)
    val effectiveEntranceSpec = rememberEffectiveEntranceMotionSpec()
    // 开关开着、但有效参数被降级为不动画 → 系统减弱动效在生效。
    val entranceDowngradedBySystem = uiEntranceAnimationEnabled && !effectiveEntranceSpec.animate
    val sharedTransitionSpeedOptions = remember {
        listOf(
            AppSegmentOption(VideoSharedTransitionSpeed.FAST, "快速"),
            AppSegmentOption(VideoSharedTransitionSpeed.STANDARD, "标准"),
            AppSegmentOption(VideoSharedTransitionSpeed.SLOW, "慢速"),
            AppSegmentOption(VideoSharedTransitionSpeed.CUSTOM, "自定")
        )
    }
    val predictiveBackStyle = remember(appNavigationSettings) {
        if (appNavigationSettings.predictiveBackEnabled) {
            BiliPaiPredictiveBackAnimationStyle.fromStorageValue(
                appNavigationSettings.predictiveBackAnimationStyle
            )
        } else {
            BiliPaiPredictiveBackAnimationStyle.NONE
        }
    }
    val predictiveBackStyleOptions = remember {
        listOf(
            AppSegmentOption(BiliPaiPredictiveBackAnimationStyle.NONE, "无"),
            AppSegmentOption(BiliPaiPredictiveBackAnimationStyle.AOSP, "AOSP"),
            AppSegmentOption(BiliPaiPredictiveBackAnimationStyle.MIUIX, "Miuix"),
            AppSegmentOption(BiliPaiPredictiveBackAnimationStyle.SCALE, "缩放"),
            AppSegmentOption(BiliPaiPredictiveBackAnimationStyle.CLASSIC, "经典"),
        )
    }
    val predictiveBackExitDirection = remember(appNavigationSettings.predictiveBackExitDirection) {
        BiliPaiPredictiveBackExitDirection.fromStorageValue(
            appNavigationSettings.predictiveBackExitDirection
        )
    }
    val predictiveBackExitDirectionOptions = remember {
        listOf(
            AppSegmentOption(BiliPaiPredictiveBackExitDirection.FOLLOW_GESTURE, "跟随手势"),
            AppSegmentOption(BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT, "始终向右"),
            AppSegmentOption(BiliPaiPredictiveBackExitDirection.ALWAYS_LEFT, "始终向左"),
        )
    }
    var customTransitionDurationMillis by remember(state.videoSharedTransitionCustomDurationMillis) {
        mutableIntStateOf(state.videoSharedTransitionCustomDurationMillis)
    }
    fun snapCustomTransitionDuration(value: Float): Int {
        val stepMillis = 20
        val min = VIDEO_SHARED_TRANSITION_CUSTOM_MIN_MILLIS
        val snapped = min + (((value - min) / stepMillis).roundToInt() * stepMillis)
        return normalizeVideoSharedTransitionCustomDurationMillis(snapped)
    }
    LaunchedEffect(focusRequest?.token) {
        val request = focusRequest ?: return@LaunchedEffect
        if (request.target != SettingsSearchTarget.ANIMATION) return@LaunchedEffect
        val index = resolveAnimationSettingsScrollIndex(request.focusId) ?: return@LaunchedEffect
        listState.animateScrollToItem(index)
        SettingsSearchFocusController.clear(request.token)
    }

    EntranceGroup {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {

            //  界面动效（全 App 入场）
            item {
                Box(modifier = Modifier.entrance()) {
                    AppPreferenceSectionTitle("界面动效")
                }
            }
            item {
                Box(modifier = Modifier.entrance()) {
                    AppPreferenceGroup {
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.CARD_ENTRANCE_ANIMATION),
                            title = "界面入场动画",
                            subtitle = "进入设置等页面时，让内容依次淡入；关闭后页面会直接显示",
                            checked = uiEntranceAnimationEnabled,
                            onCheckedChange = { value ->
                                scope.launch {
                                    SettingsManager.setUiEntranceAnimationEnabled(context, value)
                                }
                            },
                            iconTint = iOSGreen
                        )
                        AppPreferenceDivider()
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.FULLSCREEN_GESTURE),
                            title = "触感反馈",
                            subtitle = "为导航、切换与关键操作提供触感反馈",
                            checked = state.hapticFeedbackEnabled,
                            onCheckedChange = viewModel::toggleHapticFeedback,
                            iconTint = iOSBlue,
                        )
                        if (entranceDowngradedBySystem) {
                            AppPreferenceDivider()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                AppText(
                                    text = "系统已开启「减弱动效」，入场动画已自动关闭。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            //  卡片动画
            item {
                Box(modifier = Modifier.entrance()) {
                    AppPreferenceSectionTitle("卡片动画")
                }
            }
            item {
                Box(modifier = Modifier.entrance()) {
                    AppPreferenceGroup {
	                        AppSwitchPreference(
	                            icon = rememberSettingsSemanticIcon(SettingsIconRole.CARD_ENTRANCE_ANIMATION),
                            title = "进场动画",
                            subtitle = "打开首页时让首屏卡片依次淡入，正常滚动时不会重复播放",
                            checked = state.cardAnimationEnabled,
                            onCheckedChange = { viewModel.toggleCardAnimation(it) },
                            iconTint = iOSPink
                        )
                        AppPreferenceDivider()
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.CARD_TRANSITION_ANIMATION),
                            title = "过渡动画",
                            subtitle = "点击视频卡片时，让封面和标题自然移动到详情页",
                            checked = state.cardTransitionEnabled,
                            onCheckedChange = { viewModel.toggleCardTransition(it) },
                            iconTint = iOSTeal
                        )
                        AppPreferenceDivider()
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.LIVE_SURFACE_TRANSITION),
                            title = "实时画面转场",
                            subtitle = "进出详情用播放器当前画面做双向变形；HDR/杜比仍走高质量输出，不降画质",
                            checked = liveSurfaceCardTransitionEnabled,
                            onCheckedChange = { viewModel.toggleLiveSurfaceCardTransition(it) },
                            enabled = state.cardTransitionEnabled,
                            iconTint = iOSTeal
                        )
                        AppPreferenceDivider()
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.WALLPAPER_EFFECT),
                            title = "转场时模糊背景",
                            subtitle = "让视频转场更有层次；关闭可减少性能和耗电开销",
                            checked = videoTransitionRealtimeBlurEnabled,
                            onCheckedChange = { viewModel.toggleVideoTransitionRealtimeBlur(it) },
                            iconTint = iOSTeal
                        )
                        AppPreferenceDivider()
                        SettingsSingleChoicePreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.PREDICTIVE_BACK),
                            title = "全局返回动画",
                            subtitle = "普通返回与预测性返回统一使用 Miuix 导航动画",
                            options = predictiveBackStyleOptions,
                            selectedValue = predictiveBackStyle,
                            onSelectionChange = { style ->
                                scope.launch {
                                    SettingsManager.setPredictiveBackEnabled(context, true)
                                    SettingsManager.setPredictiveBackAnimationStyle(
                                        context,
                                        style.storageValue,
                                    )
                                }
                            },
                            iconTint = iOSTeal
                        )
                        if (predictiveBackStyle == BiliPaiPredictiveBackAnimationStyle.SCALE) {
                            AppPreferenceDivider()
                            SettingsSingleChoicePreference(
                                title = "缩放退出方向",
                                subtitle = "仅缩放样式使用",
                                options = predictiveBackExitDirectionOptions,
                                selectedValue = predictiveBackExitDirection,
                                onSelectionChange = { direction ->
                                    scope.launch {
                                        SettingsManager.setPredictiveBackExitDirection(
                                            context,
                                            direction.storageValue,
                                        )
                                    }
                                },
                            )
                        }
                        AppPreferenceDivider()
                        AppSwitchPreference(
                            icon = rememberSettingsSemanticIcon(SettingsIconRole.PREDICTIVE_BACK),
                            title = "全屏滑动返回",
                            subtitle = if (fullScreenSwipeBackEnabled) {
                                "列表与设置页支持全屏右滑返回；播放器、详情与网页页不受影响"
                            } else {
                                "仅屏幕边缘系统手势触发返回"
                            },
                            checked = fullScreenSwipeBackEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    SettingsManager.setFullScreenSwipeBackEnabled(context, enabled)
                                }
                            },
                            iconTint = iOSTeal
                        )
                        AppPreferenceDivider()
                        SettingsSingleChoicePreference(
                            title = "视频转场速度：${state.videoSharedTransitionSpeed.label}",
                            subtitle = "选择封面进入详情页和返回卡片时的动画速度",
                            options = sharedTransitionSpeedOptions,
                            selectedValue = state.videoSharedTransitionSpeed,
                            onSelectionChange = viewModel::setVideoSharedTransitionSpeed
                        )
                        if (state.videoSharedTransitionSpeed == VideoSharedTransitionSpeed.CUSTOM) {
                            AppPreferenceDivider()
                            AppSliderDialogPreference(
                                title = "自定义时长",
                                subtitle = "数值越大，视频转场越慢",
                                value = customTransitionDurationMillis.toFloat(),
                                onValueChange = { value ->
                                    val snappedValue = snapCustomTransitionDuration(value)
                                    customTransitionDurationMillis = snappedValue
                                    viewModel.setVideoSharedTransitionCustomDurationMillis(snappedValue)
                                },
                                valueRange = VIDEO_SHARED_TRANSITION_CUSTOM_MIN_MILLIS.toFloat()..
                                    VIDEO_SHARED_TRANSITION_CUSTOM_MAX_MILLIS.toFloat(),
                                steps = (
                                    (VIDEO_SHARED_TRANSITION_CUSTOM_MAX_MILLIS -
                                        VIDEO_SHARED_TRANSITION_CUSTOM_MIN_MILLIS) / 20
                                    ) - 1,
                                valueFormatter = { value -> "${value.roundToInt()}ms" },
                            )
                        }
                        AppPreferenceDivider()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            AppText(
                                text = "首页卡片动画档位",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AppText(
                                text = motionTierLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            AppText(
                                text = motionTierHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AppText(
                                text = "设置页使用独立轻量入场动效，不跟随此开关关闭。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ✨ 视觉效果
            item {
                Box(modifier = Modifier.entrance()) {
                    AppPreferenceSectionTitle("玻璃效果")
                }
            }
            item {
                Box(modifier = Modifier.entrance()) {
                    AppPreferenceGroup {
                        if (isLiquidGlassAvailable) {
                            AppSwitchPreference(
                                icon = rememberSettingsSemanticIcon(SettingsIconRole.TOP_DOCK_GLASS),
                                title = "顶部标签栏液态玻璃",
                                subtitle = "为首页顶部的搜索框和标签栏增加折射与滑动效果",
                                checked = state.topBarLiquidGlassEnabled,
                                onCheckedChange = { viewModel.toggleTopBarLiquidGlass(it) },
                                iconTint = iOSBlue
                            )
                            AppPreferenceDivider()
                            AppSwitchPreference(
                                icon = rememberSettingsSemanticIcon(SettingsIconRole.HOME_SEARCH_GLASS),
                                title = "首页搜索框液态玻璃",
                                subtitle = "首页搜索框上下滑动时的液态玻璃折射效果",
                                checked = state.homeSearchLiquidGlassEnabled,
                                onCheckedChange = { viewModel.toggleHomeSearchLiquidGlass(it) },
                                iconTint = iOSBlue
                            )
                            AppPreferenceDivider()
                            AppSwitchPreference(
                                icon = rememberSettingsSemanticIcon(SettingsIconRole.BOTTOM_BAR_GLASS),
                                title = "底栏液态玻璃",
                                subtitle = "底部导航栏的液态玻璃折射效果",
                                checked = bottomBarLiquidGlassEnabled,
                                onCheckedChange = { viewModel.toggleBottomBarLiquidGlass(it) },
                                iconTint = iOSBlue
                            )
                            androidx.compose.animation.AnimatedVisibility(
                                visible = bottomBarLiquidGlassEnabled,
                                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                            ) {
                                Column {
                                    AppPreferenceDivider()
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        AppText(
                                            "当前使用固定材质策略",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        AppText(
                                            text = "开启全局液态玻璃后，顶部栏、搜索框、选择控件和底栏会统一使用同一套玻璃效果。",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            AppPreferenceDivider()
                        }
                        // 磨砂效果 (始终显示)
	                        AppSwitchPreference(
	                            icon = rememberSettingsSemanticIcon(SettingsIconRole.TOP_BAR_BLUR),
                            title = "顶部栏磨砂",
                            subtitle = "顶部导航栏的毛玻璃模糊效果",
                            checked = state.headerBlurEnabled,
                            onCheckedChange = { viewModel.toggleHeaderBlur(it) },
                            iconTint = iOSBlue
                        )
                        AppPreferenceDivider()
	                        AppSwitchPreference(
	                            icon = rememberSettingsSemanticIcon(SettingsIconRole.BOTTOM_BAR_BLUR),
                            title = "底栏磨砂",
                            subtitle = "底部导航栏的毛玻璃模糊效果",
                            checked = state.bottomBarBlurEnabled,
                            onCheckedChange = { viewModel.toggleBottomBarBlur(it) },
                            iconTint = iOSBlue
                        )
                        
                        // 模糊强度（仅在任意模糊开启时显示）
                        if (state.headerBlurEnabled || state.bottomBarBlurEnabled) {
                            AppPreferenceDivider()
                            BlurIntensitySelector(
                                selectedIntensity = state.blurIntensity,
                                onIntensityChange = { viewModel.setBlurIntensity(it) }
                            )
                        }
                    }
                }
            }
            
            //  提示
            item {
                Box(modifier = Modifier.entrance()) {
                    AppSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = AppShapes.container(ContainerLevel.Card),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(
                                Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = warningTint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            AppText(
                                text = "关闭动画可以减少电量消耗，提升流畅度",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
