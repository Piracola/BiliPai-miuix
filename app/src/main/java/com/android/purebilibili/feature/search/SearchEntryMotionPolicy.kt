package com.android.purebilibili.feature.search

enum class SearchEntryMotionSource {
    NONE,
    BOTTOM_BAR
}

data class SearchEntryMotionSpec(
    val durationMillis: Int,
    val initialAlpha: Float,
    val initialScale: Float,
    val initialTranslationYDp: Float,
    val transformOriginPivotX: Float,
    val transformOriginPivotY: Float
)

internal fun resolveSearchEntryMotionSpec(
    @Suppress("UNUSED_PARAMETER") source: SearchEntryMotionSource,
    @Suppress("UNUSED_PARAMETER") reducedMotionBudget: Boolean
): SearchEntryMotionSpec? {
    return null
}
