package com.android.purebilibili.core.ui.motion

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnimationApiBoundaryTest {

    @Test
    fun navigationAndVideoCardHotPathCannotRestoreDecorativeMotion() {
        val navigationTransform = source("navigation3/BiliPaiNavContentTransformPolicy.kt")
        val videoCardBounds = source("core/ui/transition/VideoCardShellSharedBounds.kt")
        val homeSkeleton = source("feature/home/HomeFeedSkeletonCard.kt")
        val videoSkeleton = source("feature/video/ui/components/SkeletonComponents.kt")

        assertTrue(navigationTransform.contains("EnterTransition.None togetherWith ExitTransition.None"))
        assertFalse(navigationTransform.contains("slideIn"))
        assertFalse(navigationTransform.contains("fadeIn"))
        assertFalse(videoCardBounds.substringAfter("videoCardShellSharedBoundsOrEmpty")
            .contains("Modifier.sharedBounds("))
        assertFalse(homeSkeleton.contains("rememberInfiniteTransition"))
        assertFalse(videoSkeleton.contains("val transition = rememberInfiniteTransition"))
    }

    @Test
    fun appSourcesCannotRestoreSharedElementsOrInfiniteDecoration() {
        val violations = mainSources().flatMap { file ->
            val text = file.readText()
            buildList {
                if (SHARED_BOUNDS.containsMatchIn(text)) add("${file.name}: sharedBounds")
                if (SHARED_ELEMENT.containsMatchIn(text)) add("${file.name}: sharedElement")
                if (INFINITE_TRANSITION.containsMatchIn(text)) add("${file.name}: rememberInfiniteTransition")
                if (INFINITE_REPEATABLE.containsMatchIn(text)) add("${file.name}: infiniteRepeatable")
            }
        }

        val violationList = violations.toList()
        assertTrue(
            violationList.isEmpty(),
            "共享元素和无限装饰动画必须保持为零；发现：${violationList.sorted()}",
        )
    }

    @Test
    fun playerFeedbackAndSurfaceRevealStayStatic() {
        val celebration = source("feature/video/ui/components/CelebrationAnimations.kt")
        val playerSection = source("feature/video/ui/section/VideoPlayerSection.kt")
        val surfaceRevealBlock = playerSection
            .substringAfter("val surfaceRevealSpec =")
            .substringBefore("key(isFlippedHorizontal, isFlippedVertical, isPortraitFullscreen)")

        assertFalse(celebration.contains("Animatable("))
        assertFalse(celebration.contains("animateFloatAsState("))
        assertFalse(celebration.contains("Canvas("))
        assertFalse(celebration.contains("graphicsLayer"))
        assertFalse(surfaceRevealBlock.contains("animateFloatAsState("))
        assertTrue(surfaceRevealBlock.contains("val playerSurfaceScale = 1f"))
        assertFalse(playerSection.contains("resolveVideoPlayerRevealMotionSpec()"))
        assertFalse(playerSection.contains("coverRevealHoldDelayMillis"))
    }

    @Test
    fun videoActionRowDoesNotAnimateLayoutOrPrimaryButtonScale() {
        val actionSection = source("feature/video/ui/section/VideoActionSection.kt")
        val primaryRow = actionSection
            .substringAfter("Row(\n        modifier = modifier")
            .substringBefore("// Like")
        val primaryButton = actionSection
            .substringAfter("private fun TripleProgressActionButton(")
            .substringBefore("private fun TripleLikeActionButton(")

        assertFalse(primaryRow.contains("animateContentSize("))
        assertFalse(primaryButton.contains("animateFloatAsState("))
        assertFalse(primaryButton.contains("graphicsLayer"))
    }

    @Test
    fun playerControlOptionsUseOnlyShortAlphaVisibilityFeedback() {
        val controlBar = source("feature/video/ui/overlay/BottomControlBar.kt")
        val anime4kAction = controlBar
            .substringAfter("private fun Anime4KMoreAction(")
            .substringBefore("private fun Anime4KIntensityOption(")

        assertTrue(anime4kAction.contains("enter = fadeIn(tween(100))"))
        assertTrue(anime4kAction.contains("exit = fadeOut(tween(100))"))
        assertFalse(anime4kAction.contains("expandIn("))
        assertFalse(anime4kAction.contains("expandVertically("))
    }

    @Test
    fun portraitPlayerOverlayUsesExplicitShortAlphaFeedback() {
        val portraitOverlay = source("feature/video/ui/overlay/PortraitFullscreenOverlay.kt")

        assertFalse(portraitOverlay.contains("fadeIn()"))
        assertFalse(portraitOverlay.contains("fadeOut()"))
        assertTrue(portraitOverlay.contains("fadeIn(tween(100))"))
        assertTrue(portraitOverlay.contains("fadeOut(tween(100))"))
    }

    @Test
    fun imagePreviewOpenCloseAndDragSettlementUseDirectState() {
        val imagePreview = source("feature/dynamic/components/ImagePreviewDialog.kt")

        assertFalse(imagePreview.contains("animateTo("))
        assertTrue(imagePreview.contains("animateTrigger.snapTo(1f)"))
        assertTrue(imagePreview.contains("animateTrigger.snapTo(0f)"))
        assertTrue(imagePreview.contains("verticalDismissOffsetYPx = 0f"))
    }

    @Test
    fun imagePreviewTextChangesUseNoOpContentTransforms() {
        val imagePreview = source("feature/dynamic/components/ImagePreviewDialog.kt")

        assertFalse(imagePreview.contains("slideInVertically("))
        assertFalse(imagePreview.contains("slideOutVertically("))
        assertFalse(imagePreview.contains("slideInHorizontally("))
        assertFalse(imagePreview.contains("slideOutHorizontally("))
        assertTrue(
            imagePreview.contains("EnterTransition.None togetherWith ExitTransition.None")
        )
    }

    @Test
    fun legacyLottieComponentsCannotRestoreDecorativePlayback() {
        val lottieComponents = source("core/ui/LottieComponents.kt")

        assertFalse(lottieComponents.contains("animateLottieCompositionAsState"))
        assertFalse(lottieComponents.contains("rememberLottieComposition"))
        assertFalse(lottieComponents.contains("com.airbnb.lottie"))
        assertTrue(lottieComponents.contains("AdaptiveLoadingIndicator("))
    }

    @Test
    fun simpleTopTabIndicatorTracksPositionWithoutDecorativeDeformation() {
        val indicator = source("feature/home/components/LiquidIndicator.kt")
        val simpleIndicator = indicator
            .substringAfter("fun SimpleLiquidIndicator(")
            .substringBefore("internal fun resolveTopTabIndicatorWidthPx")

        assertFalse(simpleIndicator.contains("animateFloatAsState("))
        assertTrue(simpleIndicator.contains("isDragging = false"))
        assertTrue(simpleIndicator.contains("val scale = 1f"))
    }

    @Test
    fun bottomBarDragScaleStopsImmediatelyWithTheGesture() {
        val bottomBar = source("feature/home/components/BottomBar.kt")
        val dragScale = bottomBar
            .substringAfter("internal fun rememberBottomBarIndicatorDragScaleProgress(")
            .substringBefore("internal fun resolveBottomBarVisualIndicatorPosition")

        assertTrue(dragScale.contains("if (isDragging) 1f else 0f"))
        assertFalse(dragScale.contains("Animatable("))
        assertFalse(dragScale.contains("animateTo("))
    }

    @Test
    fun pullRefreshIndicatorsChangeStateWithoutTweenedScaleOrAlpha() {
        val refreshIndicator = source("feature/home/components/HomeRefreshIndicator.kt")

        assertFalse(refreshIndicator.contains("animateFloatAsState("))
        assertFalse(refreshIndicator.contains("iosRefreshArrowMotionSpec("))
        assertFalse(refreshIndicator.contains("iosRefreshAlphaMotionSpec("))
        assertFalse(refreshIndicator.contains("iosRefreshScaleMotionSpec("))
        assertFalse(refreshIndicator.contains("md3RefreshAlphaMotionSpec("))
        assertFalse(refreshIndicator.contains("md3RefreshScaleMotionSpec("))
        assertTrue(refreshIndicator.contains("AppPullRefreshLoadingIndicator("))
        assertTrue(refreshIndicator.contains("AdaptiveLoadingIndicator("))
    }

    @Test
    fun homeTodayWatchContentDoesNotRestoreWaterfallEntranceMotion() {
        val categoryPage = source("feature/home/HomeCategoryPage.kt")
        val waterfallReveal = categoryPage
            .substringAfter("private fun WaterfallReveal(")
            .substringBefore("private fun OldContentDivider()")

        assertTrue(waterfallReveal.contains("content()"))
        assertFalse(waterfallReveal.contains("AnimatedVisibility("))
        assertFalse(waterfallReveal.contains("nonLinearWaterfallDelayMillis("))
        assertFalse(waterfallReveal.contains("homeWaterfallFadeInSpec("))
        assertFalse(waterfallReveal.contains("homeWaterfallExpandSpec("))
    }

    @Test
    fun gesturePercentTextTracksInputWithoutDecorativeMotion() {
        val percentText = source("feature/video/ui/components/AnimatedGesturePercentText.kt")

        assertFalse(percentText.contains("AnimatedContent("))
        assertFalse(percentText.contains("Animatable("))
        assertFalse(percentText.contains("slideInVertically("))
        assertFalse(percentText.contains("slideOutVertically("))
        assertFalse(percentText.contains(".blur("))
        assertTrue(percentText.contains("shouldTriggerGesturePercentHaptic("))
    }

    @Test
    fun videoCommentSheetUsesStaticHostAndContentTransitions() {
        val commentSheet = source("feature/video/ui/components/VideoCommentSheetHost.kt")

        assertFalse(commentSheet.contains("animateFloatAsState("))
        assertFalse(commentSheet.contains("rememberAppBottomSheetMotion("))
        assertFalse(commentSheet.contains("slideInHorizontally("))
        assertFalse(commentSheet.contains("slideOutHorizontally("))
        assertFalse(commentSheet.contains("fadeIn(animationSpec = tween"))
        assertTrue(commentSheet.contains("EnterTransition.None togetherWith ExitTransition.None"))
    }

    @Test
    fun onboardingBottomSheetUsesStaticVisibilityAndDirectPageChanges() {
        val onboardingSheet = source("feature/onboarding/OnboardingBottomSheet.kt")

        assertFalse(onboardingSheet.contains("AnimatedVisibility("))
        assertFalse(onboardingSheet.contains("rememberAppBottomSheetMotion("))
        assertFalse(onboardingSheet.contains("Animatable("))
        assertFalse(onboardingSheet.contains("animateScrollToPage("))
        assertTrue(onboardingSheet.contains("pagerState.scrollToPage("))
    }

    @Test
    fun ordinaryListPagesDoNotRestoreDecorativeTransitions() {
        val following = source("feature/following/FollowingListScreen.kt")
        val bangumi = source("feature/bangumi/BangumiScreen.kt")
        val commonList = source("feature/list/CommonListScreen.kt")
        val actionButton = source("feature/dynamic/components/ActionButton.kt")
        val dynamicSidebar = source("feature/dynamic/components/DynamicSidebar.kt")
        val dynamicScreen = source("feature/dynamic/DynamicScreen.kt")
        val spaceScreen = source("feature/space/SpaceScreen.kt")

        assertFalse(following.contains("AnimatedContent("))
        assertFalse(following.contains("Animatable("))
        assertFalse(following.contains(".blur("))
        assertFalse(bangumi.contains("AnimatedContent("))
        assertFalse(bangumi.contains("AnimatedVisibility("))
        assertFalse(bangumi.contains("slideInHorizontally("))
        assertFalse(bangumi.contains("scaleIn("))
        assertFalse(commonList.contains("AnimatedVisibility("))
        assertTrue(commonList.contains("if (shouldShowBackToTop)"))
        assertFalse(actionButton.contains("animateFloatAsState("))
        assertFalse(actionButton.contains(".scale("))
        assertFalse(dynamicSidebar.contains("animateFloatAsState("))
        assertFalse(dynamicSidebar.contains("CascadeSidebarItem("))
        assertFalse(dynamicScreen.contains("AnimatedContent("))
        assertFalse(dynamicScreen.contains("AnimatedVisibility("))
        assertFalse(dynamicScreen.contains("slideInHorizontally("))
        assertFalse(spaceScreen.contains("AnimatedVisibility("))
        assertFalse(spaceScreen.contains("animateColorAsState("))
    }

    private fun source(relativePath: String): String {
        return File("src/main/java/com/android/purebilibili/$relativePath").readText()
    }

    private fun mainSources(): Sequence<File> =
        File("src/main/java/com/android/purebilibili")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }

    private companion object {
        val SHARED_BOUNDS = Regex("""\.sharedBounds\s*\(""")
        val SHARED_ELEMENT = Regex("""\.sharedElement\s*\(""")
        val INFINITE_TRANSITION = Regex("""rememberInfiniteTransition\s*\(""")
        val INFINITE_REPEATABLE = Regex("""infiniteRepeatable\s*\(""")
    }
}
