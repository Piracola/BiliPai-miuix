package com.android.purebilibili.feature.video.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.theme.calculateContrastRatio
import com.android.purebilibili.core.ui.rememberAppBookmarkIcon
import com.android.purebilibili.core.ui.rememberAppCoinIcon
import com.android.purebilibili.core.ui.rememberAppLikeFilledIcon
import com.android.purebilibili.core.ui.rememberAppLikeIcon
import com.android.purebilibili.core.ui.rememberAppShareIcon
import com.android.purebilibili.core.ui.blur.BlurSurfaceType
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.feature.home.components.BottomBarMatchedReusableLiquidDock
import com.android.purebilibili.feature.home.components.resolveBottomBarSurfaceColor
import com.android.purebilibili.feature.home.components.resolveSharedBottomBarCapsuleShape
import dev.chrisbanes.haze.HazeState
import top.yukonga.miuix.kmp.blur.Backdrop
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

internal const val BOTTOM_INPUT_BAR_PLACEHOLDER_MIN_CONTRAST = 4.5f

internal fun resolveBottomInputBarPlaceholderTextColor(
    inputContainerColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color
): Color {
    return listOf(
        onSurfaceColor,
        onSurfaceVariantColor,
        if (inputContainerColor.luminance() < 0.5f) Color.White else Color.Black
    ).firstOrNull { candidate ->
        calculateContrastRatio(candidate, inputContainerColor) >= BOTTOM_INPUT_BAR_PLACEHOLDER_MIN_CONTRAST
    } ?: onSurfaceColor
}

/**
 * Floating liquid-glass chrome for the detail comment/action bar is retired:
 * the detail bar always renders the docked solid surface.
 */
internal fun shouldUseFloatingLiquidBottomInputBar(): Boolean = false

/** The comment bar follows the bottom-bar blur preference when liquid glass is not active. */
internal fun shouldUseFrostedBottomInputBar(
    bottomBarBlurEnabled: Boolean,
    floatingLiquidGlass: Boolean,
    hasHazeState: Boolean
): Boolean =
    bottomBarBlurEnabled &&
        !floatingLiquidGlass &&
        hasHazeState

internal fun resolveBottomInputBarContentBottomPadding(
    showBar: Boolean,
    floatingLiquidGlass: Boolean,
    showActionButtonsFallback: Boolean
): Dp {
    if (!showBar) {
        return if (showActionButtonsFallback) 84.dp else 12.dp
    }
    return if (floatingLiquidGlass) 112.dp else 96.dp
}

@Composable
fun BottomInputBar(
    modifier: Modifier = Modifier,
    isLiked: Boolean,
    isFavorited: Boolean,
    isCoined: Boolean,
    onLikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCoinClick: () -> Unit,
    onShareClick: () -> Unit,
    onCommentClick: () -> Unit,
    backdrop: Backdrop? = null,
    hazeState: HazeState? = null,
    isScrollInProgressProvider: () -> Boolean = { false },
) {
    val context = LocalContext.current
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    val floatingLiquidGlass = shouldUseFloatingLiquidBottomInputBar()
    val frostedBottomBar = shouldUseFrostedBottomInputBar(
        bottomBarBlurEnabled = homeSettings.isBottomBarBlurEnabled,
        floatingLiquidGlass = floatingLiquidGlass,
        hasHazeState = hazeState != null
    )

    DockedSolidBottomInputBar(
        modifier = modifier,
        hazeState = hazeState,
        frostedBottomBar = frostedBottomBar,
        isLiked = isLiked,
        isFavorited = isFavorited,
        isCoined = isCoined,
        onLikeClick = onLikeClick,
        onFavoriteClick = onFavoriteClick,
        onCoinClick = onCoinClick,
        onShareClick = onShareClick,
        onCommentClick = onCommentClick
    )
}

@Composable
private fun DockedSolidBottomInputBar(
    modifier: Modifier,
    hazeState: HazeState?,
    frostedBottomBar: Boolean,
    isLiked: Boolean,
    isFavorited: Boolean,
    isCoined: Boolean,
    onLikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCoinClick: () -> Unit,
    onShareClick: () -> Unit,
    onCommentClick: () -> Unit,
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val bottomBarColor = resolveBottomBarSurfaceColor(
        surfaceColor = surfaceColor,
        blurEnabled = frostedBottomBar,
        blurIntensity = currentUnifiedBlurIntensity()
    )
    val inputContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val inputTextColor = resolveBottomInputBarPlaceholderTextColor(
        inputContainerColor = inputContainerColor,
        onSurfaceColor = MaterialTheme.colorScheme.onSurface,
        onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    AppSurface(
        color = bottomBarColor,
        tonalElevation = if (frostedBottomBar) 0.dp else 8.dp,
        shadowElevation = if (frostedBottomBar) 0.dp else 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (frostedBottomBar && hazeState != null) {
                    Modifier.unifiedBlur(
                        hazeState = hazeState,
                        surfaceType = BlurSurfaceType.BOTTOM_BAR
                    )
                } else {
                    Modifier
                }
            )
    ) {
        BottomInputBarContentRow(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            inputContainerColor = inputContainerColor,
            inputTextColor = inputTextColor,
            isLiked = isLiked,
            isFavorited = isFavorited,
            isCoined = isCoined,
            onLikeClick = onLikeClick,
            onFavoriteClick = onFavoriteClick,
            onCoinClick = onCoinClick,
            onShareClick = onShareClick,
            onCommentClick = onCommentClick
        )
    }
}

@Composable
private fun FloatingLiquidBottomInputBar(
    modifier: Modifier,
    backdrop: Backdrop?,
    isLiked: Boolean,
    isFavorited: Boolean,
    isCoined: Boolean,
    onLikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCoinClick: () -> Unit,
    onShareClick: () -> Unit,
    onCommentClick: () -> Unit,
    isScrollInProgressProvider: () -> Boolean,
) {
    val shellShape = resolveSharedBottomBarCapsuleShape()
    val inputTextColor = resolveBottomInputBarPlaceholderTextColor(
        inputContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        onSurfaceColor = MaterialTheme.colorScheme.onSurface,
        onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val bottomInset = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = bottomInset),
        contentAlignment = Alignment.BottomCenter
    ) {
        BottomBarMatchedReusableLiquidDock(
            shape = shellShape,
            modifier = Modifier.fillMaxWidth(),
            // 外层整条保留液态玻璃（含 shell lens）；内层提示框不再嵌套 liquid dock。
            drawShellLens = true,
            isScrollInProgressProvider = isScrollInProgressProvider
        ) {
            FloatingLiquidBottomInputBarContentRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                commentFieldShape = shellShape,
                inputTextColor = inputTextColor,
                isLiked = isLiked,
                isFavorited = isFavorited,
                isCoined = isCoined,
                onLikeClick = onLikeClick,
                onFavoriteClick = onFavoriteClick,
                onCoinClick = onCoinClick,
                onShareClick = onShareClick,
                onCommentClick = onCommentClick
            )
        }
    }
}

@Composable
private fun FloatingLiquidBottomInputBarContentRow(
    modifier: Modifier,
    commentFieldShape: androidx.compose.ui.graphics.Shape,
    inputTextColor: Color,
    isLiked: Boolean,
    isFavorited: Boolean,
    isCoined: Boolean,
    onLikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCoinClick: () -> Unit,
    onShareClick: () -> Unit,
    onCommentClick: () -> Unit,
) {
    val favoriteIcon = rememberAppBookmarkIcon()
    val coinIcon = rememberAppCoinIcon()
    val likeIcon = rememberAppLikeIcon()
    val likeFilledIcon = rememberAppLikeFilledIcon()
    val shareIcon = rememberAppShareIcon()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 外层壳已是液态玻璃：内层提示框用实心半透明，禁止嵌套 liquid dock
        // （嵌套 refraction 边沿会出现「虾线」，且 content padding 易丢失）。
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(commentFieldShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                .clickable { onCommentClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            AppText(
                text = "评论 UP 主和大家...",
                color = inputTextColor,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        BottomInputBarActionButtons(
            favoriteIcon = favoriteIcon,
            coinIcon = coinIcon,
            likeIcon = likeIcon,
            likeFilledIcon = likeFilledIcon,
            shareIcon = shareIcon,
            isLiked = isLiked,
            isFavorited = isFavorited,
            isCoined = isCoined,
            onLikeClick = onLikeClick,
            onFavoriteClick = onFavoriteClick,
            onCoinClick = onCoinClick,
            onShareClick = onShareClick
        )
    }
}

@Composable
private fun BottomInputBarContentRow(
    modifier: Modifier,
    inputContainerColor: Color,
    inputTextColor: Color,
    isLiked: Boolean,
    isFavorited: Boolean,
    isCoined: Boolean,
    onLikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCoinClick: () -> Unit,
    onShareClick: () -> Unit,
    onCommentClick: () -> Unit,
) {
    val favoriteIcon = rememberAppBookmarkIcon()
    val coinIcon = rememberAppCoinIcon()
    val likeIcon = rememberAppLikeIcon()
    val likeFilledIcon = rememberAppLikeFilledIcon()
    val shareIcon = rememberAppShareIcon()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(AppShapes.container(ContainerLevel.Card))
                .background(inputContainerColor)
                .clickable { onCommentClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            AppText(
                text = "评论 UP 主和大家...",
                color = inputTextColor,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        BottomInputBarActionButtons(
            favoriteIcon = favoriteIcon,
            coinIcon = coinIcon,
            likeIcon = likeIcon,
            likeFilledIcon = likeFilledIcon,
            shareIcon = shareIcon,
            isLiked = isLiked,
            isFavorited = isFavorited,
            isCoined = isCoined,
            onLikeClick = onLikeClick,
            onFavoriteClick = onFavoriteClick,
            onCoinClick = onCoinClick,
            onShareClick = onShareClick
        )
    }
}

@Composable
private fun BottomInputBarActionButtons(
    favoriteIcon: ImageVector,
    coinIcon: ImageVector,
    likeIcon: ImageVector,
    likeFilledIcon: ImageVector,
    shareIcon: ImageVector,
    isLiked: Boolean,
    isFavorited: Boolean,
    isCoined: Boolean,
    onLikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCoinClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconActionButton(
            icon = if (isLiked) likeFilledIcon else likeIcon,
            label = "点赞",
            tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            onClick = onLikeClick,
            showLabel = false
        )
        IconActionButton(
            icon = coinIcon,
            label = "投币",
            tint = if (isCoined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            onClick = onCoinClick,
            showLabel = false
        )
        IconActionButton(
            icon = favoriteIcon,
            label = "收藏",
            tint = if (isFavorited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            onClick = onFavoriteClick,
            showLabel = false
        )
        IconActionButton(
            icon = shareIcon,
            label = "分享",
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onShareClick,
            showLabel = false
        )
    }
}

@Composable
private fun IconActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    showLabel: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp)
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        if (showLabel) {
            Spacer(modifier = Modifier.height(2.dp))
            AppText(
                text = label,
                fontSize = 10.sp,
                color = tint
            )
        }
    }
}
