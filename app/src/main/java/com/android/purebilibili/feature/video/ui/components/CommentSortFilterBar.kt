package com.android.purebilibili.feature.video.ui.components
import com.android.purebilibili.core.ui.components.AppText

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import com.android.purebilibili.feature.video.viewmodel.CommentSortMode
import com.kyant.backdrop.Backdrop
import top.yukonga.miuix.kmp.blur.Backdrop as MiuixBackdrop
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

internal data class CommentSortSegmentedControlSpec(
    val itemWidthDp: Int,
    val heightDp: Int,
    val indicatorHeightDp: Int
)

internal fun resolveCommentSortSegmentedControlSpec(itemCount: Int): CommentSortSegmentedControlSpec {
    return CommentSortSegmentedControlSpec(
        itemWidthDp = if (itemCount >= 4) 56 else 66,
        heightDp = AppChromeSizeTokens.BottomBarMatchedSegmentedControlHeightDp,
        indicatorHeightDp = AppChromeSizeTokens.BottomBarMatchedSegmentedIndicatorHeightDp,
    )
}

internal fun hasCommentSortIndicatorScaleClearance(
    containerHeightDp: Int,
    indicatorHeightDp: Int
): Boolean {
    val bottomBarScale = 78f / 56f
    return containerHeightDp >= indicatorHeightDp * bottomBarScale + 2f
}

/**
 * 评论列表标题。
 */
@Composable
fun CommentListHeader(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommentListTitle(count = count)
    }
}

@Composable
private fun CommentListTitle(count: Int) {
    val appearance = rememberVideoCommentAppearance()
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppText(
            text = "评论",
            fontSize = 20.sp, // iOS Large Title style scale
            fontWeight = FontWeight.Bold,
            color = appearance.primaryTextColor,
        )
        Spacer(modifier = Modifier.width(6.dp))
        AppText(
            text = FormatUtils.formatStat(count.toLong()),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = appearance.secondaryTextColor,
        )
    }
}

/**
 * 同时展示评论标题和排序控件的通用列表栏，用于不使用详情页顶栏的评论界面。
 */
@Composable
fun CommentSortHeader(
    count: Int,
    sortMode: CommentSortMode,
    onSortModeChange: (CommentSortMode) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    miuixBackdrop: MiuixBackdrop? = null,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CommentListTitle(count = count)
        CommentSortFilterBar(
            sortMode = sortMode,
            onSortModeChange = onSortModeChange,
            backdrop = backdrop,
            miuixBackdrop = miuixBackdrop,
        )
    }
}

/**
 * 评论排序分段控件，放置在详情页顶栏的“评论”标签右侧。
 */
@Composable
fun CommentSortFilterBar(
    sortMode: CommentSortMode,
    onSortModeChange: (CommentSortMode) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    miuixBackdrop: MiuixBackdrop? = null,
) {
    val sortModes = remember { listOf(CommentSortMode.HOT, CommentSortMode.NEWEST) }
    CommentSegmentedControl(
        items = sortModes.map { it.label },
        selectedIndex = sortModes.indexOf(sortMode).coerceAtLeast(0),
        onScaleChange = { index ->
            sortModes.getOrNull(index)?.let(onSortModeChange)
        },
        modifier = modifier,
        backdrop = backdrop,
        miuixBackdrop = miuixBackdrop,
    )
}

/**
 * Bottom-bar matched segmented control.
 */
@Composable
fun CommentSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onScaleChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    miuixBackdrop: MiuixBackdrop? = null,
) {
    val context = LocalContext.current
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    val spec = remember(items.size) {
        resolveCommentSortSegmentedControlSpec(itemCount = items.size)
    }
    BottomBarLiquidSegmentedControl(
        items = items,
        selectedIndex = selectedIndex,
        onSelected = onScaleChange,
        itemWidth = spec.itemWidthDp.dp,
        height = spec.heightDp.dp,
        indicatorHeight = spec.indicatorHeightDp.dp,
        labelFontSize = 13.sp,
        modifier = modifier,
        dragSelectionEnabled = false
    )
}
