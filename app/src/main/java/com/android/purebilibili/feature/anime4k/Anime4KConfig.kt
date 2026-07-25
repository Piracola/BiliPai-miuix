package com.android.purebilibili.feature.anime4k

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/** Anime4K 的用户可持久化配置。启用状态由 PluginManager 单独管理。 */
@Serializable
data class Anime4KConfig(
    val preset: Anime4KPreset = Anime4KPreset.FAST
)

@Serializable
enum class Anime4KPreset {
    FAST,
    QUALITY
}

/** 与 Kazumi 一致，只提供效率和质量两档。 */
fun resolveAnime4KPresetLabel(preset: Anime4KPreset): String {
    return when (preset) {
        Anime4KPreset.FAST -> "效率档"
        Anime4KPreset.QUALITY -> "质量档"
    }
}

enum class Anime4KShaderChain {
    KAZUMI_EFFICIENCY,
    KAZUMI_QUALITY
}

/** 渲染器使用的非持久化 CNN 预设参数。 */
data class Anime4KRenderProfile(
    val shaderChain: Anime4KShaderChain
)

fun resolveAnime4KRenderProfile(preset: Anime4KPreset): Anime4KRenderProfile {
    return when (preset) {
        Anime4KPreset.FAST -> Anime4KRenderProfile(
            shaderChain = Anime4KShaderChain.KAZUMI_EFFICIENCY
        )

        Anime4KPreset.QUALITY -> Anime4KRenderProfile(
            shaderChain = Anime4KShaderChain.KAZUMI_QUALITY
        )
    }
}

/**
 * Kazumi 会让 shader 直接处理视频原始尺寸。这里只在输入超过 GPU 纹理上限时等比缩小，
 * 不能按性能档主动压低 720P/1080P，否则 CNN 会放大已经丢失的细节并产生涂抹感。
 */
fun resolveAnime4KInputSize(
    inputWidth: Int,
    inputHeight: Int,
    glMaxTextureSize: Int
): Pair<Int, Int> {
    if (inputWidth <= 0 || inputHeight <= 0 || glMaxTextureSize <= 0) return 1 to 1
    val longEdge = maxOf(inputWidth, inputHeight)
    if (longEdge <= glMaxTextureSize) return inputWidth to inputHeight

    val clampScale = glMaxTextureSize.toFloat() / longEdge.toFloat()
    return (inputWidth * clampScale).roundToInt().coerceAtLeast(1) to
        (inputHeight * clampScale).roundToInt().coerceAtLeast(1)
}
