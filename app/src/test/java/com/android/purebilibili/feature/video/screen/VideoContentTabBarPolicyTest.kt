package com.android.purebilibili.feature.video.screen

import com.android.purebilibili.core.ui.AppTopTabPresentation
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VideoContentTabBarPolicyTest {

    @Test
    fun `comment list at top only when first item and zero offset`() {
        assertTrue(isVideoContentCommentListAtTop(0, 0))
        assertFalse(isVideoContentCommentListAtTop(0, 1))
        assertFalse(isVideoContentCommentListAtTop(1, 0))
    }

    @Test
    fun `collapse progress follows nested collapse px and snaps full when list leaves top`() {
        assertEquals(
            0f,
            resolveVideoContentTabBarCollapseProgress(
                collapsePx = 20f,
                maxCollapsePx = 80f,
                selectedTabIndex = 0,
                listAtTop = true,
            ),
        )
        assertEquals(
            0.25f,
            resolveVideoContentTabBarCollapseProgress(
                collapsePx = 20f,
                maxCollapsePx = 80f,
                selectedTabIndex = 1,
                listAtTop = true,
            ),
        )
        assertEquals(
            1f,
            resolveVideoContentTabBarCollapseProgress(
                collapsePx = 10f,
                maxCollapsePx = 80f,
                selectedTabIndex = 1,
                listAtTop = false,
            ),
        )
    }

    @Test
    fun `preScroll collapses with downward content scroll and can reverse expand at top`() {
        val collapse = reduceVideoContentTabBarCollapseOnPreScroll(
            collapsePx = 10f,
            maxCollapsePx = 80f,
            availableY = -30f,
            listAtTop = true,
            enabled = true,
        )
        assertEquals(40f, collapse!!.nextCollapsePx)
        assertEquals(-30f, collapse.consumedY)

        val expand = reduceVideoContentTabBarCollapseOnPreScroll(
            collapsePx = 40f,
            maxCollapsePx = 80f,
            availableY = 25f,
            listAtTop = true,
            enabled = true,
        )
        assertEquals(15f, expand!!.nextCollapsePx)
        assertEquals(25f, expand.consumedY)
    }

    @Test
    fun `preScroll expand is ignored when list not at top so browsing can keep chrome hidden`() {
        assertNull(
            reduceVideoContentTabBarCollapseOnPreScroll(
                collapsePx = 80f,
                maxCollapsePx = 80f,
                availableY = 40f,
                listAtTop = false,
                enabled = true,
            ),
        )
    }

    @Test
    fun `preScroll is interruptible mid collapse`() {
        val mid = reduceVideoContentTabBarCollapseOnPreScroll(
            collapsePx = 0f,
            maxCollapsePx = 100f,
            availableY = -40f,
            listAtTop = true,
            enabled = true,
        )!!
        assertEquals(40f, mid.nextCollapsePx)

        val reverse = reduceVideoContentTabBarCollapseOnPreScroll(
            collapsePx = mid.nextCollapsePx,
            maxCollapsePx = 100f,
            availableY = 15f,
            listAtTop = true,
            enabled = true,
        )!!
        assertEquals(25f, reverse.nextCollapsePx)
        assertEquals(15f, reverse.consumedY)
    }

    @Test
    fun `postScroll expands remainder after list hits top during fling`() {
        val update = reduceVideoContentTabBarCollapseOnPostScroll(
            collapsePx = 50f,
            maxCollapsePx = 80f,
            availableY = 30f,
            listAtTop = true,
            enabled = true,
        )
        assertEquals(20f, update!!.nextCollapsePx)
        assertEquals(30f, update.consumedY)
    }

    @Test
    fun `leaving list top forces full collapse while intro tab resets`() {
        assertEquals(
            80f,
            resolveVideoContentTabBarCollapsePxWhenListLeavesTop(
                collapsePx = 12f,
                maxCollapsePx = 80f,
                listAtTop = false,
                enabled = true,
            ),
        )
        assertEquals(
            0f,
            resolveVideoContentTabBarCollapsePxWhenListLeavesTop(
                collapsePx = 40f,
                maxCollapsePx = 80f,
                listAtTop = true,
                enabled = false,
            ),
        )
        assertEquals(
            40f,
            resolveVideoContentTabBarCollapsePxWhenListLeavesTop(
                collapsePx = 40f,
                maxCollapsePx = 80f,
                listAtTop = true,
                enabled = true,
            ),
        )
    }

    @Test
    fun `tab bar layout reserves trailing danmaku action area`() {
        val spec = resolveVideoContentTabBarLayoutSpec(widthDp = 412)

        assertEquals(1f, spec.tabsRowWeight)
        assertTrue(spec.tabsRowScrollable)
        assertEquals(12, spec.containerHorizontalPaddingDp)
        assertEquals(12, spec.tabHorizontalPaddingDp)
        assertEquals(40, spec.segmentedControlHeightDp)
        assertEquals(27, spec.segmentedControlIndicatorHeightDp)
        assertTrue(
            hasVideoContentTabBarIndicatorScaleClearance(
                containerHeightDp = spec.segmentedControlHeightDp,
                indicatorHeightDp = spec.segmentedControlIndicatorHeightDp
            )
        )
    }

    @Test
    fun `danmaku input stays visible when player is expanded`() {
        assertTrue(
            shouldShowDanmakuSendInput(
                isPlayerCollapsed = false
            )
        )
    }

    @Test
    fun `danmaku input hidden when player is collapsed`() {
        assertFalse(
            shouldShowDanmakuSendInput(
                isPlayerCollapsed = true
            )
        )
    }

    @Test
    fun `danmaku action layout keeps settings target comfortably tappable`() {
        val policy = resolveVideoContentTabBarDanmakuActionLayoutPolicy(widthDp = 412)

        assertEquals("发弹幕", policy.sendLabel)
        assertEquals(40, policy.secondaryControlHeightDp)
        assertEquals(20, policy.secondaryControlCornerRadiusDp)
        assertEquals(40, policy.settingsButtonSizeDp)
        assertEquals(18, policy.settingsIconSizeDp)
    }

    @Test
    fun `compact phone layout tightens tabs and danmaku actions`() {
        val spec = resolveVideoContentTabBarLayoutSpec(widthDp = 393)
        val policy = resolveVideoContentTabBarDanmakuActionLayoutPolicy(widthDp = 393)

        assertEquals(8, spec.containerHorizontalPaddingDp)
        assertEquals(8, spec.tabHorizontalPaddingDp)
        assertEquals(10, spec.tabSpacingDp)
        assertEquals(16, spec.selectedTabFontSizeSp)
        assertEquals(40, spec.segmentedControlHeightDp)
        assertTrue(
            hasVideoContentTabBarIndicatorScaleClearance(
                containerHeightDp = spec.segmentedControlHeightDp,
                indicatorHeightDp = spec.segmentedControlIndicatorHeightDp
            )
        )
        assertEquals("发弹幕", policy.sendLabel)
        assertEquals(40, policy.secondaryControlHeightDp)
        assertEquals(20, policy.secondaryControlCornerRadiusDp)
        assertEquals(40, policy.settingsButtonSizeDp)
        assertEquals(18, policy.settingsIconSizeDp)
    }

    @Test
    fun `danmaku action controls share the tab bar visual grid`() {
        val compact = resolveVideoContentTabBarDanmakuActionLayoutPolicy(widthDp = 393)
        val regular = resolveVideoContentTabBarDanmakuActionLayoutPolicy(widthDp = 412)

        listOf(compact, regular).forEach { policy ->
            assertEquals(40, policy.secondaryControlHeightDp)
            assertEquals(policy.secondaryControlHeightDp, policy.settingsButtonSizeDp)
            assertEquals(policy.toggleVerticalPaddingDp, policy.sendVerticalPaddingDp)
            assertEquals(policy.toggleTextSizeSp, policy.sendTextSizeSp)
        }
    }

    @Test
    fun `info comment tab bar disables tap press refraction`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/screen/VideoContentSection.kt"
        )
        val tabBarBlock = source
            .substringAfter("fun VideoContentTabBar(")
            .substringBefore("// [新增] 恢复画面按钮")

        assertTrue(tabBarBlock.contains("tapPressRefractionEnabled = false"))
        assertTrue(
            tabBarBlock.contains(
                "externalPagerMotionEffectsEnabled = liquidChromeSpec.reusesLiquidGlassDock"
            )
        )
        assertTrue(source.contains("clip = tabBarCollapseProgress > 0.001f"))
    }

    @Test
    fun `video content section wires chrome backdrop into tab and comment segmented controls`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/screen/VideoContentSection.kt"
        )

        assertTrue(source.contains("val videoContentChromeBackdrop = rememberLayerBackdrop()"))
        assertTrue(source.contains("val videoContentMiuixBackdrop = rememberMiuixLayerBackdrop()"))
        assertTrue(source.contains(".miuixLayerBackdrop(videoContentMiuixBackdrop)"))
        assertTrue(source.contains(".alpha(0f)"))
        assertTrue(source.contains("sortMode = sortMode"))
        assertTrue(source.contains("onSortModeChange = onSortModeChange"))
        assertTrue(source.contains("backdrop = videoContentChromeBackdrop"))
        assertTrue(source.contains("miuixBackdrop = videoContentMiuixBackdrop"))
        assertTrue(source.contains("Modifier.layerBackdrop(chromeBackdrop)"))
        assertTrue(source.contains("Column(modifier = modifier.fillMaxSize())"))
        assertTrue(
            source.contains(
                "采样层只挂在 Tab 页滚动内容上；排序栏/顶栏分段控件必须在捕获区外"
            )
        )
        val commentTabSource = source.substringAfter("internal fun VideoCommentTab(")
            .substringBefore("internal fun LandscapeCommentPanel(")
        assertTrue(commentTabSource.contains("CommentListHeader("))
        assertFalse(commentTabSource.contains("CommentSortFilterBar("))
        assertTrue(source.contains("if (selectedTabIndex == 1)"))
        val pagerBlock = source
            .substringAfter("HorizontalPager(")
            .substringBefore(") { page ->")
        assertFalse(
            pagerBlock.contains("layerBackdrop"),
            "Pager must not capture backdrop; segmented controls inside would self-sample and overflow RenderThread stack on MIUI"
        )
        assertTrue(source.contains("liquidGlassEffectsEnabled = liquidChromeSpec.liquidGlassEffectsEnabled"))
    }

    @Test
    fun `moving capsule uses calmer intro comment tab switch motion`() {
        val iosSpec = resolveVideoContentTabSwitchAnimationSpec(AppTopTabPresentation.MOVING_CAPSULE)
        val md3Spec = resolveVideoContentTabSwitchAnimationSpec(AppTopTabPresentation.MATERIAL_UNDERLINE)

        assertEquals(360, iosSpec.durationMs)
        assertEquals(240, md3Spec.durationMs)
        assertTrue(iosSpec.durationMs > md3Spec.durationMs)
        assertEquals(iosSpec.durationMs, resolveInlinePortraitPlayerCommentCollapseDurationMillis(iosSpec))
        assertEquals(md3Spec.durationMs, resolveInlinePortraitPlayerCommentCollapseDurationMillis(md3Spec))
    }

    @Test
    fun `effective selected tab follows target while pager is switching`() {
        assertEquals(
            1,
            resolveVideoContentEffectiveSelectedTabIndex(
                currentPage = 0,
                targetPage = 1,
                isScrollInProgress = true,
                pageCount = 2
            )
        )
    }

    @Test
    fun `effective selected tab uses current page when pager is idle`() {
        assertEquals(
            0,
            resolveVideoContentEffectiveSelectedTabIndex(
                currentPage = 0,
                targetPage = 1,
                isScrollInProgress = false,
                pageCount = 2
            )
        )
    }

    @Test
    fun `effective selected tab falls back to current page for invalid target`() {
        assertEquals(
            0,
            resolveVideoContentEffectiveSelectedTabIndex(
                currentPage = 0,
                targetPage = 3,
                isScrollInProgress = true,
                pageCount = 2
            )
        )
    }

    @Test
    fun `horizontal page drags stay enabled on both intro and comment tabs`() {
        assertTrue(
            shouldEnableVideoContentHorizontalPagerSwipe(
                currentPage = 1,
                commentPageIndex = 1,
                isPagerScrollInProgress = false,
            )
        )
        assertTrue(
            shouldEnableVideoContentHorizontalPagerSwipe(
                currentPage = 1,
                commentPageIndex = 1,
                isPagerScrollInProgress = true,
            )
        )
        assertTrue(
            shouldEnableVideoContentHorizontalPagerSwipe(
                currentPage = 0,
                commentPageIndex = 1,
                isPagerScrollInProgress = false,
            )
        )
    }

    @Test
    fun `intro action row exposes a labeled comment entry and opens comment tab`() {
        val contentSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/screen/VideoContentSection.kt"
        )
        val actionSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/section/VideoActionSection.kt"
        )

        assertTrue(contentSource.contains("onCommentClick = { onTabSelected(1) }"))
        assertTrue(actionSource.contains("text = \"评论 \${FormatUtils.formatStat(info.stat.reply.toLong())}\""))
        assertTrue(actionSource.contains("onClick = onCommentClick"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
