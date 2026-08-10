package com.android.purebilibili.feature.bangumi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.core.ui.AppScaffold
import com.android.purebilibili.core.ui.AppTopBar
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppNativeTabRow
import com.android.purebilibili.core.ui.components.AppSearchField
import com.android.purebilibili.core.ui.components.AppSearchFieldPresentation
import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.android.purebilibili.core.ui.rememberAppBackIcon
import com.android.purebilibili.core.ui.rememberAppSearchIcon
import com.android.purebilibili.core.util.responsiveContentWidth
import com.android.purebilibili.feature.download.DownloadManager
import kotlinx.coroutines.launch

/** Navigation-compatible state holder for the Bangumi/Cinema hub. */
@Composable
fun BangumiScreen(
    onBack: () -> Unit,
    onBangumiClick: (Long) -> Unit,
    onBangumiEpisodeClick: (Long, Long) -> Unit = { seasonId, _ -> onBangumiClick(seasonId) },
    initialType: Int = 1,
    viewModel: BangumiHubViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var searchQuery by remember { mutableStateOf("") }
    val selectionActive = state.page == BangumiHubPage.FOLLOW &&
        state.followStates[state.channel to state.followStatus]?.selectedIds?.isNotEmpty() == true

    LaunchedEffect(initialType) { viewModel.initialize(initialType) }
    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(state.page) {
        if (state.page == BangumiHubPage.SEARCH) focusRequester.requestFocus()
    }

    val handleBack = {
        if (!viewModel.consumeBack()) onBack()
    }
    BackHandler(onBack = handleBack)

    AppScaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (state.page == BangumiHubPage.SEARCH) {
                BangumiSearchTopBar(
                    query = searchQuery,
                    focusRequester = focusRequester,
                    channel = state.channel,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        viewModel.search(searchQuery)
                        keyboard?.hide()
                    },
                    onBack = handleBack,
                )
            } else {
                AppTopBar(
                    title = when (state.page) {
                        BangumiHubPage.HOME -> "番剧影视"
                        BangumiHubPage.INDEX -> "索引"
                        BangumiHubPage.FOLLOW -> if (state.channel == BangumiChannel.BANGUMI) "我的追番" else "我的追剧"
                        BangumiHubPage.SEARCH -> "搜索"
                    },
                    navigationIcon = {
                        AppIconButton(
                            onClick = handleBack,
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            AppIcon(rememberAppBackIcon(), contentDescription = "返回")
                        }
                    },
                    actions = {
                        AppIconButton(
                            onClick = viewModel::openSearch,
                            enabled = !selectionActive,
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            AppIcon(rememberAppSearchIcon(), contentDescription = "搜索")
                        }
                    },
                )
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .responsiveContentWidth(),
        ) {
            if (state.page != BangumiHubPage.SEARCH) {
                AppNativeTabRow(
                    options = BangumiChannel.entries.map { AppSegmentOption(it, it.label) },
                    selectedValue = state.channel,
                    enabled = !selectionActive,
                    onSelectionChange = viewModel::selectChannel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            BangumiHubContent(
                state = state,
                onBangumiClick = onBangumiClick,
                onEpisodeClick = onBangumiEpisodeClick,
                onRefreshHome = { viewModel.refreshHome() },
                onLoadMoreHomeRecommendations = viewModel::loadMoreHomeRecommendations,
                onLoadMoreHomeFollows = viewModel::loadMoreHomeFollows,
                onRetryTimeline = viewModel::retryTimeline,
                onOpenIndex = viewModel::openIndex,
                onOpenFollow = viewModel::openFollowManager,
                onIndexCategorySelected = viewModel::selectIndexCategory,
                onIndexFilterSelected = viewModel::selectIndexFilter,
                onToggleFiltersExpanded = viewModel::toggleIndexFiltersExpanded,
                onRetryIndexConditions = viewModel::retryIndexConditions,
                onRetryIndexResults = viewModel::retryIndexResults,
                onLoadMoreIndexResults = viewModel::loadMoreIndexResults,
                onFollowStatusSelected = viewModel::selectFollowStatus,
                onRefreshFollow = viewModel::refreshFollowManager,
                onLoadMoreFollow = viewModel::loadMoreFollowManager,
                onToggleFollowSelection = viewModel::toggleFollowSelection,
                onSelectAllFollow = viewModel::selectAllFollowItems,
                onClearFollowSelection = viewModel::clearFollowSelection,
                onMoveSelectedFollow = viewModel::moveSelectedFollowItems,
                onMoveSingleFollow = viewModel::updateSingleFollowItem,
                onUnfollowSingle = viewModel::unfollowSingleItem,
                onLoadMoreSearch = viewModel::loadMoreSearch,
                onSaveCover = { url, title ->
                    scope.launch {
                        val saved = DownloadManager.saveImageToGallery(context, url, title)
                        snackbarHostState.showSnackbar(if (saved) "封面已保存" else "保存封面失败")
                    }
                },
            )
        }
    }
}

/** Source-compatible bridge for callers that injected the former combined ViewModel. */
@Deprecated("BangumiScreen now owns a dedicated lightweight hub state holder")
@Composable
fun BangumiScreen(
    onBack: () -> Unit,
    onBangumiClick: (Long) -> Unit,
    onBangumiEpisodeClick: (Long, Long) -> Unit = { seasonId, _ -> onBangumiClick(seasonId) },
    initialType: Int = 1,
    @Suppress("UNUSED_PARAMETER") viewModel: BangumiViewModel,
) {
    BangumiScreen(
        onBack = onBack,
        onBangumiClick = onBangumiClick,
        onBangumiEpisodeClick = onBangumiEpisodeClick,
        initialType = initialType,
    )
}

@Composable
private fun BangumiSearchTopBar(
    query: String,
    focusRequester: FocusRequester,
    channel: BangumiChannel,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AppIconButton(
            onClick = onBack,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        ) {
            AppIcon(rememberAppBackIcon(), contentDescription = "返回")
        }
        AppSearchField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            placeholder = if (channel == BangumiChannel.BANGUMI) "搜索番剧" else "搜索影视",
            presentation = AppSearchFieldPresentation.TOP_BAR,
            autoFocusEnabled = true,
            focusRequester = focusRequester,
            modifier = Modifier.weight(1f),
        )
        AppIconButton(
            onClick = onSearch,
            enabled = query.isNotBlank(),
            modifier = Modifier
                .width(48.dp)
                .sizeIn(minHeight = 48.dp),
        ) {
            AppIcon(rememberAppSearchIcon(), contentDescription = "搜索")
        }
    }
}
