package com.android.purebilibili.core.ui.transition

import android.os.Trace
import android.os.Build
import java.util.concurrent.atomic.AtomicLong

/** Perfetto counters are cumulative, so a SettledHidden interval must have a zero delta. */
internal object VideoCardTransitionDiagnostics {
    private val snapshotRecords = AtomicLong()
    private val blurEffectUpdates = AtomicLong()
    private val sourceLayerDraws = AtomicLong()
    private val navBackdropDraws = AtomicLong()

    fun onSnapshotRecorded() = increment("bili.video_card.snapshot_records", snapshotRecords)

    fun onBlurEffectUpdated() = increment("bili.video_card.blur_updates", blurEffectUpdates)

    fun onSourceLayerDrawn() = increment("bili.video_card.source_layer_draws", sourceLayerDraws)

    fun onNavBackdropDrawn() = increment("bili.video_card.nav_backdrop_draws", navBackdropDraws)

    fun onExposureChanged(exposure: VideoCardTransitionExposure) {
        setCounter("bili.video_card.exposure", exposure.ordinal.toLong())
    }

    fun onDepthAnimationJobChanged(active: Boolean) {
        setCounter("bili.video_card.depth_jobs", if (active) 1L else 0L)
    }

    fun onNavigationBackJobChanged(active: Boolean) {
        setCounter("bili.video_card.back_jobs", if (active) 1L else 0L)
    }

    private fun increment(name: String, counter: AtomicLong) {
        val value = counter.incrementAndGet()
        setCounter(name, value)
    }

    private fun setCounter(name: String, value: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Trace.setCounter(name, value)
        }
    }
}
