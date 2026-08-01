package com.android.purebilibili.feature.plugin

import com.android.purebilibili.data.model.response.SponsorCategory
import kotlinx.serialization.Serializable
import java.net.URI
import java.util.UUID

/** Per-category playback behavior, modeled after SponsorBlock-compatible clients. */
@Serializable
enum class SponsorBlockSegmentBehavior(val label: String) {
    AUTOMATIC("自动跳过"),
    MANUAL("显示跳过按钮"),
    MARKER_ONLY("仅在进度条标记"),
    DISABLED("忽略")
}

internal data class SponsorBlockCategorySetting(
    val category: String,
    val title: String,
    val description: String,
    val behavior: SponsorBlockSegmentBehavior
)

internal fun defaultSponsorBlockCategoryBehaviors(
    autoSkip: Boolean = true,
    skipSponsor: Boolean = true,
    skipIntro: Boolean = true,
    skipOutro: Boolean = true,
    skipInteraction: Boolean = true
): Map<String, SponsorBlockSegmentBehavior> {
    val fallback = if (autoSkip) SponsorBlockSegmentBehavior.AUTOMATIC else SponsorBlockSegmentBehavior.MANUAL
    return SponsorCategory.ALL_SKIP_CATEGORIES.associateWith { category ->
        when (category) {
            SponsorCategory.SPONSOR -> if (skipSponsor) fallback else SponsorBlockSegmentBehavior.DISABLED
            SponsorCategory.INTRO -> if (skipIntro) fallback else SponsorBlockSegmentBehavior.DISABLED
            SponsorCategory.OUTRO -> if (skipOutro) fallback else SponsorBlockSegmentBehavior.DISABLED
            SponsorCategory.INTERACTION -> if (skipInteraction) fallback else SponsorBlockSegmentBehavior.DISABLED
            else -> fallback
        }
    }
}

internal fun resolveSponsorBlockSegmentBehavior(
    category: String,
    rawBehaviors: Map<String, String>,
    fallback: SponsorBlockSegmentBehavior
): SponsorBlockSegmentBehavior {
    return rawBehaviors[category]
        ?.let { raw -> SponsorBlockSegmentBehavior.entries.firstOrNull { it.name == raw } }
        ?: fallback
}

internal fun resolveSponsorBlockCategorySettings(config: SponsorBlockConfig): List<SponsorBlockCategorySetting> {
    return SponsorCategory.ALL_SKIP_CATEGORIES.map { category ->
        SponsorBlockCategorySetting(
            category = category,
            title = SponsorCategory.getCategoryName(category),
            description = resolveSponsorBlockCategoryDescription(category),
            behavior = config.behaviorFor(category)
        )
    }
}

internal fun resolveSponsorBlockCategoryDescription(category: String): String = when (category) {
    SponsorCategory.SPONSOR -> "付费推广与口播恰饭"
    SponsorCategory.SELFPROMO -> "关注、点赞、商品或创作者推广"
    SponsorCategory.INTRO -> "无实际内容的开场或过场动画"
    SponsorCategory.OUTRO -> "鸣谢、结束画面与片尾"
    SponsorCategory.INTERACTION -> "一键三连、关注等互动提醒"
    SponsorCategory.PREVIEW -> "内容预告、回顾与重复片段"
    SponsorCategory.FILLER -> "与正片无关的填充或跑题内容"
    else -> "社区标注的可选片段"
}

internal fun normalizeSponsorBlockServerUrl(raw: String): String? {
    val value = raw.trim().trimEnd('/')
    if (value.isBlank()) return null
    val uri = runCatching { URI(value) }.getOrNull() ?: return null
    if (uri.scheme !in setOf("https", "http") || uri.host.isNullOrBlank()) return null
    return value
}

internal fun generateSponsorBlockUserId(): String = UUID.randomUUID().toString().replace("-", "")

internal fun shouldUploadSponsorBlockView(config: SponsorBlockConfig): Boolean {
    return config.communityTrackingEnabled && config.userId.isNotBlank()
}
