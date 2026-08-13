// 文件路径: app/PureApplication.kt
package com.android.purebilibili.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentCallbacks2
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import androidx.profileinstaller.ProfileInstaller
import com.android.purebilibili.BuildConfig
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.android.purebilibili.core.coroutines.AppScope
import com.android.purebilibili.core.lifecycle.BackgroundManager
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.core.network.WbiKeyManager
import com.android.purebilibili.core.plugin.PluginManager
import com.android.purebilibili.core.store.DEFAULT_ANALYTICS_ENABLED
import com.android.purebilibili.core.store.DEFAULT_CRASH_TRACKING_ENABLED
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.store.TokenManager
import com.android.purebilibili.core.util.AnalyticsHelper
import com.android.purebilibili.core.util.CrashReporter
import com.android.purebilibili.core.util.Logger
import com.android.purebilibili.feature.settings.applyAppLanguage
import com.android.purebilibili.feature.settings.AppThemeMode
import com.android.purebilibili.feature.settings.resolveThemeModePreference
import com.android.purebilibili.feature.plugin.AdFilterPlugin
import com.android.purebilibili.feature.plugin.CdnRegionPlugin
import com.android.purebilibili.feature.plugin.DanmakuEnhancePlugin
import com.android.purebilibili.feature.plugin.EyeProtectionPlugin
import com.android.purebilibili.feature.plugin.HomeFeedAnonymizerPlugin
import com.android.purebilibili.feature.plugin.SponsorBlockPlugin
import com.android.purebilibili.feature.plugin.dlna.DlnaCastPlugin
import com.android.purebilibili.feature.plugin.googlecast.GoogleCastPlugin
import com.android.purebilibili.feature.plugin.TodayWatchPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

//  实现 ImageLoaderFactory 以提供自定义 Coil 配置
//  实现 ComponentCallbacks2 响应系统内存警告
class PureApplication : Application(), ImageLoaderFactory, ComponentCallbacks2 {
    
    //  保存 ImageLoader 引用以便在 onTrimMemory 中使用
    private var _imageLoader: ImageLoader? = null

    private val telemetryListener =
        PureApplicationRuntimeConfig.createTelemetryBackgroundStateListener()

    private val startupOrchestrator by lazy { AppStartupOrchestrator() }
    
    //  Coil 图片加载器 - 优化内存和磁盘缓存
    override fun newImageLoader(): ImageLoader {
        val memoryCachePercent = PureApplicationRuntimeConfig.resolveImageMemoryCachePercent()
        val diskCacheBytes = 150L * 1024 * 1024
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            //  内存缓存预算（移动/平板主仓）
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(memoryCachePercent)
                    .build()
            }
            //  磁盘缓存预算（移动/平板主仓）
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(diskCacheBytes)
                    .build()
            }
            .okHttpClient { NetworkModule.okHttpClient } // 🔥 [Fix] 共享 OkHttpClient 以获得 DNS 修复
            //  优先使用缓存
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            //  启用 Bitmap 复用减少内存分配
            .allowRgb565(true)
            .crossfade(true)
            .build()
            .also { _imageLoader = it }  // 保存引用
    }
    
    override fun onCreate() {
        // StrictMode 必须装在任何业务代码之前，否则紧接着的 applyThemePreference()
        // 里那次同步偏好读取就漏检了——而那恰恰是最该被看见的一处。
        installStrictModeForDebugBuilds()

        //  [关键] 必须在 super.onCreate() 之前设置！
        // 这样系统在初始化时就能读取到正确的夜间模式配置
        applyThemePreference()
        
        super.onCreate()
        Logger.init(this)
        CrashReporter.installGlobalExceptionHandler()

        // 启动即确保首页视觉默认值生效：底栏悬浮 + 液态玻璃 + 顶部模糊
        // 冷启动路径不阻塞主线程，迁移改为后台执行。
        if (PureApplicationRuntimeConfig.shouldBlockStartupForHomeVisualDefaultsMigration()) {
            runBlocking(Dispatchers.IO) {
                SettingsManager.ensureHomeVisualDefaults(this@PureApplication)
            }
        } else {
            AppScope.ioScope.launch {
                SettingsManager.ensureHomeVisualDefaults(this@PureApplication)
            }
        }

        startupOrchestrator.runImmediate(::runStartupTask)
        startupOrchestrator.scheduleDeferred(::runStartupTask)
    }

    /**
     * debug 包启用 StrictMode。
     *
     * 此前全仓 **0 处 StrictMode 引用**，意味着主线程磁盘 I/O 在开发期永远不会自动
     * 暴露，只能靠人肉 review 抓。这是「性能优化在盲打」的第三条证据——另两条是
     * Baseline Profile 从未产出产物、Compose 指标被注释掉。
     *
     * 刻意用 [StrictMode.ThreadPolicy.Builder.penaltyLog] 而不是 `penaltyDeath()`：
     * 现存违规的数量还未知（`SettingsManager` 的 `*Sync` 家族在 core/store 之外就有
     * 76 个调用点），直接 death 会让 debug 包起不来，结果必然是有人把整段删掉。
     * 等这批清干净后，再单独把 `detectDiskReads` 升级为 death 并配棘轮。
     *
     * 只在 debug 生效：release/smooth 包不受任何影响，这段在 R8 下会被整体裁掉。
     */
    private fun installStrictModeForDebugBuilds() {
        if (!BuildConfig.DEBUG) return

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .build()
        )
    }

    private fun runStartupTask(task: AppStartupTask) {
        when (task.id) {
            "network_module_init" -> NetworkModule.init(this)
            "token_manager_init" -> TokenManager.init(this)
            "wbi_key_restore" -> WbiKeyManager.restoreFromStorage(this)
            "video_repository_init" -> com.android.purebilibili.data.repository.VideoRepository.init(this)
            "background_manager_init" -> BackgroundManager.init(this)
            "player_settings_cache_init" -> com.android.purebilibili.core.store.PlayerSettingsCache.init(this)
            "notification_channel_init" -> createNotificationChannel()
            "playlist_restore" -> initPlaylistRestoreNow()
            "telemetry_init" -> initTelemetryNow()
            "plugin_init" -> initPluginStackNow()
            "dex2oat_profile_install" -> requestDex2OatProfileInstallNow()
        }
    }

    private fun initPlaylistRestoreNow() {
        AppScope.ioScope.launch {
            com.android.purebilibili.feature.video.player.PlaylistManager.init(this@PureApplication)
        }
    }

    private fun initTelemetryNow() {
        initCrashlytics()
        initAnalytics()
        attachTelemetryListener()
    }

    private fun initPluginStackNow() {
        PluginManager.initialize(this)
        PluginManager.register(SponsorBlockPlugin())
        PluginManager.register(AdFilterPlugin())
        PluginManager.register(DanmakuEnhancePlugin())
        PluginManager.register(EyeProtectionPlugin())
        PluginManager.register(TodayWatchPlugin())
        PluginManager.register(CdnRegionPlugin())
        PluginManager.register(HomeFeedAnonymizerPlugin())
        PluginManager.register(DlnaCastPlugin())
        PluginManager.register(GoogleCastPlugin())
        //  [BiliPai 移植] 推荐流过滤(默认关闭, 可在插件中心启用)
        PluginManager.register(com.android.purebilibili.feature.plugin.BiliPaiFeedFilterPlugin())
        Logger.d(PureApplicationRuntimeConfig.TAG, " Plugin system initialized with 11 built-in plugins")

        com.android.purebilibili.core.plugin.json.JsonPluginManager.initialize(this)
        Logger.d(PureApplicationRuntimeConfig.TAG, " JSON plugin system initialized")

        com.android.purebilibili.feature.download.DownloadManager.init(this)

        AppScope.ioScope.launch {
            val sponsorBlockEnabled = com.android.purebilibili.core.store.SettingsManager
                .getSponsorBlockEnabled(this@PureApplication)
                .first()
            PluginManager.setEnabled("sponsor_block", sponsorBlockEnabled)
            Logger.d(PureApplicationRuntimeConfig.TAG, " SponsorBlock plugin synced: enabled=$sponsorBlockEnabled")

            SettingsManager.forceDanmakuDefaults(this@PureApplication)
        }

    }

    private fun requestDex2OatProfileInstallNow() {
        runCatching {
            ProfileInstaller.writeProfile(this)
        }.onSuccess {
            Logger.d(PureApplicationRuntimeConfig.TAG, "📦 Requested ART profile installation for dex2oat")
        }.onFailure { throwable ->
            Logger.w(PureApplicationRuntimeConfig.TAG, "⚠️ ART profile installation request failed", throwable)
        }
    }

    private fun attachTelemetryListener() {
        // 监听全局前后台状态，增强会话与崩溃上下文
        BackgroundManager.addListener(telemetryListener)
        if (!BackgroundManager.isInBackground) {
            AnalyticsHelper.onAppForeground()
            CrashReporter.setAppForegroundState(true)
        }
    }
    
    //  初始化 Firebase Crashlytics
    private fun initCrashlytics() {
        try {
            //  读取用户设置（默认开启）
            val prefs = getSharedPreferences("crash_tracking", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("enabled", DEFAULT_CRASH_TRACKING_ENABLED)
            
            CrashReporter.init(this)
            CrashReporter.installGlobalExceptionHandler()
            CrashReporter.setEnabled(enabled)
            
            if (enabled) {
                CrashReporter.syncUserContext(
                    mid = TokenManager.midCache,
                    isVip = TokenManager.isVipCache,
                    privacyModeEnabled = SettingsManager.isPrivacyModeEnabledSync(this)
                )
            }
            
            Logger.d(PureApplicationRuntimeConfig.TAG, " Firebase Crashlytics initialized (enabled=$enabled)")
        } catch (e: Exception) {
            android.util.Log.e(PureApplicationRuntimeConfig.TAG, "Failed to init Crashlytics", e)
        }
    }
    
    // � 初始化 Firebase Analytics
    private fun initAnalytics() {
        try {
            // 初始化 AnalyticsHelper
            AnalyticsHelper.init(this)
            
            //  读取用户设置（默认开启）
            val prefs = getSharedPreferences("analytics_tracking", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("enabled", DEFAULT_ANALYTICS_ENABLED)
            
            //  根据用户设置启用/禁用 Analytics
            AnalyticsHelper.setEnabled(enabled)
            
            if (enabled) {
                AnalyticsHelper.syncUserContext(
                    mid = TokenManager.midCache,
                    isVip = TokenManager.isVipCache,
                    privacyModeEnabled = SettingsManager.isPrivacyModeEnabledSync(this)
                )
                // 记录应用打开事件
                AnalyticsHelper.logAppOpen()
                AnalyticsHelper.logDailyActive(source = "app_start")
            }
            
            Logger.d(PureApplicationRuntimeConfig.TAG, " Firebase Analytics initialized (enabled=$enabled)")
        } catch (e: Exception) {
            android.util.Log.e(PureApplicationRuntimeConfig.TAG, "Failed to init Analytics", e)
        }
    }
    
    // [后台内存优化] 响应系统内存警告
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        val plan = PureApplicationRuntimeConfig.resolveBackgroundMemoryTrimPlan(level)
        if (plan.imageCacheTrimLevel != null) {
            _imageLoader?.memoryCache?.trimMemory(plan.imageCacheTrimLevel)
            if (plan.requestGcHint) {
                System.gc()
            }
            when {
                plan.clearImageMemoryCache -> {
                    Logger.d(PureApplicationRuntimeConfig.TAG, "🚨 trim(level=$level), released image memory cache")
                }
                level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                    Logger.d(PureApplicationRuntimeConfig.TAG, " UI hidden, trimmed image memory cache for background")
                }
                else -> {
                    Logger.d(PureApplicationRuntimeConfig.TAG, " Low memory trim(level=$level), trimmed image memory cache")
                }
            }
        }
        if (plan.notifyPlayerHeavyOptimization) {
            com.android.purebilibili.feature.video.player.MiniPlayerManager
                .getInstanceOrNull()
                ?.onMemoryPressureTrim(
                    level = level,
                    requestIdlePlaybackRelease = plan.requestIdlePlaybackRelease
                )
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        val plan = PureApplicationRuntimeConfig.resolveBackgroundMemoryTrimPlan(
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE
        )
        _imageLoader?.memoryCache?.clear()
        if (plan.notifyPlayerHeavyOptimization) {
            com.android.purebilibili.feature.video.player.MiniPlayerManager
                .getInstanceOrNull()
                ?.onMemoryPressureTrim(
                    level = ComponentCallbacks2.TRIM_MEMORY_COMPLETE,
                    requestIdlePlaybackRelease = plan.requestIdlePlaybackRelease
                )
        }
        Logger.d(PureApplicationRuntimeConfig.TAG, "🚨 onLowMemory, cleared all caches")
    }

    private fun createNotificationChannel() {
        // 仅在 Android 8.0 (API 26) 及以上需要通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            resolveAppNotificationChannels().forEach { spec ->
                val channel = NotificationChannel(spec.id, spec.name, spec.importance).apply {
                    description = spec.description
                    setShowBadge(spec.showBadge)
                    if (spec.silent) {
                        setSound(null, null)
                    }
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    /**
     *  应用主题偏好 - 在 Splash Screen 显示前调用
     * 
     * 这解决了：用户在应用内强制深色模式，但系统是浅色时，启动屏仍然是白色的问题。
     * 通过 AppCompatDelegate.setDefaultNightMode() 强制系统使用正确的深色/浅色模式。
     */
    private fun applyThemePreference() {
        // 同步读取保存的主题设置（必须同步，因为 Splash Screen 马上就会显示）
        val prefs = getSharedPreferences("theme_cache", Context.MODE_PRIVATE)
        val themeModeValue = prefs.getInt("theme_mode", 0)  // 0 = FOLLOW_SYSTEM
        val appLanguage = SettingsManager.getAppLanguageSync(this)
        val themeMode = resolveThemeModePreference(themeModeValue)
        
        val nightMode = when (themeMode) {
            AppThemeMode.FOLLOW_SYSTEM -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            AppThemeMode.LIGHT -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            AppThemeMode.DARK -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
        }
        
        applyAppLanguage(appLanguage)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode)
        Logger.d(
            PureApplicationRuntimeConfig.TAG,
            " Applied launch preferences: themeMode=$themeModeValue -> resolvedMode=$themeMode, nightMode=$nightMode, appLanguage=${appLanguage.name}"
        )
    }
    
    /**
     *  同步应用图标状态
     * 
     * 在 Application.onCreate 时调用，确保启动器图标与用户偏好一致。
     * 
     * 修复：重装后检测 icon 偏好与 Manifest 默认状态冲突，自动重置为默认图标。
     */
}
