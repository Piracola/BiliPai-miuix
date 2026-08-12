package com.android.purebilibili.feature.video.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.media3.ui.PlayerView
import com.android.purebilibili.feature.video.util.captureVideoScreenshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** Click-time UI position of a parent detail before a related-detail entry covers it. */
internal data class VideoDetailParentUiSnapshot(
    val selectedTabIndex: Int,
    val introItemIndex: Int,
    val introItemOffsetPx: Int,
    val commentItemIndex: Int,
    val commentItemOffsetPx: Int,
)

internal fun captureVideoDetailParentUiSnapshot(
    selectedTabIndex: Int,
    introListState: LazyListState,
    commentListState: LazyListState,
): VideoDetailParentUiSnapshot = VideoDetailParentUiSnapshot(
    selectedTabIndex = selectedTabIndex.coerceAtLeast(0),
    introItemIndex = introListState.firstVisibleItemIndex,
    introItemOffsetPx = introListState.firstVisibleItemScrollOffset,
    commentItemIndex = commentListState.firstVisibleItemIndex,
    commentItemOffsetPx = commentListState.firstVisibleItemScrollOffset,
)

internal suspend fun restoreVideoDetailParentUiSnapshot(
    snapshot: VideoDetailParentUiSnapshot,
    introListState: LazyListState,
    commentListState: LazyListState,
    pagerState: PagerState,
) {
    runCatching {
        pagerState.scrollToPage(
            snapshot.selectedTabIndex.coerceIn(0, (pagerState.pageCount - 1).coerceAtLeast(0)),
        )
    }
    restoreVideoDetailListPosition(
        listState = introListState,
        itemIndex = snapshot.introItemIndex,
        itemOffsetPx = snapshot.introItemOffsetPx,
    )
    restoreVideoDetailListPosition(
        listState = commentListState,
        itemIndex = snapshot.commentItemIndex,
        itemOffsetPx = snapshot.commentItemOffsetPx,
    )
}

private suspend fun restoreVideoDetailListPosition(
    listState: LazyListState,
    itemIndex: Int,
    itemOffsetPx: Int,
) {
    val totalItems = listState.layoutInfo.totalItemsCount
    if (totalItems <= 0) return
    val safeIndex = itemIndex.coerceIn(0, totalItems - 1)
    runCatching {
        listState.scrollToItem(
            index = safeIndex,
            scrollOffset = if (safeIndex == itemIndex) itemOffsetPx.coerceAtLeast(0) else 0,
        )
    }
}

/**
 * Captures the composed window region before detail-to-detail navigation starts.
 *
 * Window PixelCopy is intentional: unlike `View.draw`, it includes the current SurfaceView or
 * TextureView video frame. The fallback still freezes Compose scroll/layout on older devices.
 */
internal suspend fun captureVideoDetailParentFreezeFrame(
    window: Window?,
    view: View,
): Bitmap? = withContext(Dispatchers.Main.immediate) {
    if (!view.isAttachedToWindow || view.width <= 0 || view.height <= 0) return@withContext null
    val width = view.width.coerceAtLeast(1)
    val height = view.height.coerceAtLeast(1)

    if (window == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return@withContext drawVideoDetailParentFallback(view, width, height)
    }

    val location = IntArray(2)
    view.getLocationInWindow(location)
    val decorWidth = window.decorView.width.coerceAtLeast(1)
    val decorHeight = window.decorView.height.coerceAtLeast(1)
    val sourceRect = android.graphics.Rect(
        location[0].coerceIn(0, decorWidth - 1),
        location[1].coerceIn(0, decorHeight - 1),
        (location[0] + width).coerceIn(1, decorWidth),
        (location[1] + height).coerceIn(1, decorHeight),
    )
    if (sourceRect.width() <= 0 || sourceRect.height() <= 0) {
        return@withContext drawVideoDetailParentFallback(view, width, height)
    }

    val bitmap = runCatching {
        Bitmap.createBitmap(sourceRect.width(), sourceRect.height(), Bitmap.Config.ARGB_8888)
    }.getOrNull() ?: return@withContext null

    val capturedPage = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
        runCatching {
            PixelCopy.request(
                window,
                sourceRect,
                bitmap,
                { result ->
                    if (!continuation.isActive) {
                        if (!bitmap.isRecycled) bitmap.recycle()
                    } else if (result == PixelCopy.SUCCESS) {
                        continuation.resume(bitmap)
                    } else {
                        if (!bitmap.isRecycled) bitmap.recycle()
                        continuation.resume(drawVideoDetailParentFallback(view, width, height))
                    }
                },
                Handler(Looper.getMainLooper()),
            )
        }.onFailure {
            if (!bitmap.isRecycled) bitmap.recycle()
            if (continuation.isActive) {
                continuation.resume(drawVideoDetailParentFallback(view, width, height))
            }
        }
    }
    compositeCurrentVideoFrame(
        pageBitmap = capturedPage,
        sourceRect = sourceRect,
        rootView = view,
    )
}

private fun drawVideoDetailParentFallback(view: View, width: Int, height: Int): Bitmap? =
    runCatching {
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            view.draw(Canvas(bitmap))
        }
    }.getOrNull()

/** SurfaceView can be absent from a window copy, so explicitly merge its current frame. */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private suspend fun compositeCurrentVideoFrame(
    pageBitmap: Bitmap?,
    sourceRect: android.graphics.Rect,
    rootView: View,
): Bitmap? {
    val page = pageBitmap?.takeUnless(Bitmap::isRecycled) ?: return null
    val playerView = findLargestVisiblePlayerView(rootView) ?: return page
    val videoSurface = playerView.videoSurfaceView ?: return page
    if (!videoSurface.isShown || videoSurface.width <= 0 || videoSurface.height <= 0) return page
    val playerFrame = captureVideoScreenshot(
        playerView = playerView,
        videoWidth = 0,
        videoHeight = 0,
    ) ?: return page

    try {
        val surfaceLocation = IntArray(2)
        videoSurface.getLocationInWindow(surfaceLocation)
        val scaleX = page.width.toFloat() / sourceRect.width().coerceAtLeast(1)
        val scaleY = page.height.toFloat() / sourceRect.height().coerceAtLeast(1)
        val destination = android.graphics.Rect(
            ((surfaceLocation[0] - sourceRect.left) * scaleX).toInt(),
            ((surfaceLocation[1] - sourceRect.top) * scaleY).toInt(),
            ((surfaceLocation[0] + videoSurface.width - sourceRect.left) * scaleX).toInt(),
            ((surfaceLocation[1] + videoSurface.height - sourceRect.top) * scaleY).toInt(),
        )
        if (!destination.intersect(0, 0, page.width, page.height)) return page
        Canvas(page).drawBitmap(
            playerFrame,
            null,
            destination,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
    } finally {
        if (!playerFrame.isRecycled) playerFrame.recycle()
    }
    return page
}

private fun findLargestVisiblePlayerView(root: View): PlayerView? {
    var best: PlayerView? = null
    var bestArea = 0L

    fun visit(view: View) {
        if (!view.isShown) return
        if (view is PlayerView && view.isAttachedToWindow) {
            val area = view.width.toLong() * view.height.toLong()
            if (area > bestArea) {
                best = view
                bestArea = area
            }
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) visit(view.getChildAt(index))
        }
    }

    visit(root)
    return best
}

/** Opaque top layer retained by the parent detail while its related child is on top. */
@Composable
internal fun BoxScope.VideoDetailParentFreezeFrame(frame: Bitmap?) {
    val visibleFrame = frame?.takeUnless(Bitmap::isRecycled) ?: return
    val imageBitmap = remember(visibleFrame) { visibleFrame.asImageBitmap() }
    DisposableEffect(visibleFrame) {
        onDispose {
            if (!visibleFrame.isRecycled) visibleFrame.recycle()
        }
    }
    Image(
        bitmap = imageBitmap,
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(Float.MAX_VALUE),
    )
}
