package com.android.purebilibili.navigation

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

internal class MainBottomPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    var navigationStartPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var contentVisible by mutableStateOf(true)
        private set

    private var navJob: Job? = null

    fun switchToPage(targetIndex: Int) {
        val lastPage = pagerState.pageCount - 1
        if (lastPage < 0) return
        val safeTargetIndex = targetIndex.coerceIn(0, lastPage)
        if (safeTargetIndex == selectedPage) return

        val previousJob = navJob
        navJob = null
        previousJob?.cancel()

        navigationStartPage = pagerState.currentPage
        selectedPage = safeTargetIndex
        isNavigating = true

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                previousJob?.join()
                // 底栏点击表示平级页面替换，而非用户拖动页面。先淡出、直接换页、再淡入，
                // 不与 HorizontalPager 自身的跟手滑动叠加。
                contentVisible = false
                delay(BOTTOM_PAGER_FADE_OUT_DURATION_MILLIS)
                pagerState.scrollToPage(safeTargetIndex)
                contentVisible = true
                delay(BOTTOM_PAGER_FADE_IN_DURATION_MILLIS)
            } catch (_: IllegalStateException) {
                // Pager 在测量竞争期间可能拒绝切页，保持当前页并避免快速点击闪退。
            } finally {
                if (navJob == myJob) {
                    contentVisible = true
                    isNavigating = false
                    selectedPage = pagerState.currentPage
                    navigationStartPage = pagerState.currentPage
                    navJob = null
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }

}

internal const val BOTTOM_PAGER_FADE_OUT_DURATION_MILLIS = 90L
internal const val BOTTOM_PAGER_FADE_IN_DURATION_MILLIS = 130L

@Composable
internal fun rememberMainBottomPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): MainBottomPagerState {
    return remember(pagerState, coroutineScope) {
        MainBottomPagerState(
            pagerState = pagerState,
            coroutineScope = coroutineScope
        )
    }
}
