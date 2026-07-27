package com.android.purebilibili.feature.video.playback.audio

import com.android.purebilibili.data.model.response.Dash
import com.android.purebilibili.feature.video.playback.policy.resolveSpeedCompatibleAudioQualityPreference

fun resolveRequestedAudioQuality(
    defaultAudioQuality: Int,
    rememberedAudioQuality: Int
): Int {
    val resolvedPreference = if (defaultAudioQuality == AUDIO_QUALITY_FOLLOW_LAST_SELECTED) {
        rememberedAudioQuality
    } else {
        defaultAudioQuality
    }
    return normalizeAudioQualityPreference(resolvedPreference)
}

fun normalizeAudioQualityPreference(preferenceId: Int): Int {
    return when (preferenceId) {
        AUDIO_QUALITY_HI_RES,
        AUDIO_QUALITY_DOLBY,
        AUDIO_QUALITY_AUTO -> preferenceId
        else -> AUDIO_QUALITY_AUTO
    }
}

fun collectAudioStreamCandidates(dash: Dash): List<AudioStreamCandidate> {
    val standard = dash.audio.orEmpty()
        .filter { it.getValidUrl().isNotBlank() }
        .map { track ->
            AudioStreamCandidate(
                preferenceId = track.id,
                kind = AudioStreamKind.STANDARD,
                label = "高品质 AAC",
                track = track
            )
        }
    val dolby = dash.dolby?.audio.orEmpty()
        .filter { it.getValidUrl().isNotBlank() }
        .map { track ->
            AudioStreamCandidate(
                preferenceId = AUDIO_QUALITY_DOLBY,
                kind = AudioStreamKind.DOLBY,
                label = "杜比全景声",
                track = track
            )
        }
    val hiRes = dash.flac?.audio
        ?.takeIf { it.getValidUrl().isNotBlank() }
        ?.let { track ->
            AudioStreamCandidate(
                preferenceId = AUDIO_QUALITY_HI_RES,
                kind = AudioStreamKind.HI_RES,
                label = "Hi-Res 无损",
                track = track
            )
        }
        ?.let(::listOf)
        .orEmpty()

    return (hiRes + dolby + standard)
        .distinctBy { candidate ->
            Triple(
                candidate.preferenceId,
                candidate.kind,
                candidate.track.getValidUrl()
            )
        }
}

fun buildAvailableAudioQualityOptions(
    candidates: List<AudioStreamCandidate>
): List<AudioQualityOption> {
    if (candidates.isEmpty()) return emptyList()

    val premiumOptions = candidates
        .filter { it.kind != AudioStreamKind.STANDARD }
        .groupBy { it.preferenceId }
        .mapNotNull { (_, groupedCandidates) ->
            groupedCandidates.maxByOrNull { it.track.bandwidth }
        }
        .sortedWith(
            compareBy<AudioStreamCandidate> { audioKindOrder(it.kind) }
                .thenByDescending { it.track.bandwidth }
        )
        .map { candidate ->
            AudioQualityOption(
                preferenceId = candidate.preferenceId,
                kind = candidate.kind,
                label = candidate.label,
                isHiRes = candidate.kind == AudioStreamKind.HI_RES,
                isDolby = candidate.kind == AudioStreamKind.DOLBY
            )
        }

    val hasStandardAudio = candidates.any { it.kind == AudioStreamKind.STANDARD }
    return premiumOptions + if (hasStandardAudio) {
        listOf(
            AudioQualityOption(
                preferenceId = AUDIO_QUALITY_AUTO,
                kind = AudioStreamKind.STANDARD,
                label = "高品质 AAC"
            )
        )
    } else {
        emptyList()
    }
}

fun resolveAudioStreamSelection(
    dash: Dash,
    requestedAudioQuality: Int,
    playbackSpeed: Float = 1.0f
): AudioSelectionDecision {
    val normalizedRequestedAudioQuality = normalizeAudioQualityPreference(requestedAudioQuality)
    val candidates = collectAudioStreamCandidates(dash)
    val availableOptions = buildAvailableAudioQualityOptions(candidates)
    if (candidates.isEmpty()) {
        return AudioSelectionDecision(
            requestedPreferenceId = normalizedRequestedAudioQuality,
            effectivePreferenceId = AUDIO_QUALITY_AUTO,
            selected = null,
            availableOptions = emptyList(),
            fallbackReason = AudioFallbackReason.NO_PLAYABLE_AUDIO
        )
    }

    val effectivePreference = resolveSpeedCompatibleAudioQualityPreference(
        requestedAudioQuality = normalizedRequestedAudioQuality,
        playbackSpeed = playbackSpeed
    )
    val bestStandard = candidates
        .filter { it.kind == AudioStreamKind.STANDARD }
        .maxByOrNull { it.track.bandwidth }

    val exactSelection = if (effectivePreference == AUDIO_QUALITY_AUTO) {
        bestStandard
    } else {
        candidates
            .filter { it.preferenceId == effectivePreference }
            .maxByOrNull { it.track.bandwidth }
    }
    val selected = exactSelection ?: bestStandard
    val fallbackReason = when {
        selected == null -> AudioFallbackReason.NO_PLAYABLE_AUDIO
        effectivePreference != normalizedRequestedAudioQuality ->
            AudioFallbackReason.SPEED_INCOMPATIBLE
        effectivePreference != AUDIO_QUALITY_AUTO && exactSelection == null ->
            AudioFallbackReason.REQUESTED_UNAVAILABLE
        else -> null
    }

    return AudioSelectionDecision(
        requestedPreferenceId = normalizedRequestedAudioQuality,
        effectivePreferenceId = effectivePreference,
        selected = selected,
        availableOptions = availableOptions,
        fallbackReason = fallbackReason
    )
}

private fun audioKindOrder(kind: AudioStreamKind): Int {
    return when (kind) {
        AudioStreamKind.HI_RES -> 0
        AudioStreamKind.DOLBY -> 1
        AudioStreamKind.STANDARD -> 2
    }
}
