package com.android.purebilibili.feature.home

import com.android.purebilibili.core.ui.AppSpacingTokens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.store.HomeWallpaperEffectMode
import com.android.purebilibili.core.theme.LocalCornerRadiusScale
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_COVER_ASPECT_RATIO

@Composable
internal fun rememberHomeFeedSkeletonPulse(): Float {
    return 0f
}

@Composable
internal fun HomeFeedSkeletonCard(
    pulse: Float,
    wallpaperTintEnabled: Boolean,
    wallpaperEffectMode: HomeWallpaperEffectMode,
    isDataSaverActive: Boolean,
    coverAspectRatio: Float = VIDEO_SHARED_COVER_ASPECT_RATIO,
    modifier: Modifier = Modifier
) {
    val cardCornerRadius = AppShapes.containerCornerDp(ContainerLevel.Card)
    val cardShape = AppShapes.container(ContainerLevel.Card)
    val isDarkCardTheme = AppSurfaceTokens.chromeBackground().luminance() < 0.5f
    val infoSurfaceAppearance = remember(
        wallpaperTintEnabled,
        wallpaperEffectMode,
        isDarkCardTheme,
        isDataSaverActive
    ) {
        resolveHomeCardInfoSurfaceAppearance(
            wallpaperTintEnabled = wallpaperTintEnabled,
            wallpaperEffectMode = wallpaperEffectMode,
            isDarkTheme = isDarkCardTheme,
            isDataSaverActive = isDataSaverActive
        )
    }
    val blockColor = rememberHomeFeedSkeletonBlockColor(
        pulse = pulse,
        isDarkTheme = isDarkCardTheme
    )
    val coverShape = remember(cardCornerRadius, infoSurfaceAppearance.useTintedSurface) {
        if (infoSurfaceAppearance.useTintedSurface) {
            resolveHomeSkeletonCoverShape(cardCornerRadius)
        } else {
            cardShape
        }
    }
    val infoSurfaceShape = remember(cardCornerRadius) {
        resolveHomeSkeletonInfoShape(cardCornerRadius)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = AppSpacingTokens.Medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(coverAspectRatio)
                .clip(coverShape)
                .background(blockColor)
        )

        val infoModifier = if (infoSurfaceAppearance.useTintedSurface) {
            Modifier
                .fillMaxWidth()
                .background(
                    color = AppSurfaceTokens.cardContainer()
                        .copy(alpha = infoSurfaceAppearance.containerAlpha),
                    shape = infoSurfaceShape
                )
                .border(
                    border = BorderStroke(
                        width = AppSpacingTokens.Micro * 0.4f,
                        color = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = infoSurfaceAppearance.borderAlpha)
                    ),
                    shape = infoSurfaceShape
                )
                .padding(horizontal = AppSpacingTokens.Small + AppSpacingTokens.Micro, vertical = AppSpacingTokens.Small)
        } else {
            Modifier.fillMaxWidth()
        }

        Column(modifier = infoModifier) {
            if (!infoSurfaceAppearance.useTintedSurface) {
                Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
            }
            HomeFeedSkeletonTitleRow(blockColor = blockColor)
            Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro))
            HomeFeedSkeletonMetaRow(blockColor = blockColor)
        }
    }
}

@Composable
private fun HomeFeedSkeletonTitleRow(blockColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            HomeFeedSkeletonBlock(
                color = blockColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSpacingTokens.Large)
            )
            Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
            HomeFeedSkeletonBlock(
                color = blockColor,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(AppSpacingTokens.Large)
            )
        }
        Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
        HomeFeedSkeletonBlock(
            color = blockColor,
            modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
            shape = CircleShape
        )
    }
}

@Composable
private fun HomeFeedSkeletonMetaRow(blockColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro)
    ) {
        HomeFeedSkeletonBlock(
            color = blockColor,
            modifier = Modifier
                .width(AppSpacingTokens.ExtraLarge + AppSpacingTokens.ExtraSmall)
                .height(AppSpacingTokens.Medium + AppSpacingTokens.Micro)
        )
        HomeFeedSkeletonBlock(
            color = blockColor,
            modifier = Modifier
                .width(AppSpacingTokens.TripleExtraLarge * 2)
                .height(AppSpacingTokens.Medium + AppSpacingTokens.Micro)
        )
    }
}

@Composable
private fun HomeFeedSkeletonBlock(
    color: Color,
    modifier: Modifier,
    shape: androidx.compose.ui.graphics.Shape? = null
) {
    val resolvedShape = shape ?: AppShapes.container(ContainerLevel.Tag)
    Box(
        modifier = modifier
            .clip(resolvedShape)
            .background(color)
    )
}

@Composable
private fun rememberHomeFeedSkeletonBlockColor(
    pulse: Float,
    isDarkTheme: Boolean
): Color {
    val alpha = if (isDarkTheme) HOME_FEED_SKELETON_DARK_MIN_ALPHA else HOME_FEED_SKELETON_LIGHT_MIN_ALPHA
    return MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
}

private const val HOME_FEED_SKELETON_LIGHT_MIN_ALPHA = 0.06f
private const val HOME_FEED_SKELETON_DARK_MIN_ALPHA = 0.10f
