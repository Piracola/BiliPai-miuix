package com.android.purebilibili.feature.video.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.blur.BlurSurfaceType
import com.android.purebilibili.core.ui.blur.hazeSourceCompat
import com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState
import com.android.purebilibili.core.ui.blur.unifiedBlur
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect

internal const val VIDEO_STATUS_BAR_AMBIENT_CAPTURE_INTERVAL_MS = 66L
internal const val VIDEO_STATUS_BAR_AMBIENT_SAMPLE_WIDTH_PX = 96
internal const val VIDEO_STATUS_BAR_AMBIENT_SAMPLE_HEIGHT_PX = 54

internal fun resolveVideoStatusBarAmbientHazeStyle(): HazeBlurStyle = HazeBlurStyle(
    backgroundColor = Color.Black,
    colorEffects = emptyList(),
    blurRadius = 24.dp,
    noiseFactor = 0f,
    fallbackColorEffect = HazeColorEffect.tint(Color.Black),
)

/**
 * 播放器顶部为系统状态栏预留的背景条，保证系统状态图标在视频画面上清晰可见。
 *
 * [useAmbientHaze] 开启（「播放页沉浸状态栏」开关）时，实时采样播放画面做毛玻璃模糊，
 * 状态栏背景跟随视频画面变化；关闭时保持纯黑背景（默认），视觉统一且零采样开销。
 * 黑色同时作为首帧与采样失败的兜底。
 */
@Composable
internal fun ImmersiveStatusBarBackdrop(
    ambientFrame: State<ImageBitmap?>?,
    height: Dp,
    useAmbientHaze: Boolean,
    modifier: Modifier = Modifier,
) {
    if (height.value <= 0f) return
    val currentAmbientFrame = if (useAmbientHaze) ambientFrame?.value else null
    val hazeState = rememberRecoverableHazeState()
    val colorFaithfulHazeStyle = remember { resolveVideoStatusBarAmbientHazeStyle() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Color.Black),
    ) {
        if (currentAmbientFrame != null) {
            Image(
                bitmap = currentAmbientFrame,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSourceCompat(hazeState),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .unifiedBlur(
                        hazeState = hazeState,
                        surfaceType = BlurSurfaceType.HEADER,
                        blurStyleOverride = colorFaithfulHazeStyle,
                    )
                    .background(Color.Black.copy(alpha = 0.34f)),
            )
        }
    }
}
