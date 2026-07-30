package com.android.purebilibili.baselineprofile

import androidx.test.platform.app.InstrumentationRegistry

internal const val TARGET_PACKAGE_NAME = "com.android.purebilibili"
internal const val STARTUP_BENCHMARK_ITERATIONS = 12
internal const val FRAME_TIMING_BENCHMARK_ITERATIONS = 10
internal const val DEFAULT_BENCHMARK_MID = 2L

internal fun requireBenchmarkBvid(): String {
    val configured = InstrumentationRegistry.getArguments()
        .getString("benchmark.bvid")
        .orEmpty()
        .trim()
    require(configured.isNotBlank() && configured != "BV1xx411c7mD") {
        "Macrobenchmark requires -e benchmark.bvid <stable video BVID>; the placeholder is not a fixture."
    }
    return configured
}
