package com.android.purebilibili.navigation3

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.navigation3.predictiveback.biliPaiMiuixNavTransition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal class BiliPaiProgrammaticBackDispatcher {
    private var callback: (() -> Unit)? = null

    fun register(callback: () -> Unit) {
        this.callback = callback
    }

    fun unregister(callback: () -> Unit) {
        if (this.callback === callback) this.callback = null
    }

    fun dispatch(): Boolean {
        val action = callback ?: return false
        action()
        return true
    }
}

@Composable
internal fun BiliPaiNavDisplayHost(
    backStack: SnapshotStateList<BiliPaiNavKey>,
    isLightBackground: Boolean = false,
    reduceMotion: Boolean = false,
    programmaticBackDispatcher: BiliPaiProgrammaticBackDispatcher,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (BiliPaiNavKey) -> Unit,
) {
    val application = LocalContext.current.applicationContext as Application
    val stackSnapshot = backStack.toList()
    val latestOnBack by rememberUpdatedState(onBack)
    val performBack = remember(backStack) {
        {
            latestOnBack()
        }
    }

    DisposableEffect(programmaticBackDispatcher, performBack) {
        programmaticBackDispatcher.register(performBack)
        onDispose { programmaticBackDispatcher.unregister(performBack) }
    }

    val globalTransition = remember(isLightBackground) {
        biliPaiMiuixNavTransition(
            isLightBackground = isLightBackground,
        )
    }

    val navCornerRadius = rememberDeviceCornerRadius(defaultRadius = 0.dp)
    val backdropColor = MiuixTheme.colorScheme.surface
    val effects = remember(
        navCornerRadius,
        backdropColor,
    ) {
        NavDisplayEffects(
            enableCornerClip = true,
            cornerClipRadius = navCornerRadius,
            cornerClipMode = NavCornerClipMode.Leading,
            dimAmount = 0.5f,
            backdropColor = backdropColor,
            blockInputDuringTransition = false,
        )
    }
    // 全屏滑动返回默认关闭（仅系统边缘预测返回），可在设置中开启。
    // 开启后仅对列表/设置等纵向页面生效，播放器、详情、WebView 等
    // 横滑冲突页面始终禁用（见 BiliPaiNavEntryProvider）。
    val fullScreenSwipeBackEnabled by
        com.android.purebilibili.core.store.SettingsManager
            .getFullScreenSwipeBackEnabled(LocalContext.current)
            .collectAsStateWithLifecycle(initialValue = false)
    val swipeBackDirection = if (fullScreenSwipeBackEnabled) {
        when (LocalLayoutDirection.current) {
            LayoutDirection.Rtl -> NavSwipeDirection.RightToLeft
            LayoutDirection.Ltr -> NavSwipeDirection.LeftToRight
        }
    } else {
        NavSwipeDirection.None
    }
    val interceptPredictiveBack = backStack.size > 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppSurfaceTokens.groupedListContainer()),
    ) {
        @Suppress("UNCHECKED_CAST")
        NavDisplay(
            backStack = backStack as NavBackStack,
            onBack = performBack,
            transition = globalTransition,
            effects = effects,
        ) {
            biliPaiNavEntries(
                swipeBackDirection = swipeBackDirection,
            ) { key ->
                BiliPaiMiuixNavEntry(
                    interceptPredictiveBack = interceptPredictiveBack,
                    onBack = performBack,
                ) {
                    ProvideMiuixNavViewModelApplicationExtras(application) {
                        content(key)
                    }
                }
            }
        }
    }
}

@Composable
private fun BiliPaiMiuixNavEntry(
    interceptPredictiveBack: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val navigationEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = interceptPredictiveBack,
        onBackCompleted = onBack,
    )
    content()
}

@Composable
private fun ProvideMiuixNavViewModelApplicationExtras(
    application: Application,
    content: @Composable () -> Unit,
) {
    val navEntryOwner = LocalViewModelStoreOwner.current
    if (navEntryOwner == null) {
        content()
        return
    }
    val patchedOwner = remember(navEntryOwner, application) {
        buildMiuixNavViewModelStoreOwner(navEntryOwner, application)
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides patchedOwner) {
        content()
    }
}

private fun buildMiuixNavViewModelStoreOwner(
    navEntryOwner: ViewModelStoreOwner,
    application: Application,
): ViewModelStoreOwner {
    val defaultFactoryOwner = navEntryOwner as? HasDefaultViewModelProviderFactory
    val defaultCreationExtras = defaultFactoryOwner?.defaultViewModelCreationExtras
        ?: CreationExtras.Empty
    val patchedCreationExtras = MutableCreationExtras(defaultCreationExtras).apply {
        set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, application)
    }
    return object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
        override val viewModelStore = navEntryOwner.viewModelStore
        override val defaultViewModelProviderFactory =
            defaultFactoryOwner?.defaultViewModelProviderFactory
                ?: ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        override val defaultViewModelCreationExtras: CreationExtras = patchedCreationExtras
    }
}
