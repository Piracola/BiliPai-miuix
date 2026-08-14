package com.android.purebilibili.core.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.iOSSystemGray4
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens
import com.android.purebilibili.core.ui.motion.AppMotionTokens

data class AdaptiveBottomSheetVisualSpec(
    val cornerRadiusDp: Int,
    val useMaterialDragHandle: Boolean
)

internal data class AdaptiveBottomSheetMotionSpec(
    val scrimEnterDurationMillis: Int,
    val scrimExitDurationMillis: Int,
    val contentEnterFadeDurationMillis: Int,
    val contentExitFadeDurationMillis: Int
)

fun resolveAdaptiveBottomSheetVisualSpec(): AdaptiveBottomSheetVisualSpec {
    val cornerRadiusDp = AppShapes.resolveContainerCornerDp(
        level = ContainerLevel.Pill,
    ).value.toInt()
    return AdaptiveBottomSheetVisualSpec(
        cornerRadiusDp = cornerRadiusDp,
        useMaterialDragHandle = true,
    )
}

internal fun resolveAdaptiveBottomSheetMotionSpec(): AdaptiveBottomSheetMotionSpec {
    val tokens = resolveAndroidNativeChromeTokens()
    return AdaptiveBottomSheetMotionSpec(
        scrimEnterDurationMillis = tokens.motionEmphasizedMillis,
        scrimExitDurationMillis = tokens.expressiveMotionDurationMillis,
        contentEnterFadeDurationMillis = tokens.motionEmphasizedMillis,
        contentExitFadeDurationMillis = tokens.expressiveMotionDurationMillis
    )
}

/**
 * 弹层宿主契约：App 风格对应的 BottomSheet 宿主实现。
 *
 * [MIUIX_OVERLAY] 对应 Miuix OverlayBottomSheet。OverlayBottomSheet 依赖
 * Miuix overlay popup host（仅 AdaptiveScaffold 挂载），直接替换会导致无 popup
 * host 的页面点击无效或弹层不显示 —— 不允许机械替换。业务页不允许自行判断宿主，
 * 宿主感知场景（如筛选弹层）必须消费 [resolveBottomSheetHost]，而不是复制判断逻辑。
 */
enum class BottomSheetHost {
    /** Miuix OverlayBottomSheet：依赖 Miuix overlay popup host。 */
    MIUIX_OVERLAY,
}

fun resolveBottomSheetHost(): BottomSheetHost = BottomSheetHost.MIUIX_OVERLAY

internal fun bottomSheetScrimEnterTransition(): EnterTransition = fadeIn(
    AppMotionTokens.resolveBottomSheetFadeEnterSpec()
)

internal fun bottomSheetScrimExitTransition(): ExitTransition = fadeOut(
    AppMotionTokens.resolveBottomSheetFadeExitSpec()
)

internal fun bottomSheetContentEnterTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = AppMotionTokens.resolveBottomSheetSlideSpec()
    ) + fadeIn(
        AppMotionTokens.resolveBottomSheetFadeEnterSpec()
    )
}

internal fun bottomSheetContentExitTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = AppMotionTokens.resolveBottomSheetSlideExitSpec()
    ) + fadeOut(
        AppMotionTokens.resolveBottomSheetFadeExitSpec<Float>()
    )
}

/**
 * App 通用 Modal Bottom Sheet facade。
 *
 * 使用 Material3 ModalBottomSheet 作为中性宿主：即使宿主契约
 * （[resolveBottomSheetHost]）在 MIUIX 下解析为 [BottomSheetHost.MIUIX_OVERLAY]，
 * 本 facade 也不做机械替换 —— OverlayBottomSheet 依赖 Miuix overlay popup host
 * （仅 AdaptiveScaffold 的 MIUIX 模式挂载），而本 facade 的调用点无法保证处于该
 * 宿主之下。需要 Miuix overlay 宿主的场景由宿主感知 facade 消费
 * [resolveBottomSheetHost]。两值风格在此仅做视觉区分（容器色、圆角、拖拽条、动效）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape: Shape? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    presentationProgress: Float = 1f,
    dragHandle: @Composable (() -> Unit)? = { AppBottomSheetDragHandle() },
    windowInsets: androidx.compose.foundation.layout.WindowInsets = androidx.compose.material3.BottomSheetDefaults.modalWindowInsets,
    content: @Composable ColumnScope.() -> Unit
) {
    val visualSpec = remember {
        resolveAdaptiveBottomSheetVisualSpec()
    }
    val adaptiveSheetShape = remember(visualSpec) {
        RoundedCornerShape(
            topStart = visualSpec.cornerRadiusDp.dp,
            topEnd = visualSpec.cornerRadiusDp.dp,
        )
    }
    val sheetShape = shape ?: adaptiveSheetShape
    val progressVisual = resolveInteractiveOverlayProgressVisual(
        presentationProgress = presentationProgress,
        surfaceType = InteractiveOverlaySurfaceType.BOTTOM_SHEET,
        blurActive = true,
        maxScrimAlpha = scrimColor.alpha
    )
    val resolvedContainerColor = MaterialTheme.colorScheme.surfaceContainer.let { color ->
        color.copy(alpha = color.alpha * progressVisual.surfaceAlphaMultiplier)
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = sheetShape,
        containerColor = resolvedContainerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor.copy(alpha = progressVisual.scrimAlpha),
        dragHandle = { AppBottomSheetDragHandle() },
        contentWindowInsets = { windowInsets },
        content = {
            content()
        }
    )
}

data class AppBottomSheetMotion(
    val scrimEnter: EnterTransition,
    val scrimExit: ExitTransition,
    val contentEnter: EnterTransition,
    val contentExit: ExitTransition,
    val scrimEnterDurationMillis: Int,
    val scrimExitDurationMillis: Int,
    val contentEnterFadeDurationMillis: Int,
    val contentExitFadeDurationMillis: Int,
)

@Composable
fun rememberAppBottomSheetMotion(): AppBottomSheetMotion {
    return remember {
        val motionSpec = resolveAdaptiveBottomSheetMotionSpec()
        AppBottomSheetMotion(
            scrimEnter = bottomSheetScrimEnterTransition(),
            scrimExit = bottomSheetScrimExitTransition(),
            contentEnter = bottomSheetContentEnterTransition(),
            contentExit = bottomSheetContentExitTransition(),
            scrimEnterDurationMillis = motionSpec.scrimEnterDurationMillis,
            scrimExitDurationMillis = motionSpec.scrimExitDurationMillis,
            contentEnterFadeDurationMillis = motionSpec.contentEnterFadeDurationMillis,
            contentExitFadeDurationMillis = motionSpec.contentExitFadeDurationMillis,
        )
    }
}

@Composable
fun AppBottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(iOSSystemGray4.copy(alpha = 0.4f))
        )
    }
}
