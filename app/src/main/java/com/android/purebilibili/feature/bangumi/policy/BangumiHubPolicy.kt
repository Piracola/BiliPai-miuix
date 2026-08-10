package com.android.purebilibili.feature.bangumi

import androidx.compose.runtime.Immutable
import com.android.purebilibili.data.model.response.BangumiIndexConditionData
import com.android.purebilibili.data.model.response.TimelineDay

enum class BangumiChannel(val followType: Int, val label: String) {
    BANGUMI(MY_FOLLOW_TYPE_BANGUMI, "番剧"),
    CINEMA(MY_FOLLOW_TYPE_CINEMA, "影视"),
}

enum class BangumiHubPage {
    HOME,
    INDEX,
    FOLLOW,
    SEARCH,
}

enum class BangumiFollowStatus(val value: Int, val label: String) {
    WANT(1, "想看"),
    WATCHING(2, "在看"),
    WATCHED(3, "看过"),
}

enum class BangumiIndexCategory(
    val label: String,
    val seasonType: Int? = null,
    val indexType: Int? = null,
) {
    BANGUMI(label = "番剧", seasonType = 1),
    CINEMA_ALL(label = "全部", indexType = 102),
    MOVIE(label = "电影", indexType = 2),
    TV_SHOW(label = "电视剧", indexType = 5),
    DOCUMENTARY(label = "纪录片", indexType = 3),
    VARIETY(label = "综艺", indexType = 7),
}

@Immutable
data class BangumiIndexChoice(
    val label: String,
    val keyword: String,
    val sort: String? = null,
)

@Immutable
data class BangumiIndexFilterGroupUi(
    val field: String,
    val label: String,
    val choices: List<BangumiIndexChoice>,
)

enum class BangumiBackAction {
    CLEAR_SELECTION,
    CLOSE_SEARCH,
    SHOW_HOME,
    EXIT_SCREEN,
}

@Immutable
data class BangumiIndexQueryTarget(
    val seasonType: Int?,
    val indexType: Int?,
)

fun resolveBangumiChannel(initialType: Int): BangumiChannel = when (initialType) {
    1, 4 -> BangumiChannel.BANGUMI
    else -> BangumiChannel.CINEMA
}

fun resolveDefaultBangumiIndexCategory(channel: BangumiChannel): BangumiIndexCategory = when (channel) {
    BangumiChannel.BANGUMI -> BangumiIndexCategory.BANGUMI
    BangumiChannel.CINEMA -> BangumiIndexCategory.CINEMA_ALL
}

fun resolveBangumiIndexQueryTarget(category: BangumiIndexCategory): BangumiIndexQueryTarget =
    BangumiIndexQueryTarget(
        seasonType = category.seasonType,
        indexType = category.indexType,
    )

fun resolveBangumiBackAction(
    page: BangumiHubPage,
    hasSelection: Boolean,
): BangumiBackAction = when {
    hasSelection -> BangumiBackAction.CLEAR_SELECTION
    page == BangumiHubPage.SEARCH -> BangumiBackAction.CLOSE_SEARCH
    page != BangumiHubPage.HOME -> BangumiBackAction.SHOW_HOME
    else -> BangumiBackAction.EXIT_SCREEN
}

fun buildBangumiIndexFilterGroups(
    data: BangumiIndexConditionData,
): List<BangumiIndexFilterGroupUi> {
    val orderGroup = data.order.orEmpty().mapNotNull { option ->
        val keyword = option.field?.takeIf(String::isNotBlank) ?: return@mapNotNull null
        BangumiIndexChoice(
            label = option.name?.takeIf(String::isNotBlank) ?: keyword,
            keyword = keyword,
            sort = option.sort,
        )
    }.takeIf { it.isNotEmpty() }?.let { choices ->
        BangumiIndexFilterGroupUi(
            field = "order",
            label = "排序",
            choices = choices,
        )
    }
    val filterGroups = data.filter.orEmpty().mapNotNull { group ->
        val field = group.field?.takeIf(String::isNotBlank) ?: return@mapNotNull null
        val choices = group.values.orEmpty().mapNotNull { option ->
            val keyword = option.keyword?.takeIf(String::isNotBlank) ?: return@mapNotNull null
            BangumiIndexChoice(
                label = option.name?.takeIf(String::isNotBlank) ?: keyword,
                keyword = keyword,
            )
        }
        if (choices.isEmpty()) return@mapNotNull null
        BangumiIndexFilterGroupUi(
            field = field,
            label = group.name?.takeIf(String::isNotBlank) ?: field,
            choices = choices,
        )
    }
    return listOfNotNull(orderGroup) + filterGroups
}

fun buildDefaultBangumiIndexParams(
    groups: List<BangumiIndexFilterGroupUi>,
): Map<String, String> = buildMap {
    groups.forEach { group ->
        val choice = group.choices.firstOrNull() ?: return@forEach
        put(group.field, choice.keyword)
        if (group.field == "order") {
            choice.sort?.takeIf(String::isNotBlank)?.let { put("sort", it) }
        }
    }
}

fun updateBangumiIndexParams(
    current: Map<String, String>,
    group: BangumiIndexFilterGroupUi,
    choice: BangumiIndexChoice,
): Map<String, String> = current.toMutableMap().apply {
    put(group.field, choice.keyword)
    if (group.field == "order") {
        choice.sort?.takeIf(String::isNotBlank)?.let { put("sort", it) }
    }
}

fun mergeBangumiTimelineDays(
    bangumiDays: List<TimelineDay>,
    guochuangDays: List<TimelineDay>,
): List<TimelineDay> {
    val daysByKey = linkedMapOf<String, TimelineDay>()
    (bangumiDays + guochuangDays)
        .sortedWith(compareBy<TimelineDay> { it.dateTs }.thenBy { it.date })
        .forEach { day ->
            val key = day.date.ifBlank { day.dateTs.toString() }
            val previous = daysByKey[key]
            if (previous == null) {
                daysByKey[key] = day.copy(
                    episodes = day.episodes.orEmpty().distinctBy { it.episodeId to it.seasonId },
                )
            } else {
                daysByKey[key] = previous.copy(
                    dateTs = listOf(previous.dateTs, day.dateTs).filter { it > 0L }.minOrNull() ?: 0L,
                    dayOfWeek = previous.dayOfWeek.takeIf { it != 0 } ?: day.dayOfWeek,
                    isToday = maxOf(previous.isToday, day.isToday),
                    episodes = (previous.episodes.orEmpty() + day.episodes.orEmpty())
                        .distinctBy { it.episodeId to it.seasonId }
                        .sortedBy { it.pubTs },
                )
            }
        }
    return daysByKey.values.toList()
}

fun resolveBangumiTimelineDayLabel(day: TimelineDay): String {
    val dateLabel = day.date
        .split('-')
        .takeIf { it.size >= 3 }
        ?.let { parts ->
            val month = parts[parts.lastIndex - 1].trimStart('0').ifBlank { "0" }
            val date = parts.last().trimStart('0').ifBlank { "0" }
            "$month/$date"
        }
        ?: day.date.takeLast(5).replace('-', '/')
    if (day.isToday == 1) return "今天 $dateLabel"
    val weekday = day.dayOfWeek
        .takeIf { it in 1..7 }
        ?.let { listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")[it - 1] }
        .orEmpty()
    return listOf(weekday, dateLabel).filter(String::isNotBlank).joinToString(" ")
}

fun updateBangumiSelection(
    selectedIds: Set<Long>,
    seasonId: Long,
): Set<Long> = when {
    seasonId <= 0L -> selectedIds
    seasonId in selectedIds -> selectedIds - seasonId
    else -> selectedIds + seasonId
}

fun <T, K> mergeBangumiPagedItems(
    existing: List<T>,
    incoming: List<T>,
    reset: Boolean,
    keyOf: (T) -> K,
): List<T> = (if (reset) incoming else existing + incoming).distinctBy(keyOf)

fun resolveBangumiSelectionAfterMutation(
    selectedIds: Set<Long>,
    succeeded: Boolean,
): Set<Long> = if (succeeded) emptySet() else selectedIds
