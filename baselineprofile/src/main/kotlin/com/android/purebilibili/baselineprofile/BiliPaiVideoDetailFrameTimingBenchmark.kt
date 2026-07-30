package com.android.purebilibili.baselineprofile

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode.WARM
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BiliPaiVideoDetailFrameTimingBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun videoDetailContentScroll_compilationPartial() = benchmarkVideoDetailContentScroll(
        compilationMode = CompilationMode.Partial()
    )

    @Test
    fun videoDetailContentScroll_compilationFull() = benchmarkVideoDetailContentScroll(
        compilationMode = CompilationMode.Full()
    )

    @Test
    fun playerSwipeGesture_compilationPartial() = benchmarkPlayerSwipeGesture(
        compilationMode = CompilationMode.Partial()
    )

    @Test
    fun playerSwipeGesture_compilationFull() = benchmarkPlayerSwipeGesture(
        compilationMode = CompilationMode.Full()
    )

    @Test
    fun videoCardOpen_compilationPartial() = benchmarkVideoCardTransition(
        closeAction = null,
    )

    @Test
    fun videoCardTopBack_compilationPartial() = benchmarkVideoCardTransition(
        closeAction = VideoCardCloseAction.TopBack,
    )

    @Test
    fun videoCardSystemBack_compilationPartial() = benchmarkVideoCardTransition(
        closeAction = VideoCardCloseAction.SystemBack,
    )

    @Test
    fun videoCardPredictiveBackComplete_compilationPartial() = benchmarkVideoCardTransition(
        closeAction = VideoCardCloseAction.PredictiveComplete,
    )

    @Test
    fun videoCardPredictiveBackCancel_compilationPartial() = benchmarkVideoCardTransition(
        closeAction = VideoCardCloseAction.PredictiveCancel,
    )

    private fun benchmarkVideoDetailContentScroll(compilationMode: CompilationMode) =
        benchmarkRule.measureRepeated(
            packageName = requireBenchmarkTargetPackage(),
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            iterations = FRAME_TIMING_BENCHMARK_ITERATIONS,
            startupMode = WARM,
            setupBlock = {
                pressHome()
                startVideoDetailActivity()
                waitForVideoDetailReady()
            }
        ) {
            repeat(3) {
                swipeDetailContent(up = true)
                swipeDetailContent(up = false)
            }
        }

    private fun benchmarkPlayerSwipeGesture(compilationMode: CompilationMode) =
        benchmarkRule.measureRepeated(
            packageName = requireBenchmarkTargetPackage(),
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            iterations = FRAME_TIMING_BENCHMARK_ITERATIONS,
            startupMode = WARM,
            setupBlock = {
                pressHome()
                startVideoDetailActivity()
                waitForVideoDetailReady()
            }
        ) {
            repeat(3) {
                swipePlayerSeek(forward = true)
                swipePlayerSeek(forward = false)
                swipePlayerVertical(up = true)
                swipePlayerVertical(up = false)
            }
        }

    private fun benchmarkVideoCardTransition(closeAction: VideoCardCloseAction?) =
        benchmarkRule.measureRepeated(
            packageName = requireBenchmarkTargetPackage(),
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = FRAME_TIMING_BENCHMARK_ITERATIONS,
            startupMode = WARM,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                waitForHomeVideoCard()
                if (closeAction != null) {
                    openFirstHomeVideoCard()
                }
            }
        ) {
            when (closeAction) {
                null -> openFirstHomeVideoCard()
                VideoCardCloseAction.TopBack -> {
                    requireNotNull(
                        device.wait(Until.findObject(By.desc("返回")), UI_WAIT_TIMEOUT_MS)
                    ).click()
                    waitForHomeVideoCard()
                }
                VideoCardCloseAction.SystemBack -> {
                    device.pressBack()
                    waitForHomeVideoCard()
                }
                VideoCardCloseAction.PredictiveComplete -> {
                    performPredictiveBackGesture(commit = true)
                    waitForHomeVideoCard()
                }
                VideoCardCloseAction.PredictiveCancel -> {
                    performPredictiveBackGesture(commit = false)
                    requireNotNull(
                        device.wait(Until.findObject(By.desc("返回")), UI_WAIT_TIMEOUT_MS)
                    )
                    device.waitForIdle()
                }
            }
        }

    private fun MacrobenchmarkScope.startVideoDetailActivity() {
        val benchmarkBvid = resolveBenchmarkBvid()
        val targetPackage = requireBenchmarkTargetPackage()
        val component = "$targetPackage/.feature.video.VideoActivity"
        device.executeShellCommand("am start -W -n $component --es bvid $benchmarkBvid")
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.waitForVideoDetailReady() {
        device.wait(Until.findObject(By.textContains("评论")), 8_000)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.waitForHomeVideoCard() {
        requireNotNull(
            device.wait(
                Until.findObject(By.descStartsWith(HOME_VIDEO_CARD_DESCRIPTION_PREFIX)),
                UI_WAIT_TIMEOUT_MS,
            )
        )
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.openFirstHomeVideoCard() {
        requireNotNull(
            device.wait(
                Until.findObject(By.descStartsWith(HOME_VIDEO_CARD_DESCRIPTION_PREFIX)),
                UI_WAIT_TIMEOUT_MS,
            )
        ).click()
        requireNotNull(
            device.wait(Until.findObject(By.desc("返回")), UI_WAIT_TIMEOUT_MS)
        )
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.performPredictiveBackGesture(commit: Boolean) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val y = device.displayHeight * 0.55f
        val edgeX = 1f
        val peakX = device.displayWidth * if (commit) 0.72f else 0.28f
        val downTime = SystemClock.uptimeMillis()

        fun inject(action: Int, x: Float) {
            val eventTime = SystemClock.uptimeMillis()
            val event = MotionEvent.obtain(
                downTime,
                eventTime,
                action,
                x,
                y,
                0,
            ).apply {
                source = InputDevice.SOURCE_TOUCHSCREEN
            }
            automation.injectInputEvent(event, true)
            event.recycle()
            SystemClock.sleep(PREDICTIVE_BACK_STEP_DELAY_MS)
        }

        inject(MotionEvent.ACTION_DOWN, edgeX)
        repeat(PREDICTIVE_BACK_GESTURE_STEPS) { index ->
            val fraction = (index + 1f) / PREDICTIVE_BACK_GESTURE_STEPS
            inject(MotionEvent.ACTION_MOVE, edgeX + (peakX - edgeX) * fraction)
        }
        if (!commit) {
            repeat(PREDICTIVE_BACK_GESTURE_STEPS) { index ->
                val fraction = (index + 1f) / PREDICTIVE_BACK_GESTURE_STEPS
                inject(MotionEvent.ACTION_MOVE, peakX + (edgeX - peakX) * fraction)
            }
        }
        inject(MotionEvent.ACTION_UP, if (commit) peakX else edgeX)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.swipeDetailContent(up: Boolean) {
        val x = device.displayWidth / 2
        val fromY = if (up) (device.displayHeight * 88) / 100 else (device.displayHeight * 45) / 100
        val toY = if (up) (device.displayHeight * 45) / 100 else (device.displayHeight * 88) / 100
        device.swipe(x, fromY, x, toY, 24)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.swipePlayerSeek(forward: Boolean) {
        val y = (device.displayHeight * 22) / 100
        val fromX = if (forward) (device.displayWidth * 22) / 100 else (device.displayWidth * 78) / 100
        val toX = if (forward) (device.displayWidth * 78) / 100 else (device.displayWidth * 22) / 100
        device.swipe(fromX, y, toX, y, 24)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.swipePlayerVertical(up: Boolean) {
        val x = (device.displayWidth * 80) / 100
        val fromY = if (up) (device.displayHeight * 30) / 100 else (device.displayHeight * 14) / 100
        val toY = if (up) (device.displayHeight * 14) / 100 else (device.displayHeight * 30) / 100
        device.swipe(x, fromY, x, toY, 24)
        device.waitForIdle()
    }

    private fun resolveBenchmarkBvid(): String {
        return requireBenchmarkBvid()
    }

    private enum class VideoCardCloseAction {
        TopBack,
        SystemBack,
        PredictiveComplete,
        PredictiveCancel,
    }

    private companion object {
        const val HOME_VIDEO_CARD_DESCRIPTION_PREFIX = "视频标题:"
        const val UI_WAIT_TIMEOUT_MS = 8_000L
        const val PREDICTIVE_BACK_GESTURE_STEPS = 12
        const val PREDICTIVE_BACK_STEP_DELAY_MS = 8L
    }
}
