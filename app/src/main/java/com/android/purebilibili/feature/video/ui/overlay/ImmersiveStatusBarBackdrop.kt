package com.android.purebilibili.feature.video.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.android.purebilibili.core.ui.blur.BlurSurfaceType
import com.android.purebilibili.core.ui.blur.hazeSourceCompat
import com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState
import com.android.purebilibili.core.ui.blur.unifiedBlur

/**
 * Keeps the system status icons visible over an opaque, live ambient strip sampled from playback.
 * Black is retained as the first-frame and capture-failure fallback.
 */
@Composable
internal fun ImmersiveStatusBarBackdrop(
    ambientFrame: State<ImageBitmap?>?,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    if (height.value <= 0f) return
    val hazeState = rememberRecoverableHazeState()
    val currentAmbientFrame = ambientFrame?.value

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
                    )
                    .background(Color.Black.copy(alpha = 0.34f)),
            )
        }
    }
}
