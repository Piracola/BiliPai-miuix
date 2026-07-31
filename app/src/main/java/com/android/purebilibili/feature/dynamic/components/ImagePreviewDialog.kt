// 文件路径: feature/dynamic/components/ImagePreviewDialog.kt
package com.android.purebilibili.feature.dynamic.components
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppChromeSizeTokens

import com.android.purebilibili.core.ui.MediaContrastPalette

import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//  Cupertino Icons - iOS SF Symbols 风格图标
import androidx.compose.material3.*
import com.android.purebilibili.core.ui.components.AppFilledIconButton
import com.android.purebilibili.core.ui.components.AppIconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import android.app.Activity
import android.content.ClipData
import android.content.ContextWrapper
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.toArgb
import com.android.purebilibili.core.ui.LocalPredictiveBackGestureEnabled
import com.android.purebilibili.core.ui.rememberAppShareIcon
import com.android.purebilibili.core.ui.rememberAppLikeFilledIcon
import com.android.purebilibili.core.ui.rememberAppLikeIcon
import com.android.purebilibili.core.ui.rememberAppClearIcon
import com.android.purebilibili.core.ui.rememberAppDownloadIcon
import com.android.purebilibili.core.ui.rememberAppVisibilityOffIcon
import com.android.purebilibili.core.ui.rememberAppVisibilityOnIcon
import com.android.purebilibili.core.ui.AdaptiveLoadingIndicator
import com.android.purebilibili.core.ui.motion.continuityTween
import com.android.purebilibili.core.ui.motion.emphasizedEnterTween
import com.android.purebilibili.core.ui.motion.emphasizedExitTween
import com.android.purebilibili.core.ui.motion.indicatorSpring
import com.android.purebilibili.core.ui.motion.interactiveSnapSpring
import com.android.purebilibili.core.ui.motion.softLandingSpring
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.core.util.rememberHapticFeedback
import java.io.File
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/**
 *  图片预览对话框 - 支持左右滑动切换和3D立体动画
 */

internal const val IMAGE_PREVIEW_BACKDROP_TAG = "image_preview_backdrop"
internal const val IMAGE_PREVIEW_PAGE_TAG = "image_preview_page"
internal const val IMAGE_PREVIEW_COMMENT_PANEL_TAG = "image_preview_comment_panel"
internal const val IMAGE_PREVIEW_ORIGINAL_CHIP_TAG = "image_preview_original_chip"
private const val IMAGE_PREVIEW_SHARE_CACHE_MAX_AGE_MS = 24L * 60L * 60L * 1000L

private data class ImagePreviewOverlayRequest(
    val token: Long,
    val images: List<String>,
    val initialIndex: Int,
    val sourceRect: androidx.compose.ui.geometry.Rect?,
    val sourceCornerRadiusDp: Float,
    val textContent: ImagePreviewTextContent?,
    val defaultTextVisible: Boolean,
    val onImageLongPress: ((String) -> Unit)?,
    val onDismiss: () -> Unit
)

private object ImagePreviewOverlayController {
    private val _request = MutableStateFlow<ImagePreviewOverlayRequest?>(null)
    val request = _request.asStateFlow()

    fun show(request: ImagePreviewOverlayRequest) {
        _request.value = request
    }

    fun dismiss(token: Long? = null) {
        val current = _request.value ?: return
        if (token == null || current.token == token) {
            _request.value = null
        }
    }
}

@Composable
fun ImagePreviewDialog(
    images: List<String>,
    initialIndex: Int,
    sourceRect: androidx.compose.ui.geometry.Rect? = null,
    sourceCornerRadiusDp: Float = resolveDrawGridCornerRadiusDp().toFloat(),
    textContent: ImagePreviewTextContent? = null,
    defaultTextVisible: Boolean = true,
    onImageLongPress: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    val requestToken = remember(images, initialIndex, sourceRect, sourceCornerRadiusDp) { System.nanoTime() }

    LaunchedEffect(requestToken) {
        ImagePreviewOverlayController.show(
            ImagePreviewOverlayRequest(
                token = requestToken,
                images = images,
                initialIndex = initialIndex,
                sourceRect = sourceRect,
                sourceCornerRadiusDp = sourceCornerRadiusDp,
                textContent = textContent,
                defaultTextVisible = defaultTextVisible,
                onImageLongPress = onImageLongPress,
                onDismiss = { latestOnDismiss() }
            )
        )
    }

    DisposableEffect(requestToken) {
        onDispose {
            ImagePreviewOverlayController.dismiss(requestToken)
        }
    }
}

@Composable
fun ImagePreviewOverlayHost(
    modifier: Modifier = Modifier
) {
    val activeRequest by ImagePreviewOverlayController.request.collectAsStateWithLifecycle()
    activeRequest?.let { request ->
        Dialog(
            onDismissRequest = {
                ImagePreviewOverlayController.dismiss(request.token)
                request.onDismiss()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            ImagePreviewOverlayContent(
                images = request.images,
                initialIndex = request.initialIndex,
                sourceRect = request.sourceRect,
                sourceCornerRadiusDp = request.sourceCornerRadiusDp,
                textContent = request.textContent,
                defaultTextVisible = request.defaultTextVisible,
                onImageLongPress = request.onImageLongPress,
                onDismiss = {
                    ImagePreviewOverlayController.dismiss(request.token)
                    request.onDismiss()
                },
                modifier = modifier
                    .fillMaxSize()
                    .zIndex(100f)
            )
        }
    }
}

@Composable
private fun ImagePreviewOverlayContent(
    images: List<String>,
    initialIndex: Int,
    sourceRect: androidx.compose.ui.geometry.Rect? = null,
    sourceCornerRadiusDp: Float = resolveDrawGridCornerRadiusDp().toFloat(),
    textContent: ImagePreviewTextContent? = null,
    defaultTextVisible: Boolean = true,
    onImageLongPress: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()
    val shareIcon = rememberAppShareIcon()
    val likeIcon = rememberAppLikeIcon()
    val likeFilledIcon = rememberAppLikeFilledIcon()
    val commentContext = textContent?.commentContext
    val useCommentPreviewChrome = commentContext != null
    var isSaving by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }
    
    //  获取 Activity 和 Window 用于沉浸式控制
    val activity = remember {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }
    val window = remember { activity?.window }
    val insetsController = remember {
        window?.let { WindowCompat.getInsetsController(it, it.decorView) }
    }
    
    //  保存原始导航栏颜色
    val originalNavBarColor = remember { window?.navigationBarColor ?: android.graphics.Color.BLACK }
    
    //  进入时设置沉浸式导航栏（透明黑色），退出时恢复
    DisposableEffect(Unit) {
        window?.navigationBarColor = Color.Transparent.toArgb()
        insetsController?.isAppearanceLightNavigationBars = false
        
        onDispose {
            window?.navigationBarColor = originalNavBarColor
        }
    }
    
    //  动画状态控制
    // 0f = 关闭/初始状态 (at sourceRect), 1f = 打开状态 (Fullscreen)
    val animateTrigger = remember { androidx.compose.animation.core.Animatable(0f) }
    var isDismissing by remember { mutableStateOf(false) }
    var currentImageDisplayRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var dismissImageDisplayRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var activeZoomScale by remember { mutableFloatStateOf(1f) }
    var isVerticalDismissDragging by remember { mutableStateOf(false) }
    val longPressSaveEnabled by SettingsManager.getImagePreviewLongPressSaveEnabled(context)
        .collectAsStateWithLifecycle(initialValue = true)
    var imagePreviewTextVisible by remember(textContent, defaultTextVisible) {
        mutableStateOf(
            resolveImagePreviewInitialTextVisibility(
                hasText = textContent != null,
                defaultVisible = defaultTextVisible
            )
        )
    }
    // 竖滑跟手用状态值，避免每帧 launch snapTo 竞态导致滑不动。
    var verticalDismissOffsetYPx by remember { mutableFloatStateOf(0f) }
    val verticalDismissSnapAnim = remember { androidx.compose.animation.core.Animatable(0f) }

    fun handleImageSaveResult(success: Boolean) {
        haptic(resolveImagePreviewSaveFeedback(success))
        Toast.makeText(
            context,
            if (success) "图片已保存到相册" else "保存失败，请重试",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun handleImageShareResult(success: Boolean) {
        haptic(resolveImagePreviewSaveFeedback(success))
        if (!success) {
            Toast.makeText(context, "分享失败，请重试", Toast.LENGTH_SHORT).show()
        }
    }

    //  GIF 图片加载器
    val gifImageLoader = context.imageLoader

    //  使用 HorizontalPager 实现滑动切换
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { images.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        activeZoomScale = 1f
        if (!isDismissing) {
            isVerticalDismissDragging = false
            verticalDismissOffsetYPx = 0f
            verticalDismissSnapAnim.snapTo(0f)
        }
    }
    
    //  存储权限状态（Android 9 及以下需要）
    var pendingSaveUrl by remember { mutableStateOf<String?>(null) }
    val storagePermission = com.android.purebilibili.core.util.rememberStoragePermissionState { granted ->
        if (granted && pendingSaveUrl != null) {
            // 权限授予后执行保存
            isSaving = true
            scope.launch {
                val success = saveImageToGallery(context, pendingSaveUrl!!)
                isSaving = false
                pendingSaveUrl = null
                withContext(Dispatchers.Main) {
                    handleImageSaveResult(success)
                }
            }
        }
    }

    fun requestSaveCurrentImage(imageUrl: String) {
        if (imageUrl.isEmpty() || isSaving) return
        if (onImageLongPress != null) {
            onImageLongPress(imageUrl)
            return
        }
        if (storagePermission.isGranted) {
            isSaving = true
            scope.launch {
                val success = saveImageToGallery(context, imageUrl)
                isSaving = false
                withContext(Dispatchers.Main) {
                    handleImageSaveResult(success)
                }
            }
        } else {
            pendingSaveUrl = imageUrl
            storagePermission.request()
        }
    }

    fun requestShareCurrentImage(imageUrl: String) {
        if (imageUrl.isEmpty() || isSharing) return
        isSharing = true
        scope.launch {
            val success = shareImageFromPreview(context, imageUrl)
            isSharing = false
            withContext(Dispatchers.Main) {
                handleImageShareResult(success)
            }
        }
    }
    
    // 当前页的图片 URL
    val currentImageUrl = remember(pagerState.currentPage, images) {
        normalizeImageUrl(images.getOrNull(pagerState.currentPage) ?: "")
    }
    
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
            val constraints = this
            val fullWidth = constraints.maxWidth
            val fullHeight = constraints.maxHeight
            val fullWidthPx = with(density) { fullWidth.toPx() }
            val fullHeightPx = with(density) { fullHeight.toPx() }
            val maxBlurRadiusPx = with(density) {
                (AppSpacingTokens.Large + AppSpacingTokens.Micro).toPx()
            }
            
            val rawProgress = animateTrigger.value
            val verticalDragFrame = resolveImagePreviewVerticalDragFrame(
                dragOffsetYPx = verticalDismissOffsetYPx,
                containerHeightPx = fullHeightPx
            )
            
            //  计算容器位置和大小
            // 如果切走了或者没有源矩形，则全屏显示（仅淡入淡出）
            // 有缩略图源矩形时始终做尺寸落位，保证返回大小匹配预览格。
            val shouldUseRectAnim = sourceRect != null
            val transitionFrame = resolveImagePreviewTransitionFrame(
                rawProgress = rawProgress,
                hasSourceRect = shouldUseRectAnim,
                sourceCornerRadiusDp = sourceCornerRadiusDp
            )
            val presentedCornerRadiusDp = resolveImagePreviewPresentedCornerRadiusDp(
                visualProgress = transitionFrame.visualProgress,
                verticalDragProgress = if (isDismissing) 0f else verticalDragFrame.progress,
                hasSourceRect = shouldUseRectAnim,
                sourceCornerRadiusDp = sourceCornerRadiusDp
            )
            val visualFrame = resolveImagePreviewVisualFrame(
                visualProgress = transitionFrame.visualProgress,
                transitionEnabled = !isDismissing,
                maxBlurRadiusPx = maxBlurRadiusPx
            )
            val backdropAlpha = if (isDismissing) {
                resolveImagePreviewDismissBackdropAlpha(transitionFrame.visualProgress)
            } else {
                visualFrame.backdropAlpha * verticalDragFrame.backdropAlphaMultiplier
            }
            val dismissRectFrame = resolveImagePreviewDismissRectFrame(
                transitionProgress = transitionFrame.layoutProgress,
                sourceRect = if (shouldUseRectAnim && isDismissing) sourceRect else null,
                displayedImageRect = if (shouldUseRectAnim && isDismissing) dismissImageDisplayRect else null
            )
            
            val targetLeft = AppSpacingTokens.None
            val targetTop = AppSpacingTokens.None
            val targetWidth = fullWidth
            val targetHeight = fullHeight
            val previewSurfaceRect = remember(constraints.maxWidth, constraints.maxHeight) {
                androidx.compose.ui.geometry.Rect(
                    left = 0f,
                    top = 0f,
                    right = with(density) { constraints.maxWidth.toPx() },
                    bottom = with(density) { constraints.maxHeight.toPx() }
                )
            }

            LaunchedEffect(Unit) {
                animateTrigger.snapTo(1f)
            }

            fun triggerDismiss(
                startRect: androidx.compose.ui.geometry.Rect? = resolveImagePreviewDismissStartRect(
                    previewSurfaceRect = previewSurfaceRect,
                    displayedImageRect = currentImageDisplayRect,
                    // 从真实显示图区域飞回缩略图，黑边不参与 morph，观感更干净。
                    preferPreviewSurface = false
                )
            ) {
                if (isDismissing) return
                dismissImageDisplayRect = startRect
                isVerticalDismissDragging = false
                isDismissing = true
                scope.launch {
                    verticalDismissOffsetYPx = 0f
                    verticalDismissSnapAnim.snapTo(0f)
                    animateTrigger.snapTo(0f)
                    onDismiss()
                }
            }

            val backEventState = rememberNavigationEventState(NavigationEventInfo.None)
            val predictiveBackGestureEnabled = LocalPredictiveBackGestureEnabled.current
            val backProgress =
                if (predictiveBackGestureEnabled) {
                    (backEventState.transitionState as? NavigationEventTransitionState.InProgress)
                        ?.latestEvent
                        ?.progress
                        ?: 0f
                } else {
                    0f
                }
            LaunchedEffect(backProgress, isDismissing) {
                if (!isDismissing && backProgress > 0f) {
                    animateTrigger.snapTo(1f - backProgress)
                }
            }
            NavigationBackHandler(
                state = backEventState,
                isBackEnabled = !isDismissing,
                reportPredictiveProgress = predictiveBackGestureEnabled,
                onBackCancelled = { commitTransition: () -> Unit ->
                    scope.launch {
                        animateTrigger.snapTo(1f)
                        commitTransition()
                    }
                },
                onBackCompleted = { commitTransition: () -> Unit ->
                    triggerDismiss()
                    commitTransition()
                },
            )
            
            val (currentLeft, currentTop, currentWidth, currentHeight) = if (shouldUseRectAnim) {
                val source = sourceRect
                val sourceLeft = with(density) { source.left.toDp() }
                val sourceTop = with(density) { source.top.toDp() }
                val sourceWidth = with(density) { source.width.toDp() }
                val sourceHeight = with(density) { source.height.toDp() }
                
                val l = androidx.compose.ui.unit.lerp(sourceLeft, targetLeft, transitionFrame.layoutProgress)
                val t = androidx.compose.ui.unit.lerp(sourceTop, targetTop, transitionFrame.layoutProgress)
                val w = androidx.compose.ui.unit.lerp(sourceWidth, targetWidth, transitionFrame.layoutProgress)
                val h = androidx.compose.ui.unit.lerp(sourceHeight, targetHeight, transitionFrame.layoutProgress)
                
                Quad(l, t, w, h)
            } else {
                Quad(AppSpacingTokens.None, AppSpacingTokens.None, fullWidth, fullHeight)
            }
            
            // 1. 背景层 (淡入淡出)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(IMAGE_PREVIEW_BACKDROP_TAG)
                    .background(MediaContrastPalette.Scrim.copy(alpha = backdropAlpha))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { triggerDismiss() }
                        )
                    }
            )
            
            // 2. 内容层 (缩放位移)
            val contentModifier = if (isDismissing && shouldUseRectAnim && dismissRectFrame != null) {
                Modifier
                    .offset(
                        x = with(density) { dismissRectFrame.rect.left.toDp() },
                        y = with(density) { dismissRectFrame.rect.top.toDp() }
                    )
                    .size(
                        width = with(density) { dismissRectFrame.rect.width.toDp() },
                        height = with(density) { dismissRectFrame.rect.height.toDp() }
                    )
                    .clip(RoundedCornerShape(presentedCornerRadiusDp.dp))
                    .graphicsLayer {
                        alpha = visualFrame.contentAlpha
                        renderEffect = null
                    }
            } else {
                Modifier
                    .offset(x = currentLeft, y = currentTop)
                    .size(width = currentWidth, height = currentHeight)
                    .clip(RoundedCornerShape(presentedCornerRadiusDp.dp))
                    .graphicsLayer {
                        alpha = visualFrame.contentAlpha
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            visualFrame.blurRadiusPx > 0.01f
                        ) {
                            renderEffect = RenderEffect.createBlurEffect(
                                visualFrame.blurRadiusPx,
                                visualFrame.blurRadiusPx,
                                Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        } else {
                            renderEffect = null
                        }
                        if (!shouldUseRectAnim) {
                            scaleX = transitionFrame.fallbackScale
                            scaleY = transitionFrame.fallbackScale
                        }
                        if (!isDismissing) {
                            translationY = verticalDismissOffsetYPx
                            val dragScale = verticalDragFrame.scale
                            scaleX *= dragScale
                            scaleY *= dragScale
                            transformOrigin = TransformOrigin.Center
                        }
                    }
            }

            Box(
                 modifier = contentModifier
            ) {
                //  使用 HorizontalPager 实现滑动切换 + 3D立体动画
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,  // 预加载相邻页面
                    userScrollEnabled = !isVerticalDismissDragging &&
                        !isDismissing &&
                        activeZoomScale <= 1.01f,
                    key = { images.getOrElse(it) { "" } }
                ) { page ->
                    // 🎭 3D 立体旋转动画 - Cube 效果
                    // 仅当完全打开时才应用复杂变换，避免动画冲突
                    val apply3D = transitionFrame.visualProgress > 0.92f

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(IMAGE_PREVIEW_PAGE_TAG)
                            .graphicsLayer {
                                // 页面偏移（0 = 居中，-1 = 左边，1 = 右边）在这里读取而不是
                                // 组合期。currentPageOffsetFraction 横滑时每帧都变，
                                // 在组合期读取等于把每一帧都升级成一次重组；
                                // 放进 graphicsLayer lambda 后只触发重绘，不触发重组。
                                val pageOffset =
                                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                if (apply3D) {
                                    if (useCommentPreviewChrome) {
                                        val transform = resolveCommentImagePreviewPageTransform(
                                            pageOffsetFraction = pageOffset,
                                            containerWidthPx = fullWidthPx
                                        )
                                        rotationY = transform.rotationY
                                        translationX = transform.translationXPx
                                        cameraDistance = 8f * density.density
                                        transformOrigin = TransformOrigin(
                                            pivotFractionX = transform.pivotFractionX,
                                            pivotFractionY = 0.5f
                                        )
                                        scaleX = transform.scale
                                        scaleY = transform.scale
                                        alpha = transform.alpha
                                    } else {
                                        //  3D 旋转角度（最大45度）
                                        val rotationAngle = pageOffset * 45f
                                        rotationY = rotationAngle

                                        //  设置旋转中心点
                                        cameraDistance = 12f * density.density
                                        transformOrigin = TransformOrigin(
                                            pivotFractionX = if (pageOffset < 0) 1f else 0f,
                                            pivotFractionY = 0.5f
                                        )

                                        //  缩放效果
                                        val scale = 1f - (abs(pageOffset) * 0.1f).coerceIn(0f, 0.15f)
                                        scaleX = scale
                                        scaleY = scale

                                        //  透明度渐变
                                        alpha = 1f - (abs(pageOffset) * 0.3f).coerceIn(0f, 0.5f)
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                // 阻止点击穿透到关闭手势
                                detectTapGestures { 
                                     if (!useCommentPreviewChrome) {
                                         // 点击图片也关闭
                                         triggerDismiss()
                                     }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val imageUrl = remember(images.getOrNull(page)) {
                            normalizeImageUrl(images.getOrNull(page) ?: "")
                        }
                        val decodeSize = remember {
                            resolveImageDecodeSize(ImageDecodeTarget.FULLSCREEN_PREVIEW)
                        }
                        
                        ZoomableImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                // 预览必须采样解码，避免超大原图超过 Canvas 单位图绘制上限。
                                .size(decodeSize.widthPx, decodeSize.heightPx)
                                .addHeader("Referer", "https://www.bilibili.com/")
                                // 退出 morph 时关闭 crossfade，避免尺寸变化触发二次淡入发黏。
                                .crossfade(!isDismissing)
                                .build(),
                            contentDescription = null,
                            imageLoader = gifImageLoader,  //  使用 GIF 加载器
                            modifier = Modifier.fillMaxSize(),
                            onZoomChange = {
                                activeZoomScale = it
                            },
                            onDisplayRectChange = { rect ->
                                if (!isDismissing && page == pagerState.currentPage) {
                                    currentImageDisplayRect = rect
                                }
                            },
                            onVerticalDismissDragStart = {
                                if (page == pagerState.currentPage && !isDismissing) {
                                    isVerticalDismissDragging = true
                                    scope.launch { verticalDismissSnapAnim.stop() }
                                }
                            },
                            onVerticalDismissDrag = { dragDelta ->
                                if (page == pagerState.currentPage && !isDismissing && isVerticalDismissDragging) {
                                    verticalDismissOffsetYPx += dragDelta
                                }
                            },
                            onVerticalDismissDragEnd = {
                                if (page == pagerState.currentPage && !isDismissing && isVerticalDismissDragging) {
                                    isVerticalDismissDragging = false
                                    val draggedRect = resolveImagePreviewDraggedDisplayRect(
                                        displayedImageRect = currentImageDisplayRect,
                                        translationYPx = verticalDismissOffsetYPx,
                                        scale = verticalDragFrame.scale
                                    )
                                    when (
                                        resolveImagePreviewVerticalDismissDecision(
                                            dragOffsetYPx = verticalDismissOffsetYPx,
                                            containerHeightPx = fullHeightPx
                                        )
                                    ) {
                                        ImagePreviewVerticalDismissDecision.DISMISS -> triggerDismiss(draggedRect)
                                        ImagePreviewVerticalDismissDecision.SNAP_BACK -> {
                                            scope.launch {
                                                verticalDismissSnapAnim.snapTo(verticalDismissOffsetYPx)
                                                verticalDismissSnapAnim.snapTo(0f)
                                                verticalDismissOffsetYPx = 0f
                                            }
                                        }
                                    }
                                }
                            },
                            onVerticalDismissDragCancel = {
                                if (page == pagerState.currentPage && !isDismissing) {
                                    isVerticalDismissDragging = false
                                    scope.launch {
                                        verticalDismissSnapAnim.snapTo(verticalDismissOffsetYPx)
                                        verticalDismissSnapAnim.snapTo(0f)
                                        verticalDismissOffsetYPx = 0f
                                    }
                                }
                            },
                            onLongPress = {
                                if (
                                    page == pagerState.currentPage &&
                                    shouldHandleImagePreviewLongPressSave(
                                        longPressSaveEnabled = longPressSaveEnabled,
                                        imageUrl = imageUrl,
                                        isSaving = isSaving
                                    )
                                ) {
                                    haptic(resolveImagePreviewLongPressSaveStartFeedback())
                                    requestSaveCurrentImage(imageUrl)
                                }
                            },
                            onClick = {
                                if (!useCommentPreviewChrome) {
                                    // 点击图片关闭预览
                                    triggerDismiss()
                                }
                            }
                        )
                    }
                }
            }
            
            // 3. UI 覆盖层 - 退出时先于图片清掉 chrome，只剩干净一镜 morph
            val chromeAlpha = resolveImagePreviewChromeAlpha(
                visualProgress = transitionFrame.visualProgress,
                isDismissing = isDismissing
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = chromeAlpha }
            ) {
                val safeDrawingPadding = WindowInsets.safeDrawing.asPaddingValues()
                val overlayPadding = resolveImagePreviewOverlayPadding(
                    safeInsetStart = safeDrawingPadding.calculateStartPadding(layoutDirection),
                    safeInsetTop = safeDrawingPadding.calculateTopPadding(),
                    safeInsetEnd = safeDrawingPadding.calculateEndPadding(layoutDirection),
                    safeInsetBottom = safeDrawingPadding.calculateBottomPadding()
                )
                val resolvedText = resolveImagePreviewText(
                    textContent = textContent,
                    currentPage = pagerState.currentPage,
                    totalPages = images.size
                )
                val textPlacement = textContent?.placement ?: ImagePreviewTextPlacement.OVERLAY_BOTTOM
                val shouldShowResolvedText = shouldShowImagePreviewText(
                    hasText = resolvedText != null,
                    textVisible = imagePreviewTextVisible
                )

                if (!useCommentPreviewChrome &&
                    resolvedText != null &&
                    shouldShowResolvedText &&
                    textPlacement == ImagePreviewTextPlacement.OVERLAY_BOTTOM
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(
                                start = overlayPadding.start + AppSpacingTokens.Small,
                                end = overlayPadding.end + AppSpacingTokens.Small,
                                bottom = overlayPadding.bottom + AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Large + AppSpacingTokens.Micro
                            )
                            .graphicsLayer {
                                // transform 在这里就地求值：它只依赖 currentPageOffsetFraction，
                                // 而那个值横滑时每帧都变，放在组合期会拖着整段文字浮层一起重组。
                                val textTransform = resolveImagePreviewTextTransform(
                                    pageOffsetFraction = pagerState.currentPageOffsetFraction
                                )
                                alpha = textTransform.alpha
                                rotationX = textTransform.rotationX
                                translationY = textTransform.translateYDp.dp.toPx()
                                cameraDistance = 10f * this.density
                                transformOrigin = TransformOrigin(0.5f, 1f)
                            }
                            .clickable {
                                imagePreviewTextVisible =
                                    resolveImagePreviewTextVisibilityAfterToggle(imagePreviewTextVisible)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .widthIn(max = AppSpacingTokens.TripleExtraLarge * 11 + AppSpacingTokens.DoubleExtraLarge)
                                .clip(AppShapes.container(ContainerLevel.Sheet))
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            MediaContrastPalette.Scrim.copy(alpha = 0.72f),
                                            MediaContrastPalette.Scrim.copy(alpha = 0.56f)
                                        )
                                    )
                                )
                                .padding(horizontal = AppSpacingTokens.Large, vertical = AppSpacingTokens.Medium + AppSpacingTokens.Micro / 2)
                        ) {
                            AnimatedContent(
                                targetState = pagerState.currentPage,
                                transitionSpec = {
                                    (fadeIn(animationSpec = emphasizedEnterTween(250)) + slideInVertically(
                                        animationSpec = emphasizedEnterTween(250)
                                    ) { it / 3 }) togetherWith
                                        (fadeOut(animationSpec = emphasizedExitTween(180)) + slideOutVertically(
                                            animationSpec = emphasizedExitTween(180)
                                        ) { -it / 4 })
                                },
                                label = "imagePreviewTextSwitch"
                            ) { page ->
                                val currentText = resolveImagePreviewText(
                                    textContent = textContent,
                                    currentPage = page,
                                    totalPages = images.size
                                ) ?: resolvedText
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro)
                                ) {
                                    if (currentText.headline.isNotBlank() || currentText.pageIndicator.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small + AppSpacingTokens.Micro),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (currentText.headline.isNotBlank()) {
                                                AppText(
                                                    text = currentText.headline,
                                                    color = MediaContrastPalette.Foreground.copy(alpha = 0.9f),
                                                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                            }
                                            if (currentText.pageIndicator.isNotBlank()) {
                                                AppText(
                                                    text = currentText.pageIndicator,
                                                    color = MediaContrastPalette.Foreground.copy(alpha = 0.64f),
                                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                                )
                                            }
                                        }
                                    }
                                    if (currentText.body.isNotBlank()) {
                                        AppText(
                                            text = currentText.body,
                                            color = MediaContrastPalette.Foreground.copy(alpha = 0.94f),
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                                            maxLines = 4,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                //  页码指示器（圆点样式）
                if (!useCommentPreviewChrome && images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = overlayPadding.bottom)
                            .background(MediaContrastPalette.Scrim.copy(0.5f), AppShapes.container(ContainerLevel.Dialog))
                            .padding(horizontal = AppSpacingTokens.Medium, vertical = AppSpacingTokens.Small),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        images.forEachIndexed { index, _ ->
                            val isSelected = pagerState.currentPage == index
                            // 动画过渡
                            val dotSize by animateFloatAsState(
                                targetValue = if (isSelected) 10f else 6f,
                                animationSpec = indicatorSpring(),
                                label = "dotSize"
                            )
                            val dotAlpha by animateFloatAsState(
                                targetValue = if (isSelected) 1f else 0.5f,
                                animationSpec = softLandingSpring(),
                                label = "dotAlpha"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(dotSize.dp)
                                    .clip(CircleShape)
                                    .background(MediaContrastPalette.Foreground.copy(alpha = dotAlpha))
                                    .clickable {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            )
                        }
                    }
                }
                
                val chromeModifier = Modifier.graphicsLayer {
                    // 同上：横滑期间 currentPageOffsetFraction 每帧变化，
                    // 原先在组合期读取会让整个 chrome（顶栏 + 底栏 + 页码）每帧重组。
                    // graphicsLayer 的 lambda 本身就是 Density，不需要外部的 with(density)。
                    val chromeOffset = pagerState.currentPageOffsetFraction.coerceIn(-1f, 1f)
                    rotationZ = -chromeOffset * 2.8f
                    translationX = (-chromeOffset * 10f).dp.toPx()
                    transformOrigin = TransformOrigin.Center
                }

                // 顶部按钮栏（关闭 + 页码 + 下载）
                if (commentContext != null) {
                    ImagePreviewCommentTopBar(
                        label = commentContext.originalSizeLabels.getOrNull(pagerState.currentPage)
                            ?: resolveCommentImageOriginalSizeLabel(null),
                        shareIcon = shareIcon,
                        isSharing = isSharing,
                        enabled = !isSharing && !isSaving,
                        onDismiss = { triggerDismiss() },
                        onShare = { requestShareCurrentImage(currentImageUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(
                                start = overlayPadding.start,
                                top = overlayPadding.top,
                                end = overlayPadding.end
                            )
                            .then(chromeModifier)
                    )
                } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(
                            start = overlayPadding.start,
                            top = overlayPadding.top,
                            end = overlayPadding.end
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 关闭按钮
                    AppFilledIconButton(
                        onClick = { triggerDismiss() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MediaContrastPalette.Scrim.copy(0.5f)
                        )
                    ) {
                        AppIcon(
                            imageVector = rememberAppClearIcon(),
                            contentDescription = "关闭",
                            tint = MediaContrastPalette.Foreground
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = AppSpacingTokens.Medium),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            resolvedText != null && shouldShowResolvedText && textPlacement == ImagePreviewTextPlacement.TOP_BAR -> {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        val textTransform = resolveImagePreviewTextTransform(
                                            pageOffsetFraction = pagerState.currentPageOffsetFraction
                                        )
                                        alpha = textTransform.alpha
                                        translationY = (textTransform.translateYDp * 0.45f).dp.toPx()
                                    }
                                ) {
                                    AnimatedContent(
                                        targetState = pagerState.currentPage,
                                        transitionSpec = {
                                            val isForward = targetState > initialState
                                            (fadeIn(animationSpec = emphasizedEnterTween(220)) +
                                                slideInHorizontally { fullWidth ->
                                                    if (isForward) fullWidth / 3 else -fullWidth / 3
                                                }) togetherWith
                                                (fadeOut(animationSpec = emphasizedExitTween(160)) +
                                                    slideOutHorizontally { fullWidth ->
                                                        if (isForward) -fullWidth / 4 else fullWidth / 4
                                                    })
                                        },
                                        label = "imagePreviewTopBarTextSwitch"
                                    ) { page ->
                                        val pageText = resolveImagePreviewText(
                                            textContent = textContent,
                                            currentPage = page,
                                            totalPages = images.size
                                        ) ?: resolvedText
                                        val primaryText = pageText.body.ifBlank { pageText.headline }
                                        val secondaryText = if (
                                            pageText.body.isNotBlank() &&
                                            pageText.headline.isNotBlank()
                                        ) {
                                            pageText.headline
                                        } else {
                                            ""
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Micro)
                                        ) {
                                            if (secondaryText.isNotBlank()) {
                                                AppText(
                                                    text = secondaryText,
                                                    color = MediaContrastPalette.Foreground.copy(alpha = 0.82f),
                                                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                            if (primaryText.isNotBlank()) {
                                                AppText(
                                                    text = primaryText,
                                                    color = MediaContrastPalette.Foreground,
                                                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                                    maxLines = 2,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    modifier = Modifier
                                                        .background(MediaContrastPalette.Scrim.copy(0.5f), AppShapes.container(ContainerLevel.Card))
                                                        .padding(horizontal = AppSpacingTokens.Medium, vertical = AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro)
                                                )
                                            }
                                            if (images.size > 1) {
                                                AppText(
                                                    text = "${page + 1} / ${images.size}",
                                                    color = MediaContrastPalette.Foreground.copy(alpha = 0.8f),
                                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            images.size > 1 -> {
                                AppText(
                                    "${pagerState.currentPage + 1} / ${images.size}",
                                    color = MediaContrastPalette.Foreground,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    modifier = Modifier
                                        .background(MediaContrastPalette.Scrim.copy(0.5f), AppShapes.container(ContainerLevel.Card))
                                        .padding(horizontal = AppSpacingTokens.Medium, vertical = AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro)
                                )
                            }
                        }
                    }

                    if (resolvedText != null) {
                        AppFilledIconButton(
                            onClick = {
                                imagePreviewTextVisible =
                                    resolveImagePreviewTextVisibilityAfterToggle(imagePreviewTextVisible)
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MediaContrastPalette.Scrim.copy(0.5f)
                            )
                        ) {
                            AppIcon(
                                imageVector = if (imagePreviewTextVisible) {
                                    rememberAppVisibilityOffIcon()
                                } else {
                                    rememberAppVisibilityOnIcon()
                                },
                                contentDescription = if (imagePreviewTextVisible) "隐藏图片文字" else "显示图片文字",
                                tint = MediaContrastPalette.Foreground
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                    }
                    
                    // 分享按钮
                    AppFilledIconButton(
                        onClick = {
                            requestShareCurrentImage(currentImageUrl)
                        },
                        enabled = !isSharing && !isSaving,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MediaContrastPalette.Scrim.copy(0.5f)
                        )
                    ) {
                        if (isSharing) {
                            AdaptiveLoadingIndicator(
                                size = AppSpacingTokens.ExtraLarge,
                                color = MediaContrastPalette.Foreground,
                                strokeWidth = AppSpacingTokens.Micro
                            )
                        } else {
                            AppIcon(
                                imageVector = shareIcon,
                                contentDescription = "分享图片",
                                tint = MediaContrastPalette.Foreground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(AppSpacingTokens.Small))

                    //  下载按钮
                    AppFilledIconButton(
                        onClick = {
                            requestSaveCurrentImage(currentImageUrl)
                        },
                        enabled = !isSaving && !isSharing,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MediaContrastPalette.Scrim.copy(0.5f)
                        )
                    ) {
                        if (isSaving) {
                            AdaptiveLoadingIndicator(
                                size = AppSpacingTokens.ExtraLarge,
                                color = MediaContrastPalette.Foreground,
                                strokeWidth = AppSpacingTokens.Micro
                            )
                        } else {
                            AppIcon(
                                imageVector = rememberAppDownloadIcon(),
                                contentDescription = "保存图片",
                                tint = MediaContrastPalette.Foreground
                            )
                        }
                    }
                }
                }

                if (commentContext != null) {
                    ImagePreviewCommentPanel(
                        context = commentContext,
                        likeIcon = likeIcon,
                        likeFilledIcon = likeFilledIcon,
                        shareIcon = shareIcon,
                        isSharing = isSharing,
                        enabled = !isSharing && !isSaving,
                        onShare = { requestShareCurrentImage(currentImageUrl) },
                        onReply = {
                            commentContext.onReplyClick?.invoke()
                            triggerDismiss()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(
                                start = overlayPadding.start,
                                end = overlayPadding.end,
                                bottom = overlayPadding.bottom + AppSpacingTokens.Medium
                            )
                            .then(chromeModifier)
                    )
                }
            }
    }
}

@Composable
private fun ImagePreviewCommentTopBar(
    label: String,
    shareIcon: ImageVector,
    isSharing: Boolean,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconButton(
            onClick = onDismiss,
            modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget)
        ) {
            AppIcon(
                imageVector = rememberAppClearIcon(),
                contentDescription = "关闭",
                tint = MediaContrastPalette.Foreground,
                modifier = Modifier.size(AppSpacingTokens.ExtraLarge)
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AppText(
                text = label,
                color = MediaContrastPalette.Foreground.copy(alpha = 0.9f),
                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .testTag(IMAGE_PREVIEW_ORIGINAL_CHIP_TAG)
                    .clip(AppShapes.container(ContainerLevel.Floating))
                    .background(MediaContrastPalette.Foreground.copy(alpha = 0.16f))
                    .padding(horizontal = AppSpacingTokens.Large + AppSpacingTokens.Micro, vertical = AppSpacingTokens.Small - AppSpacingTokens.Micro / 2)
            )
        }
        AppIconButton(
            onClick = onShare,
            enabled = enabled,
            modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget)
        ) {
            if (isSharing) {
                AdaptiveLoadingIndicator(
                    size = AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro,
                    color = MediaContrastPalette.Foreground,
                    strokeWidth = AppSpacingTokens.Micro
                )
            } else {
                AppIcon(
                    imageVector = shareIcon,
                    contentDescription = "分享图片",
                    tint = MediaContrastPalette.Foreground,
                    modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro / 2)
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewCommentPanel(
    context: ImagePreviewCommentContext,
    likeIcon: ImageVector,
    likeFilledIcon: ImageVector,
    shareIcon: ImageVector,
    isSharing: Boolean,
    enabled: Boolean,
    onShare: () -> Unit,
    onReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localLiked by remember(context.replyId, context.liked) { mutableStateOf(context.liked) }
    var localLikeCount by remember(context.replyId, context.likeCount) { mutableIntStateOf(context.likeCount) }
    val displayLikeCount = remember(localLikeCount) {
        FormatUtils.formatStat(localLikeCount.coerceAtLeast(0).toLong())
    }

    Column(
        modifier = modifier.testTag(IMAGE_PREVIEW_COMMENT_PANEL_TAG),
        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small + AppSpacingTokens.Micro)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = context.avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro)
                    .clip(CircleShape)
                    .background(MediaContrastPalette.Foreground.copy(alpha = 0.16f))
            )
            Spacer(modifier = Modifier.width(AppSpacingTokens.Small + AppSpacingTokens.Micro))
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = context.authorName,
                    color = MediaContrastPalette.Foreground,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (context.timeText.isNotBlank()) {
                    AppText(
                        text = context.timeText,
                        color = MediaContrastPalette.Foreground.copy(alpha = 0.58f),
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        maxLines = 1
                    )
                }
            }
        }

        if (context.body.isNotBlank()) {
            AppText(
                text = context.body,
                color = MediaContrastPalette.Foreground.copy(alpha = 0.94f),
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro)
                    .clip(AppShapes.container(ContainerLevel.Floating))
                    .background(MediaContrastPalette.Foreground.copy(alpha = 0.12f))
                    .clickable(enabled = context.onReplyClick != null, onClick = onReply)
                    .padding(horizontal = AppSpacingTokens.Medium + AppSpacingTokens.Micro),
                contentAlignment = Alignment.CenterStart
            ) {
                AppText(
                    text = "回复 ${context.authorName}",
                    color = MediaContrastPalette.Foreground.copy(alpha = 0.56f),
                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(AppSpacingTokens.Large))
            ImagePreviewCommentActionButton(
                icon = if (localLiked) likeFilledIcon else likeIcon,
                label = displayLikeCount,
                selected = localLiked,
                enabled = context.onLikeClick != null,
                onClick = {
                    context.onLikeClick?.invoke()
                    if (!localLiked) {
                        localLiked = true
                        localLikeCount += 1
                    } else {
                        localLiked = false
                        localLikeCount = (localLikeCount - 1).coerceAtLeast(0)
                    }
                }
            )
            Spacer(modifier = Modifier.width(AppSpacingTokens.Medium + AppSpacingTokens.Micro))
            ImagePreviewCommentActionButton(
                icon = shareIcon,
                label = "转发",
                selected = false,
                enabled = enabled,
                onClick = onShare,
                busy = isSharing
            )
        }
    }
}

@Composable
private fun ImagePreviewCommentActionButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    busy: Boolean = false
) {
    Column(
        modifier = Modifier
            .size(width = AppSpacingTokens.TripleExtraLarge - AppSpacingTokens.Micro, height = AppSpacingTokens.TripleExtraLarge)
            .clickable(enabled = enabled && !busy, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (busy) {
            AdaptiveLoadingIndicator(
                size = AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro,
                color = MediaContrastPalette.Foreground,
                strokeWidth = AppSpacingTokens.Micro
            )
        } else {
            AppIcon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MediaContrastPalette.Foreground,
                modifier = Modifier.size(AppSpacingTokens.ExtraLarge)
            )
        }
        AppText(
            text = label,
            color = MediaContrastPalette.Foreground.copy(alpha = if (enabled) 0.88f else 0.38f),
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

// 辅助数据类
data class Quad(val left: androidx.compose.ui.unit.Dp, val top: androidx.compose.ui.unit.Dp, val width: androidx.compose.ui.unit.Dp, val height: androidx.compose.ui.unit.Dp)

/**
 *  规范化图片 URL
 * 1. 修复协议头（http -> https, // -> https://）
 * 2. 移除分辨率限制参数（@...）以获取原图
 */
private fun normalizeImageUrl(rawSrc: String): String {
    val trimmed = rawSrc.trim()
    var result = when {
        trimmed.startsWith("https://") -> trimmed
        trimmed.startsWith("http://") -> trimmed.replace("http://", "https://")
        trimmed.startsWith("//") -> "https:$trimmed"
        trimmed.isNotEmpty() -> "https://$trimmed"
        else -> ""
    }
    
    //  移除 Bilibili 图片尺寸参数（例如 @640w_400h.webp）以获取最高质量
    if (result.contains("@")) {
        result = result.substringBefore("@")
    }
    
    return result
}

internal fun resolveImageShareMimeType(imageUrl: String): String {
    val normalizedUrl = imageUrl.substringBefore('?').substringBefore('@').lowercase()
    return when {
        normalizedUrl.endsWith(".gif") -> "image/gif"
        normalizedUrl.endsWith(".webp") -> "image/webp"
        normalizedUrl.endsWith(".png") -> "image/png"
        else -> "image/jpeg"
    }
}

private fun resolveImageShareExtension(mimeType: String): String = when (mimeType) {
    "image/gif" -> "gif"
    "image/webp" -> "webp"
    "image/png" -> "png"
    else -> "jpg"
}

/**
 *  分享图片 - 下载原始图片到应用缓存，再通过 FileProvider 交给系统分享面板
 */
suspend fun shareImageFromPreview(context: Context, imageUrl: String): Boolean {
    val normalizedUrl = normalizeImageUrl(imageUrl)
    if (normalizedUrl.isEmpty()) return false
    val mimeType = resolveImageShareMimeType(normalizedUrl)
    val sharedFile = withContext(Dispatchers.IO) {
        createImagePreviewShareFile(context, normalizedUrl, mimeType)
    } ?: return false

    return withContext(Dispatchers.Main) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                sharedFile
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newUri(context.contentResolver, "BiliPai image", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "分享图片").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Log.e("ImagePreview", "Error sharing image", e)
            false
        }
    }
}

private fun createImagePreviewShareFile(
    context: Context,
    imageUrl: String,
    mimeType: String
): File? {
    return try {
        val cacheDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        cleanupImagePreviewShareCache(cacheDir)
        val extension = resolveImageShareExtension(mimeType)
        val outputFile = File(cacheDir, "BiliPai_${System.currentTimeMillis()}.$extension")
        val connection = java.net.URL(imageUrl).openConnection() as java.net.HttpURLConnection
        try {
            connection.setRequestProperty("Referer", "https://www.bilibili.com/")
            connection.connect()
            if (connection.responseCode !in 200..299) {
                Log.e("ImagePreview", "Failed to download for sharing: ${connection.responseCode}")
                return null
            }
            connection.inputStream.use { inputStream ->
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            outputFile
        } finally {
            connection.disconnect()
        }
    } catch (e: Exception) {
        Log.e("ImagePreview", "Error preparing image share", e)
        null
    }
}

private fun cleanupImagePreviewShareCache(cacheDir: File) {
    val expireBefore = System.currentTimeMillis() - IMAGE_PREVIEW_SHARE_CACHE_MAX_AGE_MS
    cacheDir.listFiles()?.forEach { file ->
        if (file.lastModified() < expireBefore) {
            file.delete()
        }
    }
}

/**
 *  保存图片到相册 - 支持 GIF/WebP 等格式保留
 */
suspend fun saveImageToGallery(context: android.content.Context, imageUrl: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            //  检测图片格式
            val isGif = imageUrl.contains(".gif", ignoreCase = true)
            val isWebp = imageUrl.contains(".webp", ignoreCase = true)
            val isPng = imageUrl.contains(".png", ignoreCase = true)
            
            //  对于 GIF/WebP，直接下载原始字节流保留动画
            if (isGif || isWebp) {
                val url = java.net.URL(imageUrl)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.setRequestProperty("Referer", "https://www.bilibili.com/")
                connection.connect()
                
                if (connection.responseCode != 200) {
                    Log.e("ImagePreview", "Failed to download: ${connection.responseCode}")
                    return@withContext false
                }
                
                val inputStream = connection.inputStream
                val bytes = inputStream.readBytes()
                inputStream.close()
                connection.disconnect()
                
                // 生成文件名
                val extension = when {
                    isGif -> "gif"
                    isWebp -> "webp"
                    else -> "jpg"
                }
                val mimeType = when {
                    isGif -> "image/gif"
                    isWebp -> "image/webp"
                    else -> "image/jpeg"
                }
                val fileName = "BiliPai_${System.currentTimeMillis()}.$extension"

                if (saveBytesToCustomImageSaveDirectory(context, bytes, fileName, mimeType)) {
                    Log.d("ImagePreview", "Image saved to custom directory: $fileName")
                    return@withContext true
                }
                
                // 使用 MediaStore 保存
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, resolveDefaultImageMediaStoreRelativePath())
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return@withContext false
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }
                
                Log.d("ImagePreview", "Image saved successfully: $fileName")
                return@withContext true
            }
            
            //  对于 JPEG/PNG 等静态图片，使用 Coil 下载并转换
            val imageLoader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .addHeader("Referer", "https://www.bilibili.com/")
                .build()
            
            val result = imageLoader.execute(request)
            if (result !is SuccessResult) {
                Log.e("ImagePreview", "Failed to download image: $imageUrl")
                return@withContext false
            }
            
            val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            if (bitmap == null) {
                Log.e("ImagePreview", "Failed to convert drawable to bitmap")
                return@withContext false
            }
            
            // 生成文件名
            val extension = if (isPng) "png" else "jpg"
            val mimeType = if (isPng) "image/png" else "image/jpeg"
            val fileName = "BiliPai_${System.currentTimeMillis()}.$extension"
            val format = if (isPng) android.graphics.Bitmap.CompressFormat.PNG else android.graphics.Bitmap.CompressFormat.JPEG

            if (
                saveBitmapToCustomImageSaveDirectory(
                    context = context,
                    bitmap = bitmap,
                    fileName = fileName,
                    format = format,
                    quality = 95,
                    mimeType = mimeType
                )
            ) {
                Log.d("ImagePreview", "Image saved to custom directory: $fileName")
                return@withContext true
            }
            
            // 使用 MediaStore 保存图片
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, resolveDefaultImageMediaStoreRelativePath())
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return@withContext false
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(format, 95, outputStream)
            }
            
            // 标记保存完成
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            
            Log.d("ImagePreview", "Image saved successfully: $fileName")
            true
        } catch (e: Exception) {
            Log.e("ImagePreview", "Error saving image", e)
            false
        }
    }
}
