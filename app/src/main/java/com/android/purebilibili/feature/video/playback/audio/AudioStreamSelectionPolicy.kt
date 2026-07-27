package com.android.purebilibili.feature.video.playback.audio

import com.android.purebilibili.data.model.response.Dash
import com.android.purebilibili.data.model.response.DashAudio
import com.android.purebilibili.feature.video.playback.policy.resolveSpeedCompatibleAudioQualityPreference

fun resolveRequestedAudioQuality(
    defaultAudioQuality: Int,
    rememberedAudioQuality: Int
): Int {
    return if (defaultAudioQuality == AUDIO_QUALITY_FOLLOW_LAST_SELECTED) {
        rememberedAudioQuality
    } else {
        defaultAudioQuality
    }
}

fun collectAudioStreamCandidates(dash: Dash): List<AudioStreamCandidate> {
    val standard = dash.audio.orEmpty()
        .filter { it.getValidUrl().isNotBlank() }
        .map { track ->
            AudioStreamCandidate(
                preferenceId = track.id,
                kind = AudioStreamKind.STANDARD,
                label = resolveStandardAudioLabel(track),
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

    val explicitOptions = candidates
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
                isHiRes = candidate.kind == AudioStreamKind.HI_RES
            )
        }

    val hasStandardAudio = candidates.any { it.kind == AudioStreamKind.STANDARD }
    return if (hasStandardAudio) {
        listOf(
            AudioQualityOption(
                preferenceId = AUDIO_QUALITY_AUTO,
                kind = null,
                label = "自动（普通最佳）"
            )
        ) + explicitOptions
    } else {
        explicitOptions
    }
}

fun resolveAudioStreamSelection(
    dash: Dash,
    requestedAudioQuality: Int,
    playbackSpeed: Float = 1.0f
): AudioSelectionDecision {
    val candidates = collectAudioStreamCandidates(dash)
    val availableOptions = buildAvailableAudioQualityOptions(candidates)
    if (candidates.isEmpty()) {
        return AudioSelectionDecision(
            requestedPreferenceId = requestedAudioQuality,
            effectivePreferenceId = AUDIO_QUALITY_AUTO,
            selected = null,
            availableOptions = emptyList(),
            fallbackReason = AudioFallbackReason.NO_PLAYABLE_AUDIO
        )
    }

    val effectivePreference = resolveSpeedCompatibleAudioQualityPreference(
        requestedAudioQuality = requestedAudioQuality,
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
        effectivePreference != requestedAudioQuality ->
            AudioFallbackReason.SPEED_INCOMPATIBLE
        effectivePreference != AUDIO_QUALITY_AUTO && exactSelection == null ->
            AudioFallbackReason.REQUESTED_UNAVAILABLE
        else -> null
    }

    return AudioSelectionDecision(
        requestedPreferenceId = requestedAudioQuality,
        effectivePreferenceId = effectivePreference,
        selected = selected,
        availableOptions = availableOptions,
        fallbackReason = fallbackReason
    )
}

private fun resolveStandardAudioLabel(track: DashAudio): String {
    return when (track.id) {
        30280 -> "192K"
        30232 -> "132K"
        30216 -> "64K"
        else -> {
            val bitrateKbps = track.bandwidth
                .takeIf { it > 0 }
                ?.div(1000)
            bitrateKbps?.let { "${it}K" } ?: "音质 ${track.id}"
        }
    }
}

private fun audioKindOrder(kind: AudioStreamKind): Int {
    return when (kind) {
        AudioStreamKind.HI_RES -> 0
        AudioStreamKind.DOLBY -> 1
        AudioStreamKind.STANDARD -> 2
    }
}
