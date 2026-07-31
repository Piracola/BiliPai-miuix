package com.android.purebilibili.feature.video.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel

@Composable
fun VideoCardSkeleton(
    modifier: Modifier = Modifier,
    index: Int = 0,
    coverAspectRatio: Float = 4f / 3f,
) {
    val delay = index * 80
    val cardShape = AppShapes.container(ContainerLevel.Card)
    val detailShape = AppShapes.container(ContainerLevel.Tag)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(bottom = AppSpacingTokens.Medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(coverAspectRatio)
                .clip(cardShape)
                .videoCardShimmer(delayMillis = delay)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(modifier = Modifier.padding(horizontal = AppSpacingTokens.Small)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .clip(detailShape)
                    .videoCardShimmer(delayMillis = delay + 50)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(detailShape)
                    .videoCardShimmer(delayMillis = delay + 100)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .videoCardShimmer(delayMillis = delay + 150)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .clip(detailShape)
                        .videoCardShimmer(delayMillis = delay + 150)
                )
            }
        }
    }
}

private fun Modifier.videoCardShimmer(
    durationMillis: Int = 1500,
    delayMillis: Int = 0,
): Modifier = composed {
    return@composed this.background(MaterialTheme.colorScheme.surfaceVariant)

}
