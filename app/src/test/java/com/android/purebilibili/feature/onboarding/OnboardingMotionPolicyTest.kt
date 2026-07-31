package com.android.purebilibili.feature.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingMotionPolicyTest {

    @Test
    fun pageCountKeepsSettingsGuideAsFinalPage() {
        assertEquals(5, resolveOnboardingPageCount())
        assertEquals(4, resolveOnboardingLastPageIndex())
    }

    @Test
    fun normalMotionAlsoUsesStaticPageAndHeroState() {
        val spec = resolveOnboardingMotionSpec(reduceMotion = false)

        assertEquals(1f, spec.pager.minScale)
        assertEquals(1f, spec.pager.minAlpha)
        assertEquals(0f, spec.floating.translationYPx)
        assertEquals(0, spec.floating.durationMillis)
        assertEquals(1f, spec.halo.minScale)
        assertEquals(1f, spec.halo.maxScale)
        assertEquals(1f, spec.card.selectedScale)
    }

    @Test
    fun reduceMotionDisablesLoopingMotion() {
        val spec = resolveOnboardingMotionSpec(reduceMotion = true)

        assertEquals(1f, spec.pager.minScale)
        assertEquals(1f, spec.pager.minAlpha)
        assertEquals(0f, spec.floating.translationYPx)
        assertEquals(0, spec.floating.durationMillis)
        assertEquals(1f, spec.halo.minScale)
        assertEquals(1f, spec.halo.maxScale)
        assertEquals(1f, spec.card.selectedScale)
    }
}
