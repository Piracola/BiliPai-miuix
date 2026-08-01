package com.android.purebilibili.navigation3

import android.app.Application
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberNavigationEventState
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ProvideAnimatedVisibilityScope
import com.android.purebilibili.feature.settings.isSettingsSubtreeNavKey
import com.android.purebilibili.feature.settings.shouldUseSettingsSplitLayout

internal class BiliPaiProgrammaticBackDispatcher {
    private var callback: (() -> Unit)? = null

    fun register(callback: () -> Unit) {
        this.callback = callback
    }

    fun unregister(callback: () -> Unit) {
        if (this.callback === callback) {
            this.callback = null
        }
    }

    fun dispatch(): Boolean {
        val action = callback ?: return false
        action()
        return true
    }
}

/**
 * Navigation3 的唯一显示宿主。
 *
 * 普通导航统一使用原生页面栈 Push/Pop；预测返回仍只交给 Navigation3/AOSP，
 * 不叠加项目的卡片、快照、模糊或回弹效果。系统关闭动画时直接切换终态。
 */
@Composable
internal fun BiliPaiNavDisplayHost(
    backStack: List<BiliPaiNavKey>,
    reduceMotion: Boolean = false,
    predictiveBackEnabled: Boolean = true,
    programmaticBackDispatcher: BiliPaiProgrammaticBackDispatcher,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (BiliPaiNavKey) -> Unit,
) {
    val safeBackStack = remember(backStack) {
        backStack.ifEmpty { listOf(BiliPaiNavKey.MainHost) }
    }
    var previousBackStack by remember { mutableStateOf(safeBackStack) }
    val application = LocalContext.current.applicationContext as Application
    var navigationEventState: NavigationEventState<SceneInfo<BiliPaiNavKey>>? = null
    val latestOnBack = rememberUpdatedState(onBack)
    val performBack: (() -> Unit) -> Unit = remember {
        { commitTransition ->
            commitTransition()
            latestOnBack.value()
        }
    }
    val latestProgrammaticBackAction = rememberUpdatedState<() -> Unit> {
        performBack { }
    }
    DisposableEffect(programmaticBackDispatcher) {
        val callback = { latestProgrammaticBackAction.value() }
        programmaticBackDispatcher.register(callback)
        onDispose { programmaticBackDispatcher.unregister(callback) }
    }

    val scopedContent: @Composable (BiliPaiNavKey) -> Unit = remember(content, application) {
        { key ->
            Box(modifier = Modifier.fillMaxSize()) {
                ProvideAnimatedVisibilityScope(
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                ) {
                    ProvideNavigation3ViewModelApplicationExtras(application) {
                        content(key)
                    }
                }
            }
        }
    }
    val entryProvider = remember(
        scopedContent,
    ) {
        biliPaiNavEntryProvider(
            content = scopedContent,
        )
    }
    val entries = rememberDecoratedNavEntries(
        backStack = safeBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider,
    )
    val sceneState = rememberSceneState(
        entries = entries,
        sceneStrategies = listOf(SinglePaneSceneStrategy()),
        sceneDecoratorStrategies = emptyList(),
        sharedTransitionScope = null,
        onBack = { performBack { } },
    )
    val scene = sceneState.currentScene
    navigationEventState = rememberNavigationEventState(sceneState)
    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = scene.previousEntries.isNotEmpty(),
        reportPredictiveProgress = predictiveBackEnabled,
        onBackCompleted = performBack,
        onBackCancelled = { commitTransition -> commitTransition() },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppSurfaceTokens.groupedListContainer()),
    ) {
        val layoutDirection = LocalLayoutDirection.current
        val configuration = LocalConfiguration.current
        val isTabletSettingsInternalTransition =
            shouldUseSettingsSplitLayout(configuration.screenWidthDp) &&
                previousBackStack.lastOrNull()?.let(::isSettingsSubtreeNavKey) == true &&
                safeBackStack.lastOrNull()?.let(::isSettingsSubtreeNavKey) == true
        val fallbackRouteTransition = if (reduceMotion) {
            BiliPaiNavRouteTransition.REDUCED_MOTION
        } else {
            BiliPaiNavRouteTransition.STACK
        }
        val fallbackForwardTransform = remember(fallbackRouteTransition, layoutDirection) {
            resolveBiliPaiNavContentTransform(fallbackRouteTransition, layoutDirection)
        }
        val fallbackPopTransform = remember(fallbackRouteTransition, layoutDirection) {
            resolveBiliPaiNavPopContentTransform(fallbackRouteTransition, layoutDirection)
        }
        val settingsInternalTransform = EnterTransition.None togetherWith ExitTransition.None
        SideEffect { previousBackStack = safeBackStack }
        NavDisplay(
            sceneState = sceneState,
            navigationEventState = navigationEventState,
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
            sizeTransform = null,
            transitionSpec = {
                if (isTabletSettingsInternalTransition) settingsInternalTransform else fallbackForwardTransform
            },
            popTransitionSpec = {
                if (isTabletSettingsInternalTransition) settingsInternalTransform else fallbackPopTransform
            },
            predictivePopTransitionSpec = {
                if (isTabletSettingsInternalTransition) settingsInternalTransform else fallbackPopTransform
            },
        )
    }
}

@Composable
private fun ProvideNavigation3ViewModelApplicationExtras(
    application: Application,
    content: @Composable () -> Unit,
) {
    val navEntryOwner = LocalViewModelStoreOwner.current
    if (navEntryOwner == null) {
        content()
        return
    }

    val patchedOwner = remember(navEntryOwner, application) {
        buildNavigation3ViewModelStoreOwner(navEntryOwner, application)
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides patchedOwner) {
        content()
    }
}

private fun buildNavigation3ViewModelStoreOwner(
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
