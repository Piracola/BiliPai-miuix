// 文件路径: app/src/main/java/com/android/purebilibili/MainActivity.kt
package com.android.purebilibili

import android.animation.ValueAnimator
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Rational
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Density
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.metrics.performance.JankStats
import androidx.window.layout.WindowMetrics
import androidx.window.layout.WindowMetricsCalculator
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.coroutines.AppScope
import com.android.purebilibili.core.theme.BiliPink
import com.android.purebilibili.core.theme.LocalDisplayMetricsSnapshot
import com.android.purebilibili.core.theme.PureBiliBiliTheme
import com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState
import com.android.purebilibili.core.ui.motion.AppMotionEasing
import com.android.purebilibili.core.ui.performance.AppRuntimeVisualGuardTracker
import com.android.purebilibili.core.ui.wallpaper.SplashWallpaperLayout
import com.android.purebilibili.core.ui.wallpaper.resolveSplashWallpaperLayout
import com.android.purebilibili.core.util.BilibiliNavigationTarget
import com.android.purebilibili.core.util.BilibiliNavigationTargetParser
import com.android.purebilibili.core.util.WindowWidthSizeClass
import com.android.purebilibili.core.util.Logger
import com.android.purebilibili.feature.plugin.EyeProtectionOverlay
import com.android.purebilibili.feature.settings.AppUpdateAutoCheckGate
import com.android.purebilibili.feature.settings.AppUpdateCheckResult
import com.android.purebilibili.feature.settings.AppUpdateChecker
import com.android.purebilibili.feature.settings.AppUpdateDialogHost
import com.android.purebilibili.feature.settings.AppUpdateDownloadState
import com.android.purebilibili.feature.settings.AppUpdateDownloadStatus
import com.android.purebilibili.feature.settings.AppUpdateInstallAction
import com.android.purebilibili.feature.settings.AppLanguage
import com.android.purebilibili.feature.settings.applyAppLanguage
import com.android.purebilibili.core.theme.buildDisplayMetricsSnapshot
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.AppDialogAction
import com.android.purebilibili.core.ui.AppThemeConfig
import com.android.purebilibili.core.ui.ProvideAppThemeConfig
import com.android.purebilibili.core.ui.components.LocalAppSingleChoicePresentation
import com.android.purebilibili.core.ui.blur.BlurIntensity
import com.android.purebilibili.core.ui.blur.ProvideUnifiedBlurIntensity
import com.android.purebilibili.core.ui.performance.ProvideRuntimeVisualGuard
import com.android.purebilibili.core.util.BilibiliUrlParser
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.core.util.calculateWindowSizeClass
import com.android.purebilibili.data.repository.VideoRepository
import com.android.purebilibili.feature.cast.LocalProxyServer
import com.android.purebilibili.feature.onboarding.USER_AGREEMENT_ACK_KEY
import com.android.purebilibili.feature.settings.RELEASE_DISCLAIMER_ACK_KEY
import com.android.purebilibili.feature.settings.completeAppUpdateDownload
import com.android.purebilibili.feature.settings.downloadAppUpdateApk
import com.android.purebilibili.feature.settings.failAppUpdateDownload
import com.android.purebilibili.feature.settings.installDownloadedAppUpdate
import com.android.purebilibili.feature.settings.resolveAppUpdateDialogTextColors
import com.android.purebilibili.feature.settings.resolveBuildSourceSubtitle
import com.android.purebilibili.feature.settings.resolveBuildSourceValue
import com.android.purebilibili.feature.settings.resolveUpdateReleaseNotesText
import com.android.purebilibili.feature.settings.selectPreferredAppUpdateAsset
import com.android.purebilibili.feature.settings.shouldRunAppEntryAutoCheck
import com.android.purebilibili.feature.settings.resolveThemePreferenceState
import com.android.purebilibili.feature.screenshot.AppScreenshotCaptureMode
import com.android.purebilibili.feature.screenshot.AppScreenshotGestureBlockState
import com.android.purebilibili.feature.screenshot.AppScreenshotResult
import com.android.purebilibili.feature.screenshot.AppScreenshotSavedImage
import com.android.purebilibili.feature.screenshot.AppScreenshotRegionOverlay
import com.android.purebilibili.feature.screenshot.appScreenshotGestureDetector
import com.android.purebilibili.feature.screenshot.captureAndSaveAppScreenshotImage
import com.android.purebilibili.feature.screenshot.captureCurrentAppWindow
import com.android.purebilibili.feature.screenshot.cropAppScreenshotBitmap
import com.android.purebilibili.feature.screenshot.saveAppScreenshotBitmapToGalleryUri
import com.android.purebilibili.feature.screenshot.shareAppScreenshot
import com.android.purebilibili.feature.screenshot.shouldOfferAppScreenshotShare
import com.android.purebilibili.feature.privacy.PrivacyAuthenticationReason
import com.android.purebilibili.feature.privacy.PrivacyAuthenticationRequest
import com.android.purebilibili.feature.privacy.PrivacyAuthenticationResult
import com.android.purebilibili.feature.video.player.MiniPlayerManager
import com.android.purebilibili.feature.video.player.buildPipPlaybackRemoteActions
import com.android.purebilibili.feature.video.ui.overlay.FullscreenPlayerOverlay
import com.android.purebilibili.feature.video.ui.overlay.MiniPlayerOverlay
import com.android.purebilibili.navigation.AppNavigation
import com.android.purebilibili.navigation.ScreenRoutes
import com.android.purebilibili.navigation.VideoRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

private const val TAG = "MainActivity"
private const val PREFS_NAME = "app_welcome"
private const val KEY_FIRST_LAUNCH = "first_launch_shown"
internal const val EXTRA_PENDING_NAVIGATION_ROUTE = "pending_navigation_route"

@OptIn(UnstableApi::class) // 解决 UnsafeOptInUsageError，因为 AppNavigation 内部使用了不稳定的 API
open class MainActivity : AppCompatActivity() {
    
    //  PiP 状态
    var isInPipMode by mutableStateOf(false)
        private set
    
    //  是否在视频页面 (用于决定是否进入 PiP)
    var isInVideoDetail by mutableStateOf(false)
    
    //  小窗管理器
    private lateinit var miniPlayerManager: MiniPlayerManager
    private var hasCompletedInitialResume = false
    private var splashFlyoutEnabledAtCreate = false
    private var splashExitCallbackTriggered = false
    private var systemInDarkThemeSnapshot by mutableStateOf(false)
    private var runtimeJankStats: JankStats? = null
    private val runtimeVisualGuardSession = Any()

    var windowMetrics: WindowMetrics? by mutableStateOf(null)

    private fun authenticatePrivacyAccess(
        request: PrivacyAuthenticationRequest,
        onResult: (PrivacyAuthenticationResult) -> Unit
    ) {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val availability = BiometricManager.from(this).canAuthenticate(authenticators)
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            onResult(PrivacyAuthenticationResult.Failure(resolvePrivacyAuthenticationUnavailableMessage(request)))
            return
        }

        var delivered = false
        fun deliver(result: PrivacyAuthenticationResult) {
            if (!delivered) {
                delivered = true
                onResult(result)
            }
        }

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    deliver(PrivacyAuthenticationResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val message = when (errorCode) {
                        BiometricPrompt.ERROR_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> "已取消解锁"
                        else -> errString.toString().ifBlank { "解锁失败，请稍后重试" }
                    }
                    deliver(PrivacyAuthenticationResult.Failure(message))
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(request.reason.title)
            .setSubtitle(request.reason.subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(promptInfo)
    }

    private fun resolvePrivacyAuthenticationUnavailableMessage(
        request: PrivacyAuthenticationRequest
    ): String {
        return when (request.reason) {
            PrivacyAuthenticationReason.OPEN_PRIVACY_CONTENT -> "请先设置系统锁屏后再解锁隐私内容"
        }
    }

    private fun refreshSystemThemeSnapshot(reason: String) {
        val currentSystemInDark = resolveMainActivitySystemInDarkTheme(
            resources.configuration.uiMode
        )
        if (shouldRefreshMainActivitySystemThemeSnapshot(systemInDarkThemeSnapshot, currentSystemInDark)) {
            Logger.d(
                TAG,
                "🌓 System theme refreshed on $reason: dark=$currentSystemInDark"
            )
            systemInDarkThemeSnapshot = currentSystemInDark
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppLanguage(SettingsManager.getAppLanguageSync(this))
        //  安装 SplashScreen
        val splashScreen = installSplashScreen()
        val runColdStartSplash = shouldRunColdStartSplash(savedInstanceStatePresent = savedInstanceState != null)
        val welcomePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val splashIconVisible = SettingsManager.isSplashIconAnimationEnabledSync(this)
        val userAgreementAcked = welcomePrefs.getBoolean(USER_AGREEMENT_ACK_KEY, false)
        val splashFlyoutEnabled = runColdStartSplash && shouldEnableSplashFlyoutAnimation(
            sdkInt = Build.VERSION.SDK_INT,
            // Flyout only after mandatory user-agreement gate (covers both new and old users).
            hasCompletedOnboarding = userAgreementAcked,
            hasAcceptedReleaseDisclaimer = userAgreementAcked ||
                welcomePrefs.getBoolean(RELEASE_DISCLAIMER_ACK_KEY, false),
            splashIconAnimationEnabled = splashIconVisible
        )
        val keepSystemSplashForPreload = shouldKeepSystemSplashForPreload(
            runColdStartSplash = runColdStartSplash
        )
        val splashFlyoutIconResId = resolveLaunchIconResId(this, intent)
        splashFlyoutEnabledAtCreate = splashFlyoutEnabled
        Logger.d(
            TAG,
            "🚀 Splash setup. coldStart=$runColdStartSplash, iconVisible=$splashIconVisible, keepForPreload=$keepSystemSplashForPreload, flyoutEnabled=$splashFlyoutEnabled, userAgreementAck=$userAgreementAcked, firstLaunchShown=${welcomePrefs.getBoolean(KEY_FIRST_LAUNCH, false)}, disclaimerAck=${welcomePrefs.getBoolean(RELEASE_DISCLAIMER_ACK_KEY, false)}, taskRoot=$isTaskRoot, savedState=${savedInstanceState != null}, intentFlags=0x${intent?.flags?.toString(16) ?: "0"}, launchIconResId=$splashFlyoutIconResId"
        )
        
        //  🚀 [启动优化] 立即开始预加载首页数据
        // 这个必须尽早调用，利用开屏动画的时间并行加载数据
        VideoRepository.preloadHomeData()
        
        super.onCreate(savedInstanceState)
        //  初始调用，后续会根据主题动态更新
        enableEdgeToEdge()
        
        // 初始化小窗管理器
        miniPlayerManager = MiniPlayerManager.getInstance(this)
        refreshSystemThemeSnapshot(reason = "create")
        
        //  🚀 [启动优化] 保持 Splash 直到数据加载完成或超时
        var isDataReady = false
        val startTime = System.currentTimeMillis()

        windowMetrics = WindowMetricsCalculator.getOrCreate().computeMaximumWindowMetrics(this)

        splashScreen.setKeepOnScreenCondition {
            if (!keepSystemSplashForPreload) {
                return@setKeepOnScreenCondition false
            }

            // 检查数据是否就绪
            if (VideoRepository.isHomeDataReady()) {
                isDataReady = true
            }
            
            // 计算耗时
            val elapsed = System.currentTimeMillis() - startTime
            
            // 条件：数据未就绪 且 未超时(1400ms)
            // 如果超时，强制进入（会显示骨架屏），避免用户以为死机
            val shouldKeep = !isDataReady && elapsed < splashMaxKeepOnScreenMs()
            
            if (!shouldKeep) {
                 Logger.d(TAG, "🚀 Splash dismissed. DataReady=$isDataReady, Elapsed=${elapsed}ms")
            }
            
            shouldKeep
        }

        if (splashFlyoutEnabled) {
            Logger.d(TAG, "🚀 Splash flyout exit listener registered")
            splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
                splashExitCallbackTriggered = true
                runCatching {
                    val splashView = splashScreenViewProvider.view
                    val systemIconView = splashScreenViewProvider.iconView
                    val frameContainer = splashView as? FrameLayout
                        ?: error("Splash root is not a FrameLayout")
                    val targetDrawableState = (systemIconView as? ImageView)
                        ?.drawable
                        ?.constantState
                    val targetSizePx = resolveSplashFlyoutTargetSizePx(
                        systemIconWidthPx = systemIconView.width,
                        systemIconHeightPx = systemIconView.height,
                        density = resources.displayMetrics.density,
                    )
                    Logger.d(
                        TAG,
                        "🚀 Splash exit target metrics. system=${systemIconView.width}x${systemIconView.height}, targetSizePx=$targetSizePx, useRealtimeBlur=${shouldUseRealtimeSplashBlur(Build.VERSION.SDK_INT)}"
                    )

                    var nextInsertIndex = frameContainer.indexOfChild(systemIconView)
                        .let { if (it >= 0) it + 1 else frameContainer.childCount }
                    fun createFlyoutIconView(initialAlpha: Float): ImageView? {
                        // Prefer the original high-density resource. The OEM iconView drawable can
                        // already be a stretched/rasterized snapshot on customized Android builds.
                        val drawable = if (splashFlyoutIconResId != 0) {
                            AppCompatResources.getDrawable(
                                this@MainActivity,
                                splashFlyoutIconResId,
                            )?.mutate()
                        } else {
                            targetDrawableState?.newDrawable(resources)?.mutate()
                        } ?: return null
                        return ImageView(this).apply {
                            scaleType = ImageView.ScaleType.CENTER_INSIDE
                            alpha = initialAlpha
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            applySplashFlyoutRoundedClip(this)
                            setImageDrawable(drawable)
                            frameContainer.addView(
                                this,
                                nextInsertIndex++,
                                FrameLayout.LayoutParams(
                                    targetSizePx,
                                    targetSizePx,
                                    Gravity.CENTER
                                )
                            )
                        }
                    }
                    val secondaryTrailView = createFlyoutIconView(initialAlpha = 0f)
                    val primaryTrailView = createFlyoutIconView(initialAlpha = 0f)
                    val animatedTarget = createFlyoutIconView(initialAlpha = 1f)
                        ?: error("Unable to create a square splash flyout icon")
                    // Never clip or animate the OEM iconView directly: some devices expose a
                    // full-height rectangular container here, which produces the giant leaking
                    // rounded card seen on first cold start.
                    systemIconView.alpha = 0f
                    val targetType = resolveSplashFlyoutTargetType(
                        hasSystemIcon = false,
                        hasFallbackIcon = true,
                    )
                    Logger.d(
                        TAG,
                        "🚀 Splash exit animation start. targetType=$targetType, dedicatedSquareIcon=true"
                    )
                    val minTranslateYPx = splashExitTranslateYDp() * resources.displayMetrics.density
                    val translateYPx = splashExitTravelDistancePx(
                        splashHeightPx = splashView.height,
                        targetSizePx = targetSizePx,
                        minTravelPx = minTranslateYPx
                    )
                    val supportsRealtimeBlur = shouldUseRealtimeSplashBlur(Build.VERSION.SDK_INT)
                    var blurEffectEnabled = supportsRealtimeBlur
                    val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = splashExitDurationMs()
                        interpolator = PathInterpolator(0.12f, 0.98f, 0.2f, 1.0f)
                        addUpdateListener { valueAnimator ->
                            val progress = valueAnimator.animatedValue as Float
                            val trailProgressPrimary = ((progress - 0.08f) / 0.92f).coerceIn(0f, 1f)
                            val trailProgressSecondary = ((progress - 0.16f) / 0.84f).coerceIn(0f, 1f)
                            animatedTarget.translationY = -translateYPx * progress
                            animatedTarget.alpha = splashExitIconAlpha(progress)
                            splashView.alpha = splashExitBackgroundAlpha(progress)

                            val scale = 1f + (splashExitScaleEnd() - 1f) * progress
                            animatedTarget.scaleX = scale
                            animatedTarget.scaleY = scale

                            primaryTrailView?.let { trail ->
                                trail.translationY = -translateYPx * trailProgressPrimary
                                trail.alpha = splashTrailPrimaryAlpha(progress)
                                trail.scaleX = scale * 1.03f
                                trail.scaleY = scale * 1.03f
                            }

                            secondaryTrailView?.let { trail ->
                                trail.translationY = -translateYPx * trailProgressSecondary
                                trail.alpha = splashTrailSecondaryAlpha(progress)
                                trail.scaleX = scale * 1.06f
                                trail.scaleY = scale * 1.06f
                            }

                            if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                shouldApplySplashRealtimeBlur(blurEffectEnabled, progress)
                            ) {
                                val radius = splashExitBlurRadiusEnd() * splashExitBlurProgress(progress)
                                runCatching {
                                    applySplashRealtimeBlur(
                                        animatedTarget = animatedTarget,
                                        primaryTrailView = primaryTrailView,
                                        secondaryTrailView = secondaryTrailView,
                                        radius = radius
                                    )
                                }.onFailure {
                                    blurEffectEnabled = false
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        clearSplashRealtimeBlur(
                                            animatedTarget = animatedTarget,
                                            primaryTrailView = primaryTrailView,
                                            secondaryTrailView = secondaryTrailView
                                        )
                                    }
                                    Logger.w(TAG, "⚠️ Splash realtime blur failed, fallback to non-blur flyout", it)
                                }
                            }
                        }
                    }
                    animator.doOnEnd {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && supportsRealtimeBlur) {
                            clearSplashRealtimeBlur(
                                animatedTarget = animatedTarget,
                                primaryTrailView = primaryTrailView,
                                secondaryTrailView = secondaryTrailView
                            )
                        }
                        frameContainer.removeView(animatedTarget)
                        primaryTrailView?.let(frameContainer::removeView)
                        secondaryTrailView?.let(frameContainer::removeView)
                        splashScreenViewProvider.remove()
                    }
                    animator.start()
                }.onFailure {
                    Logger.e(TAG, "❌ Splash exit animation failed, removing splash immediately", it)
                    splashScreenViewProvider.remove()
                }
            }
        }

        //  [新增] 处理 deep link 或分享意图
        handleIntent(intent)
        
        // --- 📺 DLNA Service Init ---
        // Android 12+ 需要运行时权限
        // requestDlnaPermissionsAndBind()
        
        if (shouldStartLocalProxyOnAppLaunch()) {
            // Optional warmup path; default keeps proxy off cold-start critical path.
            AppScope.ioScope.launch {
                try {
                    val started = LocalProxyServer.ensureStarted()
                    if (started) {
                        Logger.d(TAG, "📺 Local Proxy Server started on port 8901")
                    } else {
                        Logger.d(TAG, "📺 Local Proxy Server already running")
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "❌ Failed to start Local Proxy Server", e)
                }
            }
        }

        val composeContentView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
        val rootContainer = FrameLayout(this).apply {
            addView(
                composeContentView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
        setContentView(rootContainer)

        composeContentView.setContent {
            val context = LocalContext.current
            val uriHandler = LocalUriHandler.current
            val scope = rememberCoroutineScope()
            var startupUpdateCheckResult by remember { mutableStateOf<AppUpdateCheckResult?>(null) }
            // Legacy state remains only for the retired in-place dialog path below.
            var startupUpdateDownloadState by remember { mutableStateOf(AppUpdateDownloadState()) }
            var pendingCrashSnapshotPath by remember {
                mutableStateOf(Logger.getPendingCrashSnapshotPath(context))
            }
            var hasHandledCrashPrompt by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val autoCheckUpdateEnabled = SettingsManager.getAutoCheckAppUpdate(context).first()
                val updateChannel = SettingsManager.getAppUpdateChannel(context).first()
                val gateAllowsCheck = AppUpdateAutoCheckGate.tryMarkChecked()
                if (shouldRunAppEntryAutoCheck(autoCheckUpdateEnabled, gateAllowsCheck)) {
                    AppUpdateChecker.check(
                        currentVersion = BuildConfig.VERSION_NAME,
                        currentVersionCode = BuildConfig.VERSION_CODE,
                        includePrerelease = updateChannel == SettingsManager.AppUpdateChannel.BETA
                    ).onSuccess { info ->
                        if (info.isUpdateAvailable) {
                            startupUpdateCheckResult = info
                        }
                    }
                }
            }
            
            //  首次启动检测已移交 AppNavigation 处理
            // val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
            // var showWelcome by remember { mutableStateOf(!prefs.getBoolean(KEY_FIRST_LAUNCH, false)) }

            val appThemeSettings by SettingsManager
                .getAppThemeSettings(context)
                .collectAsStateWithLifecycle(
                    initialValue = SettingsManager.getInitialAppThemeSettings(context)
                )
            val themeMode = appThemeSettings.themeMode
            val darkThemeStyle = appThemeSettings.darkThemeStyle
            val appLanguage = appThemeSettings.appLanguage

            //  检查并请求所有文件访问权限 (Android 11+)
            //  检查并请求所有文件访问权限 (已移除启动时强制检查，改为按需申请)
            // LaunchedEffect(Unit) { ... }

            val appFontSizePreset = appThemeSettings.appFontSizePreset
            val appFontFileName = appThemeSettings.appFontFileName
            val appUiScalePreset = appThemeSettings.appUiScalePreset
            val appDpiOverridePercent = appThemeSettings.appDpiOverridePercent
            val appGestureScreenshotEnabled = appThemeSettings.appGestureScreenshotEnabled
            val appScreenshotGestureMode = appThemeSettings.appScreenshotGestureMode
            val appScreenshotCaptureMode = appThemeSettings.appScreenshotCaptureMode
            val blurIntensity by SettingsManager.getBlurIntensity(context)
                .collectAsStateWithLifecycle(initialValue = BlurIntensity.THIN)
            val hapticFeedbackEnabled by SettingsManager.getHapticFeedbackEnabled(context)
                .collectAsStateWithLifecycle(initialValue = true)
            val runtimeVisualGuardEnabled by SettingsManager
                .getRuntimeVisualGuardEnabled(context)
                .collectAsStateWithLifecycle(initialValue = true)
            val appThemeConfig = remember(
                blurIntensity,
                hapticFeedbackEnabled,
                runtimeVisualGuardEnabled,
            ) {
                AppThemeConfig(
                    blurIntensity = blurIntensity,
                    hapticFeedbackEnabled = hapticFeedbackEnabled,
                    runtimeVisualGuardEnabled = runtimeVisualGuardEnabled,
                )
            }
            
            // 4. 获取系统当前的深色状态
            val systemInDark = systemInDarkThemeSnapshot

            // 5. 根据枚举值决定是否开启 DarkTheme
            val themePreferenceState = resolveThemePreferenceState(
                themeMode = themeMode,
                darkThemeStyle = darkThemeStyle,
                systemInDark = systemInDark
            )
            val useDarkTheme = themePreferenceState.useDarkTheme
            val useAmoledDarkTheme = themePreferenceState.useAmoledDarkTheme

            //  [新增] 根据主题动态更新状态栏样式
            LaunchedEffect(useDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (useDarkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }

            //  全局 Haze 状态，用于实现毛玻璃效果
            // 强制启用 blur，避免部分设备（如 Android 12）默认降级为仅半透明遮罩
            val mainHazeState = rememberRecoverableHazeState(initialBlurEnabled = true)
            val configuration = LocalConfiguration.current
            val systemDensity = LocalDensity.current
            val displayMetricsSnapshot = remember(
                configuration.densityDpi,
                configuration.smallestScreenWidthDp,
                appFontSizePreset,
                appUiScalePreset,
                appDpiOverridePercent
            ) {
                buildDisplayMetricsSnapshot(
                    systemDensityDpi = configuration.densityDpi,
                    smallestScreenWidthDp = configuration.smallestScreenWidthDp,
                    uiScalePreset = appUiScalePreset,
                    fontSizePreset = appFontSizePreset,
                    dpiOverridePercent = appDpiOverridePercent.takeIf { it > 0 }
                )
            }
            val effectiveDensity = remember(systemDensity, displayMetricsSnapshot.effectiveDensityMultiplier) {
                Density(
                    density = systemDensity.density * displayMetricsSnapshot.effectiveDensityMultiplier,
                    fontScale = systemDensity.fontScale
                )
            }

            //  📐 [平板适配] 计算窗口尺寸类
            val windowSizeClass = calculateWindowSizeClass(
                densityMultiplier = displayMetricsSnapshot.effectiveDensityMultiplier,
                metrics = windowMetrics!!
            )

            // 6. 传入参数
            PureBiliBiliTheme(
                themeMode = themeMode,
                darkTheme = useDarkTheme,
                amoledDarkTheme = useAmoledDarkTheme,
                fontSizePreset = appFontSizePreset,
                appFontFileName = appFontFileName,
                appListItemStyle = appThemeSettings.appListItemStyle,
            ) {
                ProvideAppThemeConfig(config = appThemeConfig) {
                ProvideRuntimeVisualGuard(
                    widthSizeClass = windowSizeClass.widthSizeClass
                ) {
                ProvideUnifiedBlurIntensity {
                    //  📐 [平板适配] 提供全局 WindowSizeClass
                    CompositionLocalProvider(
                        LocalDensity provides effectiveDensity,
                        LocalWindowSizeClass provides windowSizeClass,
                        LocalDisplayMetricsSnapshot provides displayMetricsSnapshot,
                        LocalAppSingleChoicePresentation provides
                            appThemeSettings.singleChoicePresentation,
                    ) {
                    val isPipRenderingActive =
                        isInPipMode || miniPlayerManager.shouldKeepPlaybackForPipTransition()
                    val isFullscreenPlayerLocked = AppScreenshotGestureBlockState.fullscreenPlayerLocked
                    var isAppScreenshotBlockedBySplash by remember { mutableStateOf(false) }
                    var isAppScreenshotSaving by remember { mutableStateOf(false) }
                    var appScreenshotRegionBitmap by remember { mutableStateOf<Bitmap?>(null) }
                    val appScreenshotSnackbarHostState = remember { SnackbarHostState() }
                    val isLandscapeAppScreenshot =
                        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                    val showAppScreenshotSaveFeedback: suspend (AppScreenshotResult, Uri?) -> Unit = { result, uri ->
                        val message = when (result) {
                            AppScreenshotResult.Success -> "截图已保存到相册（PNG）"
                            AppScreenshotResult.Blocked -> "当前状态暂不支持截图"
                            AppScreenshotResult.CaptureFailed,
                            AppScreenshotResult.SaveFailed -> "截图失败，请稍后重试"
                        }
                        if (shouldOfferAppScreenshotShare(isLandscapeAppScreenshot, result, uri != null)) {
                            val snackbarResult = appScreenshotSnackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = "分享",
                                duration = SnackbarDuration.Short
                            )
                            if (snackbarResult == SnackbarResult.ActionPerformed && uri != null) {
                                shareAppScreenshot(context, uri)
                            }
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .appScreenshotGestureDetector(
                                enabled = appGestureScreenshotEnabled,
                                mode = appScreenshotGestureMode,
                                blocked = isPipRenderingActive ||
                                    isFullscreenPlayerLocked ||
                                    isAppScreenshotBlockedBySplash ||
                                    isAppScreenshotSaving ||
                                    appScreenshotRegionBitmap != null,
                                onCaptureRequested = {
                                    if (!isAppScreenshotSaving) {
                                        scope.launch {
                                            isAppScreenshotSaving = true
                                            try {
                                                if (appScreenshotCaptureMode == AppScreenshotCaptureMode.SELECT_REGION) {
                                                    val bitmap = runCatching {
                                                        captureCurrentAppWindow(this@MainActivity)
                                                    }.getOrElse {
                                                        Logger.e(TAG, "应用内截图预览捕获失败", it)
                                                        null
                                                    }
                                                    if (bitmap == null) {
                                                        Toast.makeText(context, "截图失败，请稍后重试", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        appScreenshotRegionBitmap?.recycle()
                                                        appScreenshotRegionBitmap = bitmap
                                                    }
                                                } else {
                                                    val savedImage = runCatching {
                                                        captureAndSaveAppScreenshotImage(this@MainActivity)
                                                    }.getOrElse {
                                                        Logger.e(TAG, "应用内截图失败", it)
                                                        AppScreenshotSavedImage(AppScreenshotResult.CaptureFailed)
                                                    }
                                                    showAppScreenshotSaveFeedback(savedImage.result, savedImage.uri)
                                                }
                                            } finally {
                                                isAppScreenshotSaving = false
                                            }
                                        }
                                    }
                                }
                            )
                            .background(MaterialTheme.colorScheme.background)  // 📐 [修复] 防止平板端返回后出现黑边
                    ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        //  [修复] 移除 .haze() 以避免与 hazeSource/hazeEffect 冲突
                        // 每个 Screen 自己管理 hazeSource（内容）和 hazeEffect（头部/底栏）
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LaunchedEffect(isInPipMode, miniPlayerManager.isPlaying) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPipMode) {
                                    val pipParams = PictureInPictureParams.Builder()
                                        .setAspectRatio(Rational(16, 9))
                                        .setActions(
                                            buildPipPlaybackRemoteActions(
                                                context = this@MainActivity,
                                                player = miniPlayerManager.player
                                            )
                                        )
                                        .build()
                                    setPictureInPictureParams(pipParams)
                                }
                            }
                            AppNavigation(
                                miniPlayerManager = miniPlayerManager,
                                isInPipMode = isPipRenderingActive,
                                pendingVideoId = pendingVideoId,
                                pendingShortcutRoute = pendingRoute,
                                pendingNavigationRoute = pendingNavigationRoute,
                                onPendingVideoIdConsumed = { consumedVideoId ->
                                    if (pendingVideoId == consumedVideoId) {
                                        pendingVideoId = null
                                    }
                                },
                                onPendingShortcutRouteConsumed = { consumedRoute ->
                                    if (pendingRoute == consumedRoute) {
                                        pendingRoute = null
                                    }
                                },
                                onPendingNavigationRouteConsumed = { consumedRoute ->
                                    if (pendingNavigationRoute == consumedRoute) {
                                        pendingNavigationRoute = null
                                    }
                                },
                                initialSearchKeyword = pendingSearchKeyword,
                                onInitialSearchKeywordConsumed = { consumedKeyword ->
                                    if (pendingSearchKeyword == consumedKeyword) {
                                        pendingSearchKeyword = null
                                    }
                                },
                                onVideoDetailEnter = {
                                    isInVideoDetail = true
                                    Logger.d(TAG, " 进入视频详情页")
                                },
                                onVideoDetailExit = {
                                    isInVideoDetail = false
                                    Logger.d(TAG, "🔙 退出视频详情页")
                                },
                                onPrivacyAuthenticationRequired = ::authenticatePrivacyAccess,
                                mainHazeState = mainHazeState //  传递全局 Haze 状态
                            )
                            
                            //  OnboardingBottomSheet 等其他 overlay 组件

                        }
                    }
                    //  小窗全屏状态
                    var showFullscreen by remember { mutableStateOf(false) }
                    val playbackOverlayState = remember(isInPipMode, miniPlayerManager.isMiniMode) {
                        resolveMainActivityPlaybackOverlayState(
                            isInPipMode = isInPipMode,
                            isMiniMode = miniPlayerManager.isMiniMode
                        )
                    }
                    if (playbackOverlayState.showDedicatedPipPlayer) {
                        miniPlayerManager.player?.let { pipPlayer ->
                            AndroidView(
                                factory = { viewContext ->
                                    PlayerView(viewContext).apply {
                                        player = pipPlayer
                                        useController = false
                                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                    }
                                },
                                update = { playerView -> playerView.player = pipPlayer },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                            )
                        }
                    }
                    //  小窗播放器覆盖层 (非 PiP 模式下显示)
                    if (playbackOverlayState.showMiniPlayerOverlay) {
                        MiniPlayerOverlay(
                            miniPlayerManager = miniPlayerManager,
                            onPictureInPictureClick = if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                miniPlayerManager.shouldEnterPip()
                            ) {
                                { enterMiniPlayerPictureInPicture() }
                            } else {
                                null
                            },
                            onExpandClick = {
                                if (miniPlayerManager.isLiveMode) {
                                    // 📺 直播小窗展开：导航回直播间
                                    val roomId = miniPlayerManager.currentRoomId
                                    val liveTitle = miniPlayerManager.currentTitle
                                    val liveUname = miniPlayerManager.currentLiveUname
                                    miniPlayerManager.exitMiniMode(animate = false)
                                    pendingNavigationRoute =
                                        ScreenRoutes.Live.createRoute(roomId, liveTitle, liveUname)
                                } else {
                                    //  [修改] 导航回详情页，而不是只显示全屏播放器
                                    miniPlayerManager.currentBvid?.let { bvid ->
                                        miniPlayerManager.isNavigatingToVideo = true
                                        miniPlayerManager.exitMiniMode(animate = false)
                                        val cid = miniPlayerManager.currentCid
                                        pendingNavigationRoute = resolveMiniPlayerExpandVideoRoute(bvid = bvid, cid = cid)
                                    }
                                }
                            }
                        )
                    }
                    
                    //  全屏播放器覆盖层（包含亮度、音量、进度调节）
                    if (showFullscreen) {
                        FullscreenPlayerOverlay(
                            miniPlayerManager = miniPlayerManager,
                            onDismiss = { 
                                showFullscreen = false
                                miniPlayerManager.enterMiniMode()
                            },
                            onNavigateToDetail = {
                                //  关闭全屏覆盖层并导航到视频详情页
                                showFullscreen = false
                                miniPlayerManager.currentBvid?.let { bvid ->
                                    miniPlayerManager.isNavigatingToVideo = true
                                    miniPlayerManager.exitMiniMode(animate = false)
                                    //  [修复] 使用正确的 cid，而不是 0
                                    val cid = miniPlayerManager.currentCid
                                    pendingNavigationRoute = resolveMainActivityVideoRoute(bvid = bvid, cid = cid)
                                }
                            }
                        )
                    }
                    
                    //  护眼模式覆盖层（最顶层，应用于所有内容）
                    EyeProtectionOverlay()
                    
                    // [New] Custom Splash Wallpaper Overlay
                    val readCustomSplashPrefs = remember { shouldReadCustomSplashPreferences() }
                    val splashUri = remember(readCustomSplashPrefs) {
                        val fixedUri = SettingsManager.getSplashWallpaperUriSync(context)
                        val randomEnabled = SettingsManager.isSplashRandomEnabledSync(context)
                        val splashRandomPool = SettingsManager.getSplashRandomPoolUrisSync(context)
                        resolveSplashWallpaperUriForLaunch(
                            randomEnabled = randomEnabled,
                            fixedSplashUri = fixedUri,
                            poolUris = splashRandomPool,
                            launchSeed = System.currentTimeMillis()
                        )
                    }
                    val splashAlignmentBias = remember(readCustomSplashPrefs, windowSizeClass.widthSizeClass) {
                        val mobileBias = SettingsManager.getSplashAlignmentSync(context, isTablet = false)
                        val tabletBias = SettingsManager.getSplashAlignmentSync(context, isTablet = true)
                        resolveSplashWallpaperAlignmentBias(
                            isTabletLayout = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact,
                            mobileBias = mobileBias,
                            tabletBias = tabletBias
                        )
                    }
                    val showCustomSplashInitially = remember(runColdStartSplash, splashUri) {
                        runColdStartSplash && shouldShowCustomSplashOverlay(
                            customSplashEnabled = SettingsManager.isSplashEnabledSync(context),
                            splashUri = splashUri
                        )
                    }
                    var showSplash by remember { mutableStateOf(showCustomSplashInitially) }
                    LaunchedEffect(showSplash) {
                        isAppScreenshotBlockedBySplash = showSplash
                    }
                    // [Optimization] If we delayed enough in splash screen, we might want to skip custom splash or show it briefly?
                    // Logic: If user uses custom splash, system splash shows icon, then custom splash shows wallpaper.
                    // If we use setKeepOnScreenCondition, system splash (icon) stays longer.
                    // This is acceptable behavior: Icon -> Wallpaper (if enabled) -> App.
                    // Or if custom wallpaper is enabled, maybe we shouldn't delay system splash?
                    // User request: "当用户看见遮罩的时候，异步加载首页视频". Mask usually means System Splash (Icon) OR Custom Wallpaper.
                    // Implementing delay on System Splash ensures data is likely ready when ANY content shows.

                    LaunchedEffect(showCustomSplashInitially) {
                        if (showCustomSplashInitially) {
                            showSplash = true
                            delay(customSplashHoldDurationMs())
                            showSplash = false
                        } else {
                            showSplash = false
                        }
                    }
                    val splashOverlayAlpha by animateFloatAsState(
                        targetValue = if (showSplash) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = customSplashFadeDurationMs(),
                            easing = AppMotionEasing.EmphasizedEnter
                        ),
                        label = "customSplashOverlayAlpha"
                    )
                    val splashFadeProgress = customSplashFadeProgress(splashOverlayAlpha)
                    val splashOverlayScale = customSplashOverlayScale(splashFadeProgress)
                    val splashExtraBlur = customSplashExtraBlurDp(splashFadeProgress)
                    val splashTailScrimAlpha = customSplashOverlayScrimAlpha(splashFadeProgress)

                    if (customSplashShouldRender(showSplash, splashOverlayAlpha) && splashUri.isNotEmpty()) {
                        val splashProbePainter = rememberAsyncImagePainter(model = splashUri)
                        val splashAspectRatio by remember(splashProbePainter.state) {
                            derivedStateOf {
                                when (val state = splashProbePainter.state) {
                                    is AsyncImagePainter.State.Success -> resolveDrawableAspectRatio(
                                        width = state.result.drawable.intrinsicWidth,
                                        height = state.result.drawable.intrinsicHeight
                                    )
                                    else -> null
                                }
                            }
                        }
                        val splashWallpaperLayout = remember(windowSizeClass.widthSizeClass, splashAspectRatio) {
                            resolveSplashWallpaperLayout(
                                widthSizeClass = windowSizeClass.widthSizeClass,
                                imageAspectRatio = splashAspectRatio
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(alpha = splashOverlayAlpha)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            when (splashWallpaperLayout) {
                                SplashWallpaperLayout.FULL_CROP -> {
                                    AsyncImage(
                                        model = splashUri,
                                        contentDescription = "Splash Wallpaper",
                                        alignment = BiasAlignment(0f, splashAlignmentBias),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(
                                                scaleX = splashOverlayScale,
                                                scaleY = splashOverlayScale
                                            )
                                            .blur(splashExtraBlur.dp)
                                    )
                                }

                                SplashWallpaperLayout.POSTER_CARD_BLUR_BG -> {
                                    AsyncImage(
                                        model = splashUri,
                                        contentDescription = null,
                                        alignment = BiasAlignment(0f, splashAlignmentBias),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(
                                                scaleX = splashOverlayScale,
                                                scaleY = splashOverlayScale
                                            )
                                            .blur((56f + splashExtraBlur).dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Color.Black.copy(
                                                    alpha = (0.16f + splashTailScrimAlpha * 0.5f).coerceAtMost(0.26f)
                                                )
                                            )
                                    )
                                    Card(
                                        shape = AppShapes.container(ContainerLevel.Floating),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .graphicsLayer(
                                                scaleX = 1f + (splashFadeProgress * 0.015f),
                                                scaleY = 1f + (splashFadeProgress * 0.015f)
                                            )
                                            .fillMaxWidth(
                                                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                                                    0.34f
                                                } else {
                                                    0.48f
                                                }
                                            )
                                            .widthIn(min = 190.dp, max = 340.dp)
                                            .aspectRatio(9f / 16f)
                                    ) {
                                        AsyncImage(
                                            model = splashUri,
                                            contentDescription = "Splash Wallpaper Poster",
                                            alignment = BiasAlignment(0f, splashAlignmentBias),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(
                                                    scaleX = splashOverlayScale,
                                                    scaleY = splashOverlayScale
                                                )
                                                .blur((splashExtraBlur * 0.35f).dp)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = splashTailScrimAlpha))
                            )
                        }
                    }

                    appScreenshotRegionBitmap?.let { bitmap ->
                        AppScreenshotRegionOverlay(
                            bitmap = bitmap,
                            saving = isAppScreenshotSaving,
                            onCancel = {
                                bitmap.recycle()
                                appScreenshotRegionBitmap = null
                            },
                            onSaveRegion = { cropRect ->
                                if (!isAppScreenshotSaving) {
                                    scope.launch {
                                        isAppScreenshotSaving = true
                                        try {
                                            val croppedBitmap = cropAppScreenshotBitmap(bitmap, cropRect)
                                            val savedUri = croppedBitmap?.let { cropped ->
                                                try {
                                                    saveAppScreenshotBitmapToGalleryUri(context, cropped)
                                                } finally {
                                                    cropped.recycle()
                                                }
                                            }
                                            val result = if (savedUri != null) {
                                                AppScreenshotResult.Success
                                            } else {
                                                AppScreenshotResult.SaveFailed
                                            }
                                            if (savedUri != null) {
                                                bitmap.recycle()
                                                appScreenshotRegionBitmap = null
                                            }
                                            showAppScreenshotSaveFeedback(result, savedUri)
                                        } finally {
                                            isAppScreenshotSaving = false
                                        }
                                    }
                                }
                            }
                        )
                    }

                    SnackbarHost(
                        hostState = appScreenshotSnackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(WindowInsets.safeDrawing.asPaddingValues())
                            .padding(16.dp)
                    )

                    startupUpdateCheckResult?.let { info ->
                        AppUpdateDialogHost(
                            update = info,
                            onDismissRequest = { startupUpdateCheckResult = null },
                        )
                    }

                    if (false) {
                    startupUpdateCheckResult?.let { info ->
                        val resolvedReleaseNotes = remember(info.releaseNotes) {
                            resolveUpdateReleaseNotesText(info.releaseNotes)
                        }
                        val preferredAsset = remember(info.assets) {
                            selectPreferredAppUpdateAsset(info.assets)
                        }
                        val releaseCommit = remember(info.buildMetadata?.gitCommitSha) {
                            resolveBuildSourceValue(info.buildMetadata?.gitCommitSha, fallback = "未知")
                        }
                        val releaseWorkflowSubtitle = remember(info.buildMetadata?.workflowRunId, info.buildMetadata?.releaseTag) {
                            resolveBuildSourceSubtitle(
                                workflowRunId = info.buildMetadata?.workflowRunId,
                                releaseTag = info.buildMetadata?.releaseTag
                            )
                        }
                        val releaseVerificationEvidence = remember(info.verificationMetadata?.attestationUrl) {
                            if (info.verificationMetadata?.attestationUrl?.isNotBlank() == true) {
                                "GitHub Attestation"
                            } else {
                                "未提供"
                            }
                        }
                        val isDialogDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                        val dialogTextColors = remember(isDialogDarkTheme) {
                            resolveAppUpdateDialogTextColors(
                                isDarkTheme = isDialogDarkTheme
                            )
                        }
                        val releaseNotesScrollState = rememberScrollState()
                        AppAlertDialog(
                            onDismissRequest = { startupUpdateCheckResult = null },
                            title = {
                                Text(
                                    text = "发现新版本 v${info.latestVersion}",
                                    color = dialogTextColors.titleColor
                                )
                            },
                            text = {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "当前版本 v${info.currentVersion}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = dialogTextColors.currentVersionColor
                                    )
                                    preferredAsset?.let { asset ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "安装包：${asset.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = dialogTextColors.currentVersionColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Release 锁定：${if (info.releaseIsImmutable) "Immutable" else "可变"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = dialogTextColors.currentVersionColor
                                    )
                                    Text(
                                        text = "源码提交：$releaseCommit",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = dialogTextColors.currentVersionColor
                                    )
                                    Text(
                                        text = "构建来源：$releaseWorkflowSubtitle",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = dialogTextColors.currentVersionColor
                                    )
                                    Text(
                                        text = "Provenance：$releaseVerificationEvidence",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = dialogTextColors.currentVersionColor
                                    )
                                    if (startupUpdateDownloadState.status != AppUpdateDownloadStatus.IDLE) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = when (startupUpdateDownloadState.status) {
                                                AppUpdateDownloadStatus.QUEUED -> "等待网络后开始下载"
                                                AppUpdateDownloadStatus.DOWNLOADING ->
                                                    "下载中 ${(startupUpdateDownloadState.progress * 100).toInt()}%"
                                                AppUpdateDownloadStatus.COMPLETED -> "下载完成，正在准备安装"
                                                AppUpdateDownloadStatus.FAILED ->
                                                    startupUpdateDownloadState.errorMessage ?: "下载失败"
                                                AppUpdateDownloadStatus.IDLE -> ""
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = dialogTextColors.currentVersionColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = resolvedReleaseNotes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = dialogTextColors.releaseNotesColor,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 280.dp)
                                            .verticalScroll(releaseNotesScrollState)
                                    )
                                }
                            },
                            confirmButton = {
                                AppDialogAction(onClick = {
                                    val downloadedFile = startupUpdateDownloadState.filePath
                                        ?.takeIf { startupUpdateDownloadState.status == AppUpdateDownloadStatus.COMPLETED }
                                        ?.let { path -> File(path) }
                                        ?.takeIf { it.exists() }

                                    if (downloadedFile != null) {
                                        installDownloadedAppUpdate(context, downloadedFile)
                                        return@AppDialogAction
                                    }

                                    val asset = preferredAsset
                                    if (asset == null) {
                                        startupUpdateCheckResult = null
                                        uriHandler.openUri(info.releaseUrl)
                                        return@AppDialogAction
                                    }

                                    if (startupUpdateDownloadState.status == AppUpdateDownloadStatus.DOWNLOADING) {
                                        return@AppDialogAction
                                    }

                                    scope.launch {
                                        downloadAppUpdateApk(
                                            context = context,
                                            asset = asset,
                                            onStateChange = { state -> startupUpdateDownloadState = state }
                                        ).onSuccess { file ->
                                            startupUpdateDownloadState = completeAppUpdateDownload(
                                                current = startupUpdateDownloadState,
                                                filePath = file.absolutePath
                                            )
                                            val installAction = installDownloadedAppUpdate(context, file)
                                            if (installAction == AppUpdateInstallAction.OPEN_UNKNOWN_SOURCES_SETTINGS) {
                                                Toast.makeText(context, "请先允许安装未知来源应用", Toast.LENGTH_SHORT).show()
                                            }
                                        }.onFailure { error ->
                                            startupUpdateDownloadState = failAppUpdateDownload(
                                                current = startupUpdateDownloadState,
                                                errorMessage = error.message ?: "更新下载失败"
                                            )
                                            Toast.makeText(context, error.message ?: "更新下载失败", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Text(
                                        when {
                                            preferredAsset == null -> "前往下载"
                                            startupUpdateDownloadState.status == AppUpdateDownloadStatus.DOWNLOADING ->
                                                "下载中 ${(startupUpdateDownloadState.progress * 100).toInt()}%"
                                            startupUpdateDownloadState.status == AppUpdateDownloadStatus.COMPLETED -> "安装更新"
                                            else -> "立即更新"
                                        }
                                    )
                                }
                            },
                            dismissButton = {
                                AppDialogAction(onClick = {
                                    startupUpdateCheckResult = null
                                    startupUpdateDownloadState = AppUpdateDownloadState()
                                }) { Text("稍后") }
                            }
                        )
                    }
                    }

                    if (
                        shouldShowPendingCrashLogPrompt(
                            hasPendingCrashSnapshot = pendingCrashSnapshotPath != null,
                            hasPromptBeenHandled = hasHandledCrashPrompt
                        )
                    ) {
                        AppAlertDialog(
                            onDismissRequest = {
                                hasHandledCrashPrompt = true
                                if (shouldClearPendingCrashLogAfterAction(CrashLogPromptAction.DISMISS)) {
                                    Logger.clearPendingCrashSnapshot(context)
                                    pendingCrashSnapshotPath = null
                                }
                            },
                            title = {
                                Text(text = "检测到上次闪退日志")
                            },
                            text = {
                                Text(
                                    text = "应用已自动保存一份崩溃快照，并同步导出到 Download/BiliPai/logs/last_crash_log.txt。现在可以直接分享给开发者排查，也可以先关闭提示。"
                                )
                            },
                            confirmButton = {
                                AppDialogAction(onClick = {
                                    hasHandledCrashPrompt = true
                                    Logger.sharePendingCrashSnapshot(context)
                                    if (shouldClearPendingCrashLogAfterAction(CrashLogPromptAction.SHARE)) {
                                        Logger.clearPendingCrashSnapshot(context)
                                        pendingCrashSnapshotPath = null
                                    }
                                }) { Text("分享") }
                            },
                            dismissButton = {
                                AppDialogAction(onClick = {
                                    hasHandledCrashPrompt = true
                                    if (shouldClearPendingCrashLogAfterAction(CrashLogPromptAction.DISMISS)) {
                                        Logger.clearPendingCrashSnapshot(context)
                                        pendingCrashSnapshotPath = null
                                    }
                                }) { Text("关闭") }
                            }
                        )
                    }

                    }
                }
                }
                }
            }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        windowMetrics = WindowMetricsCalculator.getOrCreate().computeMaximumWindowMetrics(this)
        refreshSystemThemeSnapshot(reason = "configuration")
    }

    override fun onStart() {
        super.onStart()
        AppRuntimeVisualGuardTracker.activateSession(runtimeVisualGuardSession)
        val existingJankStats = runtimeJankStats
        if (existingJankStats != null) {
            existingJankStats.isTrackingEnabled = true
        } else {
            runtimeJankStats = runCatching {
                JankStats.createAndTrack(window) { frameData ->
                    AppRuntimeVisualGuardTracker.onFrame(
                        session = runtimeVisualGuardSession,
                        frameData = frameData,
                        nowMs = SystemClock.uptimeMillis(),
                    )
                }
            }.onFailure { throwable ->
                Logger.w(TAG, "无法启动页面转场性能采样", throwable)
            }.getOrNull()
        }
        if (shouldLogWarmResume(hasCompletedInitialResume, isChangingConfigurations)) {
            Logger.d(
                TAG,
                "🔁 Warm resume path. splash flyout is not expected (Activity already created). flyoutEnabledAtCreate=$splashFlyoutEnabledAtCreate, splashExitCallbackTriggered=$splashExitCallbackTriggered"
            )
        }
    }

    override fun onStop() {
        AppRuntimeVisualGuardTracker.discardActiveWindow(runtimeVisualGuardSession)
        runtimeJankStats?.isTrackingEnabled = false
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        refreshSystemThemeSnapshot(reason = "resume")
        miniPlayerManager.clearUserLeaveHint()
        miniPlayerManager.clearPlaybackRoutePipState()
        miniPlayerManager.clearPlaybackNotificationIfIdleOnResume()
        if (
            shouldRestorePlaybackRouteStateOnResume(
                isPlaybackRouteActive = isPlaybackRouteActive(
                    isInVideoDetail = isInVideoDetail
                )
            )
        ) {
            miniPlayerManager.resetNavigationFlag()
            miniPlayerManager.player?.let { player ->
                if (shouldRestoreMutedPlaybackPlayerVolumeOnResume(player.volume)) {
                    com.android.purebilibili.core.player.PlayerVolumeController
                        .applyPreferredVolume(player)
                }
            }
        }
        if (!hasCompletedInitialResume) {
            hasCompletedInitialResume = true
        }
    }
    
    //  用户按 Home 键或切换应用时触发
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        
        Logger.d(
            TAG,
            "👋 onUserLeaveHint 触发, isInVideoDetail=$isInVideoDetail, isMiniMode=${miniPlayerManager.isMiniMode}"
        )
        miniPlayerManager.markUserLeaveHint()
        miniPlayerManager.refreshMediaSessionBinding()
        
        //  [重构] 使用新的模式判断方法
        val shouldEnterPip = miniPlayerManager.shouldEnterPip()
        val currentMode = miniPlayerManager.getCurrentMode()
        val isActuallyPlaying = miniPlayerManager.isPlaying || (miniPlayerManager.player?.isPlaying == true)
        val isPlaybackRouteActive = isPlaybackRouteActive(
            isInVideoDetail = isInVideoDetail
        )
        val shouldTriggerPip = shouldTriggerPlaybackRoutePip(
            isInVideoDetail = isInVideoDetail,
            isInMiniMode = miniPlayerManager.isMiniMode,
            shouldEnterPip = shouldEnterPip,
            isActuallyPlaying = isActuallyPlaying
        )
        miniPlayerManager.updatePlaybackRoutePipRequest(shouldTriggerPip)

        Logger.d(
            TAG,
            " miniPlayerMode=$currentMode, shouldEnterPip=$shouldEnterPip, isPlaying=$isActuallyPlaying, shouldTriggerPip=$shouldTriggerPip, API=${Build.VERSION.SDK_INT}"
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && shouldTriggerPip) {
            try {
                Logger.d(TAG, " 尝试进入 PiP 模式...")
                
                val pipParams = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .setActions(
                        buildPipPlaybackRemoteActions(
                            context = this,
                            player = miniPlayerManager.player
                        )
                    )
                
                // Android 12+: 启用自动进入和无缝调整
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    pipParams.setAutoEnterEnabled(true)
                    pipParams.setSeamlessResizeEnabled(true)
                }
                
                enterPictureInPictureMode(pipParams.build())
                Logger.d(TAG, " 成功进入 PiP 模式")
            } catch (e: Exception) {
                miniPlayerManager.updatePlaybackRoutePipRequest(false)
                Logger.e(TAG, " 进入 PiP 失败", e)
            }
        } else {
            Logger.d(TAG, "⏳ 未满足 PiP 条件: API>=${Build.VERSION_CODES.O}=${Build.VERSION.SDK_INT >= Build.VERSION_CODES.O}, shouldTriggerPip=$shouldTriggerPip")
        }
    }

    private fun enterMiniPlayerPictureInPicture() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (!miniPlayerManager.shouldEnterPip() || miniPlayerManager.player == null) return
        miniPlayerManager.updatePlaybackRoutePipRequest(true)
        try {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .setActions(
                    buildPipPlaybackRemoteActions(
                        context = this,
                        player = miniPlayerManager.player
                    )
                )
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setSeamlessResizeEnabled(true)
                    }
                }
                .build()
            enterPictureInPictureMode(params)
        } catch (error: Exception) {
            miniPlayerManager.updatePlaybackRoutePipRequest(false)
            Logger.e(TAG, "小窗切换画中画失败", error)
        }
    }
    
    //  PiP 模式变化回调
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
        miniPlayerManager.updateSystemPipActive(isInPictureInPictureMode)
        Logger.d(TAG, " PiP 模式变化: $isInPictureInPictureMode")
    }
    
    //  [新增] 处理 singleTop 模式下的新 Intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }
    
    // 📺 [DLNA] 权限请求和服务绑定 - 移除自动请求，改为按需请求
    // private val dlnaPermissionLauncher = ...
    
    // private fun requestDlnaPermissionsAndBind() { ... }
    
    //  待导航的视频 ID（用于在 Compose 中触发导航）
    var pendingVideoId by mutableStateOf<String?>(null)
    var pendingRoute by mutableStateOf<String?>(null)  // 🚀 App Shortcuts: pending route
    var pendingSearchKeyword by mutableStateOf<String?>(null)
    var pendingNavigationRoute by mutableStateOf<String?>(null)
        private set
    
    /**
     *  [新增] 处理 Deep Link 和分享意图
     */
    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        intent.getStringExtra(EXTRA_PENDING_NAVIGATION_ROUTE)
            ?.takeIf { it.isNotBlank() }
            ?.let { route ->
                Logger.d(TAG, "🧭 restore route from intent extra: $route")
                pendingNavigationRoute = route
                return
            }
        
        Logger.d(TAG, "🔗 handleIntent: action=${intent.action}, data=${intent.data}")
        
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                // 点击链接打开
                val uri = intent.data
                if (uri != null) {
                    val scheme = uri.scheme ?: ""
                    val host = uri.host ?: ""

                    val pluginInstallRequest = resolvePluginInstallDeepLink(uri.toString())
                    if (pluginInstallRequest != null) {
                        pendingNavigationRoute = ScreenRoutes.PluginsSettings
                            .createRoute(importUrl = pluginInstallRequest.pluginUrl)
                        Logger.d(TAG, "🚀 Plugin install deep link detected: ${pluginInstallRequest.pluginUrl}")
                        return
                    }
                    
                    // 🚀 App Shortcuts: bilipai:// scheme
                    if (scheme == "bilipai") {
                        pendingRoute = host  // e.g., "search", "dynamic", "favorite", "history"
                        Logger.d(TAG, "🚀 App Shortcut detected: $host")
                    } else {
                        resolveIntentLinkAndNavigate(uri.toString())
                    }
                }
            }
            Intent.ACTION_SEND -> {
                // 分享文本到 app
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (text != null) {
                    Logger.d(TAG, "📤 收到分享文本: $text")
                    
                    val urls = BilibiliUrlParser.extractUrls(text)
                    val pluginInstallLink = urls.firstOrNull { resolvePluginInstallDeepLink(it) != null }

                    if (pluginInstallLink != null) {
                        val pluginInstallRequest = resolvePluginInstallDeepLink(pluginInstallLink)
                        if (pluginInstallRequest != null) {
                            pendingNavigationRoute = ScreenRoutes.PluginsSettings
                                .createRoute(importUrl = pluginInstallRequest.pluginUrl)
                            Logger.d(TAG, "🚀 Plugin install shared link detected: ${pluginInstallRequest.pluginUrl}")
                            return
                        }
                    }

                    resolveIntentLinkAndNavigate(text)
                }
            }
        }
    }
    
    /**
     *  统一解析入口链接，必要时异步展开 b23.tv
     */
    private fun resolveIntentLinkAndNavigate(rawInput: String) {
        BilibiliNavigationTargetParser.parse(rawInput)?.let { target ->
            applyIntentNavigationTarget(target)
            return
        }

        resolveIntentLinkFallbackRoute(rawInput)?.let { route ->
            Logger.d(TAG, "🌐 入口链接先回退到 WebView: $route")
            pendingNavigationRoute = route
        }

        lifecycleScope.launch {
            val target = BilibiliNavigationTargetParser.resolve(rawInput)
            if (target != null) {
                applyIntentNavigationTarget(target)
            } else {
                resolveIntentLinkFallbackRoute(rawInput)?.let { route ->
                    Logger.d(TAG, "🌐 入口链接回退到 WebView: $route")
                    pendingNavigationRoute = route
                    return@launch
                }
                Logger.w(TAG, "⚠️ 无法解析入口链接: $rawInput")
            }
        }
    }

    private fun applyIntentNavigationTarget(target: BilibiliNavigationTarget) {
        val navigation = resolveMainActivityLinkNavigation(target)
        if (navigation == null) {
            Logger.w(TAG, "⚠️ 暂不支持入口目标: $target")
            return
        }
        navigation.pendingVideoId?.let { videoId ->
            Logger.d(TAG, "📺 入口链接解析到视频: $videoId")
            pendingVideoId = videoId
            return
        }
        navigation.pendingSearchKeyword?.let { keyword ->
            Logger.d(TAG, "🔎 入口链接解析到搜索词: $keyword")
            pendingSearchKeyword = keyword
        }
        navigation.pendingNavigationRoute?.let { route ->
            Logger.d(TAG, "🧭 入口链接解析到路由: $route")
            pendingNavigationRoute = route
        }
    }
    
    override fun onDestroy() {
        runtimeJankStats?.isTrackingEnabled = false
        runtimeJankStats = null
        super.onDestroy()
    }
}

/**
 *  首次启动欢迎弹窗 - 精美设计版
 */
