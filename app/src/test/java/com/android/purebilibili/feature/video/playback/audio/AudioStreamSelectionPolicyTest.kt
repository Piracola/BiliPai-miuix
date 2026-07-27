package com.android.purebilibili.feature.video.playback.audio

import com.android.purebilibili.data.model.response.Dash
import com.android.purebilibili.data.model.response.DashAudio
import com.android.purebilibili.data.model.response.Dolby
import com.android.purebilibili.data.model.response.Flac
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AudioStreamSelectionPolicyTest {

    private val standard192 = DashAudio(
        id = 30280,
        baseUrl = "https://example.com/audio-192.m4s",
        bandwidth = 192_000
    )
    private val standard64 = DashAudio(
        id = 30216,
        baseUrl = "https://example.com/audio-64.m4s",
        bandwidth = 64_000
    )
    private val dolby = DashAudio(
        id = AUDIO_QUALITY_DOLBY,
        baseUrl = "https://example.com/audio-dolby.m4s",
        bandwidth = 448_000
    )
    private val hiRes = DashAudio(
        id = AUDIO_QUALITY_HI_RES,
        baseUrl = "https://example.com/audio-hires.m4s",
        bandwidth = 1_800_000,
        mimeType = "audio/mp4",
        codecs = "fLaC"
    )

    @Test
    fun `collect candidates includes standard dolby and hi res streams`() {
        val candidates = collectAudioStreamCandidates(fullDash())

        assertEquals(
            listOf(AudioStreamKind.HI_RES, AudioStreamKind.DOLBY, AudioStreamKind.STANDARD, AudioStreamKind.STANDARD),
            candidates.map { it.kind }
        )
        assertEquals(
            listOf(AUDIO_QUALITY_HI_RES, AUDIO_QUALITY_DOLBY, 30280, 30216),
            candidates.map { it.preferenceId }
        )
    }

    @Test
    fun `auto selects best standard audio without enabling premium audio`() {
        val decision = resolveAudioStreamSelection(
            dash = fullDash(),
            requestedAudioQuality = AUDIO_QUALITY_AUTO
        )

        assertEquals(30280, decision.selectedPreferenceId)
        assertEquals(AudioStreamKind.STANDARD, decision.selected?.kind)
        assertNull(decision.fallbackReason)
    }

    @Test
    fun `explicit hi res selection returns flac candidate`() {
        val decision = resolveAudioStreamSelection(
            dash = fullDash(),
            requestedAudioQuality = AUDIO_QUALITY_HI_RES
        )

        assertEquals(AUDIO_QUALITY_HI_RES, decision.selectedPreferenceId)
        assertEquals("https://example.com/audio-hires.m4s", decision.selected?.track?.getValidUrl())
        assertNull(decision.fallbackReason)
    }

    @Test
    fun `missing hi res falls back to best standard without changing request`() {
        val decision = resolveAudioStreamSelection(
            dash = Dash(audio = listOf(standard64, standard192)),
            requestedAudioQuality = AUDIO_QUALITY_HI_RES
        )

        assertEquals(AUDIO_QUALITY_HI_RES, decision.requestedPreferenceId)
        assertEquals(30280, decision.selectedPreferenceId)
        assertEquals(AudioFallbackReason.REQUESTED_UNAVAILABLE, decision.fallbackReason)
    }

    @Test
    fun `high speed temporarily falls back from hi res to standard`() {
        val decision = resolveAudioStreamSelection(
            dash = fullDash(),
            requestedAudioQuality = AUDIO_QUALITY_HI_RES,
            playbackSpeed = 2.0f
        )

        assertEquals(AUDIO_QUALITY_HI_RES, decision.requestedPreferenceId)
        assertEquals(AUDIO_QUALITY_AUTO, decision.effectivePreferenceId)
        assertEquals(30280, decision.selectedPreferenceId)
        assertEquals(AudioFallbackReason.SPEED_INCOMPATIBLE, decision.fallbackReason)
    }

    @Test
    fun `concrete default overrides remembered manual selection`() {
        assertEquals(
            30280,
            resolveRequestedAudioQuality(
                defaultAudioQuality = 30280,
                rememberedAudioQuality = AUDIO_QUALITY_HI_RES
            )
        )
    }

    @Test
    fun `follow last default uses remembered manual selection`() {
        assertEquals(
            AUDIO_QUALITY_HI_RES,
            resolveRequestedAudioQuality(
                defaultAudioQuality = AUDIO_QUALITY_FOLLOW_LAST_SELECTED,
                rememberedAudioQuality = AUDIO_QUALITY_HI_RES
            )
        )
    }

    @Test
    fun `available options only expose tracks returned by current response`() {
        val options = buildAvailableAudioQualityOptions(
            collectAudioStreamCandidates(
                Dash(audio = listOf(standard192))
            )
        )

        assertEquals(listOf(AUDIO_QUALITY_AUTO, 30280), options.map { it.preferenceId })
        assertTrue(options.none { it.isHiRes })
    }

    private fun fullDash(): Dash {
        return Dash(
            audio = listOf(standard192, standard64),
            dolby = Dolby(audio = listOf(dolby)),
            flac = Flac(display = true, audio = hiRes)
        )
    }
}
