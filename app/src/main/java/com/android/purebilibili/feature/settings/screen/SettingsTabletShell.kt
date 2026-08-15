package com.android.purebilibili.feature.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppNavigationDrawerItem
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.rememberAdaptivePreferenceIconContentColor
import com.android.purebilibili.core.ui.components.rememberAdaptivePreferenceIconContainerColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.purebilibili.R
import com.android.purebilibili.core.ui.AppSplitLayout
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.rememberAppBackIcon
import com.android.purebilibili.feature.settings.SettingsHomeSearchEntry
import com.android.purebilibili.feature.settings.SettingsRootCategory
import com.android.purebilibili.feature.settings.canonicalSettingsRootCategory
import com.android.purebilibili.feature.settings.rememberSettingsEntryVisual
import com.android.purebilibili.feature.settings.resolveSettingsRootCategoryOrder
import com.android.purebilibili.feature.settings.resolveSettingsTabletLayoutPolicy
import com.android.purebilibili.feature.settings.resolveSettingsVisualSpec
import com.android.purebilibili.feature.settings.ui.LocalSettingsDetailPane

@Composable
fun SettingsTabletShell(
    selectedCategory: SettingsRootCategory?,
    onCategoryClick: (SettingsRootCategory) -> Unit,
    onBack: () -> Unit,
    onSearchOpen: () -> Unit,
    modifier: Modifier = Modifier,
    rightPane: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val layoutPolicy = remember(configuration.screenWidthDp) {
        resolveSettingsTabletLayoutPolicy(widthDp = configuration.screenWidthDp)
    }
    val categories = remember { resolveSettingsRootCategoryOrder() }
    AppSplitLayout(
        modifier = modifier.fillMaxSize(),
        primaryRatio = layoutPolicy.primaryRatio,
        primaryContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(AppSurfaceTokens.groupedListContainer())
                    .padding(layoutPolicy.masterPanePaddingDp.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 双栏下 master 面板顶栏是该窗格唯一顶部栏，需避开状态栏
                        // （详情面板内子页面 Scaffold 顶栏已被 LocalSettingsDetailPane 抑制）。
                        .windowInsetsPadding(WindowInsets.statusBars),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIconButton(onClick = onBack) {
                        AppIcon(
                            imageVector = rememberAppBackIcon(),
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                    AppText(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SettingsHomeSearchEntry(onClick = onSearchOpen)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(categories) { category ->
                        val visual = rememberSettingsEntryVisual(category.searchTarget)
                        val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(visual.iconTint)
                        val iconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
                        val selected = selectedCategory?.let(::canonicalSettingsRootCategory) == category
                        AppNavigationDrawerItem(
                            label = {
                                Column {
                                    AppText(category.title, fontWeight = FontWeight.Medium)
                                    AppText(
                                        text = category.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                    )
                                }
                            },
                            selected = selected,
                            onClick = { onCategoryClick(category) },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(resolveSettingsVisualSpec().categoryIconBubbleSize)
                                        .clip(AppShapes.container(ContainerLevel.Field))
                                        .background(effectiveIconTint),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (visual.icon != null) {
                                        AppIcon(
                                            imageVector = visual.icon,
                                            contentDescription = null,
                                            tint = iconContentColor,
                                            modifier = Modifier.size(resolveSettingsVisualSpec().categoryIconSize),
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        secondaryContent = {
            CompositionLocalProvider(LocalSettingsDetailPane provides true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppSurfaceTokens.groupedListContainer())
                        .padding(layoutPolicy.detailPanePaddingDp.dp),
                ) {
                    rightPane()
                }
            }
        },
    )
}
