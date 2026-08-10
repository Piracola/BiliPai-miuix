package com.android.purebilibili.feature.bangumi

import com.android.purebilibili.data.model.response.BangumiIndexConditionData
import com.android.purebilibili.data.model.response.BangumiIndexConditionFilter
import com.android.purebilibili.data.model.response.BangumiIndexConditionOrder
import com.android.purebilibili.data.model.response.BangumiIndexConditionValue
import com.android.purebilibili.data.model.response.TimelineDay
import com.android.purebilibili.data.model.response.TimelineEpisode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BangumiHubPolicyTest {
    @Test
    fun `initial types map to the two PiliPlus channels`() {
        assertEquals(BangumiChannel.BANGUMI, resolveBangumiChannel(1))
        assertEquals(BangumiChannel.BANGUMI, resolveBangumiChannel(4))
        assertEquals(BangumiChannel.CINEMA, resolveBangumiChannel(2))
        assertEquals(BangumiChannel.CINEMA, resolveBangumiChannel(7))
    }

    @Test
    fun `back handling prioritizes selection and nested pages`() {
        assertEquals(
            BangumiBackAction.CLEAR_SELECTION,
            resolveBangumiBackAction(BangumiHubPage.FOLLOW, hasSelection = true),
        )
        assertEquals(
            BangumiBackAction.CLOSE_SEARCH,
            resolveBangumiBackAction(BangumiHubPage.SEARCH, hasSelection = false),
        )
        assertEquals(
            BangumiBackAction.SHOW_HOME,
            resolveBangumiBackAction(BangumiHubPage.INDEX, hasSelection = false),
        )
        assertEquals(
            BangumiBackAction.EXIT_SCREEN,
            resolveBangumiBackAction(BangumiHubPage.HOME, hasSelection = false),
        )
    }

    @Test
    fun `server conditions produce default index parameters`() {
        val groups = buildBangumiIndexFilterGroups(
            BangumiIndexConditionData(
                order = listOf(BangumiIndexConditionOrder(field = "3", name = "追番人数", sort = "0")),
                filter = listOf(
                    BangumiIndexConditionFilter(
                        field = "style_id",
                        name = "风格",
                        values = listOf(
                            BangumiIndexConditionValue(keyword = "-1", name = "全部"),
                            BangumiIndexConditionValue(keyword = "25", name = "历史"),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(listOf("order", "style_id"), groups.map { it.field })
        assertEquals(mapOf("order" to "3", "sort" to "0", "style_id" to "-1"), buildDefaultBangumiIndexParams(groups))
        assertEquals(
            "25",
            updateBangumiIndexParams(
                current = buildDefaultBangumiIndexParams(groups),
                group = groups[1],
                choice = groups[1].choices[1],
            )["style_id"],
        )
    }

    @Test
    fun `bangumi and guochuang timelines merge by date and deduplicate episodes`() {
        val shared = TimelineEpisode(episodeId = 2, seasonId = 20, pubTs = 20)
        val result = mergeBangumiTimelineDays(
            bangumiDays = listOf(
                TimelineDay(
                    date = "2026-08-10",
                    dateTs = 10,
                    dayOfWeek = 1,
                    isToday = 1,
                    episodes = listOf(TimelineEpisode(episodeId = 1, seasonId = 10, pubTs = 10), shared),
                ),
            ),
            guochuangDays = listOf(
                TimelineDay(
                    date = "2026-08-10",
                    dateTs = 10,
                    dayOfWeek = 1,
                    episodes = listOf(shared, TimelineEpisode(episodeId = 3, seasonId = 30, pubTs = 30)),
                ),
            ),
        )

        assertEquals(1, result.size)
        assertEquals(1, result.single().isToday)
        assertEquals(listOf(1L, 2L, 3L), result.single().episodes.orEmpty().map { it.episodeId })
    }

    @Test
    fun `timeline labels include visible dates and today marker`() {
        assertEquals(
            "周五 8/8",
            resolveBangumiTimelineDayLabel(
                TimelineDay(date = "2026-08-08", dayOfWeek = 5),
            ),
        )
        assertEquals(
            "今天 8/10",
            resolveBangumiTimelineDayLabel(
                TimelineDay(date = "2026-08-10", dayOfWeek = 7, isToday = 1),
            ),
        )
    }

    @Test
    fun `selection toggles valid ids only`() {
        assertEquals(setOf(9L), updateBangumiSelection(emptySet(), 9L))
        assertTrue(updateBangumiSelection(setOf(9L), 9L).isEmpty())
        assertEquals(setOf(9L), updateBangumiSelection(setOf(9L), 0L))
    }

    @Test
    fun `cinema index categories produce PiliPlus query targets`() {
        assertEquals(
            BangumiIndexQueryTarget(seasonType = null, indexType = 102),
            resolveBangumiIndexQueryTarget(BangumiIndexCategory.CINEMA_ALL),
        )
        assertEquals(
            BangumiIndexQueryTarget(seasonType = null, indexType = 2),
            resolveBangumiIndexQueryTarget(BangumiIndexCategory.MOVIE),
        )
        assertEquals(
            BangumiIndexQueryTarget(seasonType = 1, indexType = null),
            resolveBangumiIndexQueryTarget(BangumiIndexCategory.BANGUMI),
        )
    }

    @Test
    fun `pagination de-duplicates and reset drops old page`() {
        assertEquals(
            listOf(1, 2, 3),
            mergeBangumiPagedItems(listOf(1, 2), listOf(2, 3), reset = false) { it },
        )
        assertEquals(
            listOf(2, 3),
            mergeBangumiPagedItems(listOf(1), listOf(2, 2, 3), reset = true) { it },
        )
    }

    @Test
    fun `failed batch mutation preserves selection`() {
        val selected = setOf(2L, 4L)
        assertEquals(selected, resolveBangumiSelectionAfterMutation(selected, succeeded = false))
        assertTrue(resolveBangumiSelectionAfterMutation(selected, succeeded = true).isEmpty())
    }
}
