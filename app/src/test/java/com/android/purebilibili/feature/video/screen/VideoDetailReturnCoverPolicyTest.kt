package com.android.purebilibili.feature.video.screen

import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.VideoCardTransitionVisualTimeline
import com.android.purebilibili.core.ui.transition.VideoSharedTransitionPlaybackIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class VideoDetailReturnCoverPolicyTest {

    @Test
    fun exitTransitionInProgressFallsBackToCardClockReturning() {
        assertTrue(
            shouldTreatVideoDetailExitTransitionInProgress(
                animatedVisibilityTargetIsPostExit = true,
                videoCardBackgroundPhase = VideoCardTransitionBackgroundPhase.HELD,
            )
        )
        assertTrue(
            shouldTreatVideoDetailExitTransitionInProgress(
                animatedVisibilityTargetIsPostExit = false,
                videoCardBackgroundPhase = VideoCardTransitionBackgroundPhase.RETURNING,
            )
        )
        assertFalse(
            shouldTreatVideoDetailExitTransitionInProgress(
                animatedVisibilityTargetIsPostExit = false,
                videoCardBackgroundPhase = VideoCardTransitionBackgroundPhase.HELD,
            )
        )
        assertFalse(
            shouldTreatVideoDetailExitTransitionInProgress(
                animatedVisibilityTargetIsPostExit = false,
                videoCardBackgroundPhase = null,
            )
        )
    }

    @Test
    fun `immediate video back target keeps secondary content visible`() {
        assertFalse(
            shouldUseVideoDetailRootTransitionProgress(
                detailShellSharedBoundsEnabled = true,
                hasAnimatedVisibilityScope = true,
                keepLoadedContentForBackPreview = true,
            )
        )
    }

    @Test
    fun `related detail return suppresses enter fade flash while keeping exit fade available`() {
        assertTrue(
            shouldSuppressVideoDetailEnterFadeAfterBackPreview(
                wasKeptAsBackPreview = true,
                keepLoadedContentForBackPreview = false,
            )
        )
        assertFalse(
            shouldSuppressVideoDetailEnterFadeAfterBackPreview(
                wasKeptAsBackPreview = false,
                keepLoadedContentForBackPreview = false,
            )
        )
        assertEquals(
            1f,
            resolveVideoDetailReturnContentAlpha(
                transitionProgress = 0.2f,
                isCommittedCardReturn = false,
                holdFullyOpaqueAfterBackPreview = true,
            ),
            0.0001f,
        )
        assertEquals(
            0f,
            resolveVideoDetailReturnContentAlpha(
                transitionProgress = 0.2f,
                isCommittedCardReturn = true,
                holdFullyOpaqueAfterBackPreview = true,
            ),
            0.0001f,
        )
    }

    @Test
    fun `normal card detail transition still animates secondary content`() {
        assertTrue(
            shouldUseVideoDetailRootTransitionProgress(
                detailShellSharedBoundsEnabled = true,
                hasAnimatedVisibilityScope = true,
                keepLoadedContentForBackPreview = false,
            )
        )
    }

    @Test
    fun `shared return morph keeps playback session after stack pop`() {
        // 轻滑即松：栈已非详情但 exit morph 仍在 → 必须保活，否则前半段黑壳。
        assertTrue(
            shouldKeepPlaybackSessionActiveForSharedReturnMorph(
                isVisible = false,
                sharedBoundsActive = true,
                isExitTransitionInProgress = true,
            ),
        )
        assertTrue(
            shouldKeepPlaybackSessionActiveForSharedReturnMorph(
                isVisible = true,
                sharedBoundsActive = false,
                isExitTransitionInProgress = false,
            ),
        )
        assertFalse(
            shouldKeepPlaybackSessionActiveForSharedReturnMorph(
                isVisible = false,
                sharedBoundsActive = true,
                isExitTransitionInProgress = false,
            ),
        )
        assertFalse(
            shouldKeepPlaybackSessionActiveForSharedReturnMorph(
                isVisible = false,
                sharedBoundsActive = false,
                isExitTransitionInProgress = true,
            ),
        )
    }

    @Test
    fun `predictive cancel keeps the cover until the detail exit transition has settled`() {
        assertTrue(
            shouldTreatVideoDetailCardExitAsReturning(
                isExitTransitionInProgress = true,
                sharedBoundsActive = true,
            )
        )
        assertFalse(
            shouldTreatVideoDetailCardExitAsReturning(
                isExitTransitionInProgress = false,
                sharedBoundsActive = true,
            )
        )
    }

    @Test
    fun `detail used as immediate back target keeps its loaded player and controls`() {
        assertFalse(
            shouldTreatVideoDetailCardExitAsReturning(
                isExitTransitionInProgress = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = true,
            )
        )
    }

    @Test
    fun `video target without player ownership uses its own cover behind the live outgoing video`() {
        assertTrue(
            shouldForceBackPreviewPlayerCover(
                keepLoadedContentForBackPreview = true,
                bindLivePlayerForBackPreview = false
            )
        )
        assertFalse(
            shouldForceBackPreviewPlayerCover(
                keepLoadedContentForBackPreview = true,
                bindLivePlayerForBackPreview = true
            )
        )
    }

    @Test
    fun `immediate back target mounts the live inline player`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
            .readText()
        val inlinePlayerCall = source
            .substringAfter("PortraitInlineVideoPlayerHost(", "")
            .substringBefore("allowLivePlayerSharedElement = true")

        assertTrue(inlinePlayerCall.contains("liveBackPreview = bindLivePlayerForBackPreview"))
    }

    @Test
    fun `detail route does not manually fade its background during return`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
            .readText()

        assertFalse(source.contains("resolveVideoDetailShellBackgroundAlphaTarget"))
        assertFalse(source.contains("shellBackgroundAlpha"))
    }

    @Test
    fun `force cover becomes active when explicit return flag is true`() {
        assertTrue(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = true
            )
        )
    }

    @Test
    fun `global returning state does not force the target detail cover`() {
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = false
            )
        )
    }

    @Test
    fun `detail shell shared bounds does not disable return cover visual`() {
        assertTrue(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = true
            )
        )
        val source = File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailTransitionPolicy.kt")
            .readText()
        val policyBlock = source
            .substringAfter("internal fun resolveForceCoverOnlyForReturn(")
            .substringBefore("internal fun shouldUseReturningVideoDetailVisualState")
        assertFalse(policyBlock.contains("detailShellSharedBoundsEnabled"))
    }

    @Test
    fun `force cover stays disabled when shared transition is disabled`() {
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = true,
                transitionEnabled = false
            )
        )
    }

    @Test
    fun `force cover stays disabled when only exit transition is in progress`() {
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = false
            )
        )
    }

    @Test
    fun `predictive card return keeps the live player instead of forcing cover`() {
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = false,
                isCardReturnExitInProgress = true
            )
        )
    }

    @Test
    fun `force cover stays disabled during predictive card return exit when transition disabled`() {
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = false,
                transitionEnabled = false,
                isCardReturnExitInProgress = true
            )
        )
    }

    @Test
    fun `force cover stays disabled when no return state is active`() {
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = false
            )
        )
    }

    @Test
    fun `idle detail does not use returning visual without exit or force cover`() {
        assertFalse(
            shouldUseReturningVideoDetailVisualState(
                forceCoverOnlyForReturn = false,
                isCardReturnExitInProgress = false,
                isSessionReturningToCard = false,
            )
        )
    }

    @Test
    fun `committed card return exit enables cover handoff without forceCoverOnly`() {
        assertTrue(
            shouldUseReturningVideoDetailVisualState(
                forceCoverOnlyForReturn = false,
                isCardReturnExitInProgress = true,
            )
        )
        assertTrue(
            shouldUseReturningVideoDetailVisualState(
                forceCoverOnlyForReturn = false,
                isSessionReturningToCard = true,
            )
        )
        assertFalse(
            resolveForceCoverOnlyForReturn(
                forceCoverOnlyOnReturn = false,
                isCardReturnExitInProgress = true,
            )
        )
    }

    @Test
    fun `predictive seek exit is leaving but not committed until markReturning`() {
        // targetState=PostExit 的 seek：离开态 true，但尚未松手提交
        assertTrue(
            shouldUseReturningVideoDetailVisualState(
                forceCoverOnlyForReturn = false,
                isCardReturnExitInProgress = true,
                isSessionReturningToCard = false,
            )
        )
        assertFalse(
            shouldTreatVideoDetailCardReturnAsCommitted(
                isActuallyLeaving = false,
                isSessionReturningToCard = false,
            )
        )
        // 松手提交 / 按钮返回后才 committed
        assertTrue(
            shouldTreatVideoDetailCardReturnAsCommitted(
                isActuallyLeaving = false,
                isSessionReturningToCard = true,
            )
        )
        assertTrue(
            shouldTreatVideoDetailCardReturnAsCommitted(
                isActuallyLeaving = true,
                isSessionReturningToCard = false,
            )
        )
    }

    @Test
    fun `uncommitted predictive seek keeps live player and zero cover even when exit is in progress`() {
        // 与 StateHolder 接线一致：isCommitted=false 时 live 路径封面永不盖住播放器
        assertEquals(
            0f,
            resolveVideoDetailReturnCoverAlpha(
                transitionProgress = 0.7f,
                isCommittedCardReturn = false,
                hasResidentCover = true,
                liveReturnMorph = true,
            ),
            0.0001f,
        )
        assertEquals(
            1f,
            resolveVideoDetailReturnPlayerAlpha(
                transitionProgress = 0.7f,
                isCommittedCardReturn = false,
                hasResidentCover = true,
                liveReturnMorph = true,
            ),
            0.0001f,
        )
        // 已提交但未到 handoff 窗口：仍保持实时画面
        assertEquals(
            0f,
            resolveVideoDetailReturnCoverAlpha(
                transitionProgress = 0.5f,
                isCommittedCardReturn = true,
                hasResidentCover = true,
                liveReturnMorph = true,
            ),
            0.0001f,
        )
        assertEquals(
            1f,
            resolveVideoDetailReturnPlayerAlpha(
                transitionProgress = 0.5f,
                isCommittedCardReturn = true,
                hasResidentCover = true,
                liveReturnMorph = true,
            ),
            0.0001f,
        )
    }

    @Test
    fun `forceCoverOnly stays off unless explicitly requested for resident path`() {
        assertFalse(
            com.android.purebilibili.core.ui.transition.shouldForceCoverOnlyForReturnOwnership(
                ownership = com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.RESIDENT_COVER,
                useReturningVisualState = true,
                forceCoverOnlyOnReturn = false,
                isCommittedCardReturn = false,
            )
        )
        // 提交返回也不得自动 forceCover，否则一点返回就掐 player
        assertFalse(
            com.android.purebilibili.core.ui.transition.shouldForceCoverOnlyForReturnOwnership(
                ownership = com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.RESIDENT_COVER,
                useReturningVisualState = true,
                forceCoverOnlyOnReturn = false,
                isCommittedCardReturn = true,
            )
        )
    }

    @Test
    fun `detail state holder splits committed return from predictive exit leaving`() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt"
        ).readText()
        assertTrue(source.contains("shouldTreatVideoDetailCardReturnAsCommitted("))
        assertTrue(source.contains("isCommittedCardReturn = isCommittedCardReturn"))
        assertFalse(source.contains("isCommittedCardReturn = isLeaving"))
        assertTrue(source.contains("isCommittedCardReturn = isCommittedCardReturn,"))
    }

    @Test
    fun `live return morph keeps player visible before the landing handoff`() {
        assertEquals(
            0f,
            resolveVideoDetailReturnCoverAlpha(0.8f, true, true, liveReturnMorph = true),
            0.0001f,
        )
        assertEquals(
            1f,
            resolveVideoDetailReturnPlayerAlpha(0.8f, true, true, liveReturnMorph = true),
            0.0001f,
        )
        // 正文按统一的 0..0.28 settle 窗口让位，播放器本身仍保持完整可见。
        assertEquals(
            1f - 0.15f / VideoCardTransitionVisualTimeline.DETAIL_CONTENT_RETURN_END,
            resolveVideoDetailReturnContentAlpha(0.85f, true, liveReturnMorph = true),
            0.0001f,
        )
        // 末段（settle=0.7 > 0.28）：正文已完全让位给源卡标题。
        val lateContent = resolveVideoDetailReturnContentAlpha(0.3f, true, liveReturnMorph = true)
        assertEquals(0f, lateContent, 0.0001f)
    }

    @Test
    fun `committed live return crossfades only during the final landing window`() {
        assertEquals(
            0f,
            resolveVideoDetailReturnCoverAlpha(0.2f, true, true, liveReturnMorph = true),
            0.0001f,
        )
        assertEquals(
            1f,
            resolveVideoDetailReturnPlayerAlpha(0.2f, true, true, liveReturnMorph = true),
            0.0001f,
        )
        // settle=0.94：最后 12% 窗口已走一半，视频与驻留封面等权交接。
        assertEquals(
            0.5f,
            resolveVideoDetailReturnCoverAlpha(0.06f, true, true, liveReturnMorph = true),
            0.0001f,
        )
        assertEquals(
            0.5f,
            resolveVideoDetailReturnPlayerAlpha(0.06f, true, true, liveReturnMorph = true),
            0.0001f,
        )
        assertEquals(
            1f,
            resolveVideoDetailReturnCoverAlpha(0f, true, true, liveReturnMorph = true),
            0.0001f,
        )
        assertEquals(
            0f,
            resolveVideoDetailReturnPlayerAlpha(0f, true, true, liveReturnMorph = true),
            0.0001f,
        )
    }

    @Test
    fun `quick committed live return hides detail content immediately to avoid dual title flash`() {
        // 源卡 chrome 快速返回立刻全显；详情标题必须马上让位，否则卸层时标题闪一下。
        assertEquals(
            0f,
            resolveVideoDetailReturnContentAlpha(
                transitionProgress = 0.9f,
                isCommittedCardReturn = true,
                liveReturnMorph = true,
                isQuickReturn = true,
            ),
            0.0001f,
        )
        assertEquals(
            0f,
            resolveVideoDetailReturnContentAlpha(
                transitionProgress = 0.2f,
                isCommittedCardReturn = true,
                liveReturnMorph = true,
                isQuickReturn = true,
            ),
            0.0001f,
        )
        // 预测返回未提交：快速返回跟 morphDepth 的统一窗口线性让位，可取消。
        assertEquals(
            1f - 0.1f / VideoCardTransitionVisualTimeline.DETAIL_CONTENT_RETURN_END,
            resolveVideoDetailReturnContentAlpha(
                transitionProgress = 0.9f,
                isCommittedCardReturn = false,
                liveReturnMorph = true,
                isQuickReturn = true,
                morphDepthProgress = 0.9f,
            ),
            0.0001f,
        )
    }

    @Test
    fun `live morph content settle follows single morphDepth clock only`() {
        // 即使 AVS progress 与 depth 不一致，只认 morphDepth，避免与源卡 chrome 叠字。
        val content = resolveVideoDetailReturnContentAlpha(
            transitionProgress = 0.95f,
            isCommittedCardReturn = true,
            liveReturnMorph = true,
            depthBlurProgress = 0.95f,
            morphDepthProgress = 0.2f,
        )
        // settle = 1 - 0.2 = 0.8 > 0.18 → 正文已让位
        assertTrue(content < 0.3f)
        assertEquals(
            resolveVideoDetailReturnContentAlpha(
                transitionProgress = 0.2f,
                isCommittedCardReturn = true,
                liveReturnMorph = true,
                morphDepthProgress = 0.2f,
            ),
            content,
            0.0001f,
        )
    }

    @Test
    fun `return session prefers LIVE upgrade and blocks LIVE demotion`() {
        val lockedResident = resolveVideoDetailReturnSessionLockedOwnership(
            lockedOwnership = null,
            isReturnSessionActive = true,
            candidateOwnership = com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.RESIDENT_COVER,
        )
        assertEquals(
            com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.RESIDENT_COVER,
            lockedResident.second,
        )
        // 首帧到达：升 LIVE，实时画面跟壳缩
        val upgraded = resolveVideoDetailReturnSessionLockedOwnership(
            lockedOwnership = lockedResident.first,
            isReturnSessionActive = true,
            candidateOwnership = com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.LIVE_SURFACE,
        )
        assertEquals(
            com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.LIVE_SURFACE,
            upgraded.second,
        )
        // 中途 candidate 变 RESIDENT：不得掐 live
        val stillLive = resolveVideoDetailReturnSessionLockedOwnership(
            lockedOwnership = upgraded.first,
            isReturnSessionActive = true,
            candidateOwnership = com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.RESIDENT_COVER,
        )
        assertEquals(
            com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.LIVE_SURFACE,
            stillLive.second,
        )
        val cleared = resolveVideoDetailReturnSessionLockedOwnership(
            lockedOwnership = stillLive.first,
            isReturnSessionActive = false,
            candidateOwnership = com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.RESIDENT_COVER,
        )
        assertEquals(null, cleared.first)
        assertEquals(
            com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.RESIDENT_COVER,
            cleared.second,
        )
    }

    @Test
    fun `cover-first committed return still hands visual ownership to resident cover`() {
        assertEquals(
            1f,
            resolveVideoDetailReturnCoverAlpha(0.8f, true, true, liveReturnMorph = false),
            0.0001f,
        )
        assertEquals(
            0f,
            resolveVideoDetailReturnPlayerAlpha(0.8f, true, true, liveReturnMorph = false),
            0.0001f,
        )
        assertEquals(
            0f,
            resolveVideoDetailReturnContentAlpha(0.8f, true, liveReturnMorph = false),
            0.0001f,
        )
    }

    @Test
    fun `uncommitted predictive live morph keeps player visible while content follows timeline`() {
        assertEquals(
            0f,
            resolveVideoDetailReturnCoverAlpha(0.8f, false, true, liveReturnMorph = true),
            0.0001f,
        )
        assertEquals(
            1f,
            resolveVideoDetailReturnPlayerAlpha(0.8f, false, true, liveReturnMorph = true),
            0.0001f,
        )
        assertEquals(
            1f - 0.15f / VideoCardTransitionVisualTimeline.DETAIL_CONTENT_RETURN_END,
            resolveVideoDetailReturnContentAlpha(0.85f, false, liveReturnMorph = true),
            0.0001f,
        )
    }

    @Test
    fun `uncommitted predictive cover-first return keeps following transition progress`() {
        assertEquals(
            0.2f,
            resolveVideoDetailReturnCoverAlpha(0.8f, false, true, liveReturnMorph = false),
            0.0001f,
        )
        assertEquals(
            0.8f,
            resolveVideoDetailReturnPlayerAlpha(0.8f, false, true, liveReturnMorph = false),
            0.0001f,
        )
        assertEquals(
            0.8f,
            resolveVideoDetailReturnContentAlpha(0.8f, false, liveReturnMorph = false),
            0.0001f,
        )
    }

    @Test
    fun `live return morph gate requires immediate playback and active shared bounds`() {
        assertTrue(
            shouldUseLiveReturnMorph(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
            )
        )
        assertFalse(
            shouldUseLiveReturnMorph(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.CoverFirst,
                detailContentReady = true,
            )
        )
        assertFalse(
            shouldUseLiveReturnMorph(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = true,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
            )
        )
        assertFalse(
            shouldUseLiveReturnMorph(
                transitionEnabled = true,
                sharedBoundsActive = false,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
            )
        )
    }

    @Test
    fun `live return morph disabled while detail content still loading to avoid skeleton in card`() {
        assertFalse(
            shouldTreatVideoDetailContentReadyForLiveReturnMorph(
                hasSuccessfulDetailContent = false,
            )
        )
        assertTrue(
            shouldTreatVideoDetailContentReadyForLiveReturnMorph(
                hasSuccessfulDetailContent = true,
            )
        )
        assertFalse(
            shouldUseLiveReturnMorph(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = false,
            )
        )
    }

    @Test
    fun `resident cover ownership is suppressed during live return morph`() {
        assertFalse(
            shouldHandVisualOwnershipToResidentCover(
                useReturningVisualState = true,
                hasResidentCover = true,
                liveReturnMorph = true,
            )
        )
        assertTrue(
            shouldHandVisualOwnershipToResidentCover(
                useReturningVisualState = true,
                hasResidentCover = true,
                liveReturnMorph = false,
            )
        )
        assertFalse(
            shouldHandVisualOwnershipToResidentCover(
                useReturningVisualState = true,
                hasResidentCover = false,
                liveReturnMorph = false,
            )
        )
    }

    @Test
    fun `detail return cover ownership table matches timeline contract`() {
        val live = resolveVideoDetailReturnCoverOwnership(
            transitionEnabled = true,
            sharedBoundsActive = true,
            keepLoadedContentForBackPreview = false,
            playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
            detailContentReady = true,
            hasResidentCover = true,
            hasRenderableLiveFrame = true,
        )
        assertTrue(isLiveReturnMorphFromOwnership(live))
        assertFalse(
            shouldHandResidentCoverFromOwnership(
                ownership = live,
                useReturningVisualState = true,
                hasResidentCover = true,
            )
        )

        val coverFirst = resolveVideoDetailReturnCoverOwnership(
            transitionEnabled = true,
            sharedBoundsActive = true,
            keepLoadedContentForBackPreview = false,
            playbackIntent = VideoSharedTransitionPlaybackIntent.CoverFirst,
            detailContentReady = true,
            hasResidentCover = true,
        )
        assertFalse(isLiveReturnMorphFromOwnership(coverFirst))
        assertTrue(
            shouldHandResidentCoverFromOwnership(
                ownership = coverFirst,
                useReturningVisualState = true,
                hasResidentCover = true,
            )
        )

        val noFrame = resolveVideoDetailReturnCoverOwnership(
            transitionEnabled = true,
            sharedBoundsActive = true,
            keepLoadedContentForBackPreview = false,
            playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
            detailContentReady = true,
            hasResidentCover = true,
            hasRenderableLiveFrame = false,
        )
        assertFalse(isLiveReturnMorphFromOwnership(noFrame))
        assertTrue(
            shouldHandResidentCoverFromOwnership(
                ownership = noFrame,
                useReturningVisualState = true,
                hasResidentCover = true,
            )
        )
    }

    @Test
    fun `cover-first entry upgrades to live surface after current video renders`() {
        val returnIntent = resolveVideoDetailReturnPlaybackIntent(
            entryPlaybackIntent = VideoSharedTransitionPlaybackIntent.CoverFirst,
            hasRenderableLiveFrame = true,
        )

        assertEquals(VideoSharedTransitionPlaybackIntent.ImmediatePlayback, returnIntent)
        val ownership = resolveVideoDetailReturnCoverOwnership(
            transitionEnabled = true,
            sharedBoundsActive = true,
            keepLoadedContentForBackPreview = false,
            playbackIntent = returnIntent,
            detailContentReady = true,
            hasResidentCover = true,
            hasRenderableLiveFrame = true,
        )
        assertTrue(isLiveReturnMorphFromOwnership(ownership))
        assertEquals(
            0f,
            resolveVideoDetailReturnCoverAlpha(
                transitionProgress = 0.5f,
                isCommittedCardReturn = false,
                hasResidentCover = true,
                liveReturnMorph = true,
            ),
            0.0001f,
        )
        assertEquals(
            1f,
            resolveVideoDetailReturnPlayerAlpha(
                transitionProgress = 0.5f,
                isCommittedCardReturn = false,
                hasResidentCover = true,
                liveReturnMorph = true,
            ),
            0.0001f,
        )
    }

    @Test
    fun `cover-first entry keeps resident cover before first frame`() {
        assertEquals(
            VideoSharedTransitionPlaybackIntent.CoverFirst,
            resolveVideoDetailReturnPlaybackIntent(
                entryPlaybackIntent = VideoSharedTransitionPlaybackIntent.CoverFirst,
                hasRenderableLiveFrame = false,
            ),
        )
    }

    @Test
    fun `detail state holder derives return intent from rendered live frame`() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt"
        ).readText()

        assertTrue(source.contains("val returnPlaybackIntent = resolveVideoDetailReturnPlaybackIntent("))
        assertTrue(source.contains("hasRenderableLiveFrame = hasRenderableLiveFrameForReturn"))
        assertTrue(source.contains("playbackIntent = returnPlaybackIntent"))
    }

    @Test
    fun `detail state holder always keeps live return preview without settings gate`() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt"
        ).readText()

        assertFalse(source.contains("getVideoTransitionLiveReturnPreviewEnabled"))
        assertFalse(source.contains("liveReturnPreviewEnabled"))
    }

    @Test
    fun `live return morph stays enabled when gate conditions are met`() {
        assertEquals(
            com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership.LIVE_SURFACE,
            resolveVideoDetailReturnCoverOwnership(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
                hasResidentCover = true,
                hasRenderableLiveFrame = true,
            ),
        )
        assertTrue(
            shouldUseLiveReturnMorph(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
                hasRenderableLiveFrame = true,
            ),
        )
    }

    @Test
    fun `missing return cover keeps player visible instead of revealing black`() {
        assertEquals(0f, resolveVideoDetailReturnCoverAlpha(0.2f, true, false), 0.0001f)
        assertEquals(1f, resolveVideoDetailReturnPlayerAlpha(0.2f, true, false), 0.0001f)
        assertEquals(0f, resolveVideoDetailReturnContentAlpha(0.2f, true), 0.0001f)
    }

    @Test
    fun `return content does not read shared transition progress`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
            .readText()

        assertTrue(source.contains("val detailTransitionProgress ="))
        assertFalse(source.contains("videoCardDepthBackgroundState"))
        assertFalse(source.contains("resolveVideoCardSecondaryContentVisualFrame("))
        assertFalse(source.contains("alpha = resolveVideoDetailReturnContentAlpha("))
        assertFalse(source.contains("val coverCrossfadeAlpha ="))
        assertFalse(source.contains("val playerFadeAlpha ="))
    }

    @Test
    fun `shared shell keeps detail content mounted while root transition owns alpha`() {
        assertTrue(
            shouldShowVideoDetailContent(
                isTransitionFinished = true,
                isLeaving = true,
                rootTransitionOwnsContentAlpha = true,
            )
        )
        assertFalse(
            shouldShowVideoDetailContent(
                isTransitionFinished = true,
                isLeaving = true,
                rootTransitionOwnsContentAlpha = false,
            )
        )
    }

    @Test
    fun `related back preview keeps parent content mounted during return morph`() {
        assertTrue(
            shouldShowVideoDetailContent(
                isTransitionFinished = false,
                isLeaving = false,
                rootTransitionOwnsContentAlpha = true,
                keepContentVisibleAfterBackPreview = true,
            )
        )
        assertFalse(
            shouldShowVideoDetailContent(
                isTransitionFinished = false,
                isLeaving = true,
                rootTransitionOwnsContentAlpha = false,
                keepContentVisibleAfterBackPreview = true,
            )
        )
    }

    @Test
    fun `explicit force cover still switches detail into returning visual state`() {
        assertTrue(
            shouldUseReturningVideoDetailVisualState(
                forceCoverOnlyForReturn = true,
                isCardReturnExitInProgress = false,
            )
        )
    }

    @Test
    fun `return session keeps business handoff without a shared morph clock`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
            .readText()
        val transitionHostSource = File(
            "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailTransitionHost.kt"
        ).readText()
        val call = source
            .substringAfter("val useReturningVideoDetailVisualState = shouldUseReturningVideoDetailVisualState(")
            .substringBefore("val handleTopBarAction")
        assertTrue(call.contains("isCardReturnExitInProgress = isCardReturnExitInProgress"))
        // session 先算 isSessionReturningToCard（含 transition/shared 门闩），再传入
        assertTrue(source.contains("val isSessionReturningToCard = isReturningFromDetail &&"))
        assertTrue(call.contains("isSessionReturningToCard = isSessionReturningToCard"))
        assertTrue(source.contains("shouldTreatVideoDetailCardReturnAsCommitted("))
        assertFalse(transitionHostSource.contains("video-detail-shared-morph-clock"))
    }

    @Test
    fun `resident cover starts return without a pre-navigation dead frame`() {
        assertEquals(0L, resolveCoverTakeoverDelayBeforeBackNavigationMillis())
    }

    @Test
    fun `resident return cover reuses the home card image cache`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
            .readText()
        val requestBlock = source
            .substringAfter("val residentCoverImageRequest =")
            .substringBefore("//  播放器容器按当前顶部避让高度计算")

        assertTrue(requestBlock.contains(".crossfade(false)"))
        assertTrue(requestBlock.contains(".placeholderMemoryCacheKey(sharedCoverCacheKey)"))
        assertTrue(requestBlock.contains(".memoryCacheKey(sharedCoverCacheKey)"))
        assertTrue(requestBlock.contains(".diskCacheKey(sharedCoverCacheKey)"))
    }

    @Test
    fun `navigation actions do not switch the loaded player to a cover`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
            .readText()
        val actionBlock = source
            .substringAfter("action@{ action: VideoDetailTopBarAction ->")
            .substringBefore("val handleBack =")

        assertFalse(actionBlock.contains("forceCoverOnlyOnReturn = true"))
        assertFalse(source.contains("useTextureSurfaceForNavigation = transitionEnabled"))
    }

    @Test
    fun `phone player container never owns cover shared bounds`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
            .readText()
        assertFalse(
            "The full PlayerView tree must stay out of sharedBounds; only the lightweight cover owns the morph.",
            source.contains("val playerContainerModifier = if (")
        )
    }

    @Test
    fun `related child transition suppresses parent detail shell shared bounds`() {
        assertTrue(
            shouldSuppressDetailShellSharedBoundsForRelatedChildTransition(
                detailBvid = "BV_A",
                lastClickedVideoSourceKey = "video/BV_A:BV_B",
                isSharedTransitionActive = true,
            )
        )
        assertFalse(
            shouldSuppressDetailShellSharedBoundsForRelatedChildTransition(
                detailBvid = "BV_A",
                lastClickedVideoSourceKey = "video/BV_A:BV_B",
                isSharedTransitionActive = false,
            )
        )
        assertFalse(
            shouldSuppressDetailShellSharedBoundsForRelatedChildTransition(
                detailBvid = "BV_A",
                lastClickedVideoSourceKey = "home:BV_A",
                isSharedTransitionActive = true,
            )
        )
        assertFalse(
            shouldSuppressDetailShellSharedBoundsForRelatedChildTransition(
                detailBvid = "BV_A",
                lastClickedVideoSourceKey = "video/BV_OTHER:BV_B",
                isSharedTransitionActive = true,
            )
        )
    }

    @Test
    fun committedReturn_doesNotCoverPlayerUntilHandoff() {
        // transitionProgress 1 = 详情全屏 settle 0；cover 必须为 0
        assertEquals(
            0f,
            resolveVideoDetailReturnCoverAlpha(
                transitionProgress = 1f,
                isCommittedCardReturn = true,
                hasResidentCover = true,
                liveReturnMorph = true,
            ),
            0.0001f,
        )
        assertEquals(
            1f,
            resolveVideoDetailReturnPlayerAlpha(
                transitionProgress = 1f,
                isCommittedCardReturn = true,
                hasResidentCover = true,
                liveReturnMorph = true,
            ),
            0.0001f,
        )
        // 非 live / CoverFirst：提交后封面立即接管，避免无帧黑壳
        assertEquals(
            1f,
            resolveVideoDetailReturnCoverAlpha(
                transitionProgress = 1f,
                isCommittedCardReturn = true,
                hasResidentCover = true,
                liveReturnMorph = false,
            ),
            0.0001f,
        )
        assertEquals(
            0f,
            resolveVideoDetailReturnPlayerAlpha(
                transitionProgress = 1f,
                isCommittedCardReturn = true,
                hasResidentCover = true,
                liveReturnMorph = false,
            ),
            0.0001f,
        )
        // settle 过 handoff 后才抬封面（progress 约 0.05 → settle 0.95）
        val coverNearEnd = resolveVideoDetailReturnCoverAlpha(
            transitionProgress = 0.05f,
            isCommittedCardReturn = true,
            hasResidentCover = true,
            liveReturnMorph = true,
        )
        assertTrue(coverNearEnd > 0.5f)
    }
}
