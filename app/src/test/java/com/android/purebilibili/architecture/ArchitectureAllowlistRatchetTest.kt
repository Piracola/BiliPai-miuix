package com.android.purebilibili.architecture

import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 阶段 1 架构护栏：白名单体量棘轮。
 *
 * [ArchitectureAllowlist.CORE_GLOBAL_OBJECT_FILES] 每加一条豁免，这里必须同步把
 * 上限调大并把 SHA 摘要改成新值——两个动作都在 PR diff 里显眼，这正是
 * 「Adding a new path here is a documented exception, not a default」想要的效果。
 * 迁移干净一个文件就调小上限，让棘轮只能往一个方向走。
 */
class ArchitectureAllowlistRatchetTest {

    @Test
    fun coreGlobalObjectAllowlistDoesNotGrow() {
        assertTrue(
            ArchitectureAllowlist.CORE_GLOBAL_OBJECT_FILES.size <= MAX_CORE_GLOBAL_OBJECT_FILES,
            "CORE_GLOBAL_OBJECT_FILES 有 ${ArchitectureAllowlist.CORE_GLOBAL_OBJECT_FILES.size} 条，" +
                "超过上限 $MAX_CORE_GLOBAL_OBJECT_FILES。" +
                "这些是 UI 直接访问 Core 全局对象的存量文件；每迁移干净一个就调小上限。",
        )
    }

    @Test
    fun allowlistContentsMatchReviewedSnapshot() {
        assertEquals(
            CORE_GLOBAL_OBJECT_FILES_SHA256,
            sha256(ArchitectureAllowlist.CORE_GLOBAL_OBJECT_FILES),
            "CORE_GLOBAL_OBJECT_FILES 内容发生变化。迁移或新增豁免时请审查具体路径并更新摘要。" +
                "不能静默用同数量的新文件替换旧文件。",
        )
    }

    private fun sha256(values: Set<String>): String {
        val bytes = values.sorted().joinToString("\n").toByteArray(Charsets.UTF_8)
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }
    }

    private companion object {
        // 116 → 115：阶段 4 首个垂直切片 PartitionScreen 完成。
        // 115 → 95：护栏规则修正——*ViewModel.kt 属 Coordinator 层，由规则豁免。
        // 95 → 94：AnimationSettingsScreen 直读收拢到 VM。
        // 94 → 93：AppearanceSettingsScreen 直读收拢到 VM。
        // 93 → 92：BottomBarSettingsScreen 直读收拢到 VM（12 项 + 15 setter）。
        // 92 → 91：PlaybackSettingsScreen 直读收拢到 VM（78 项，4 个 composable）。
        // 91 → 90：SettingsScreen 根页直读收拢到 VM（18 项 + 嵌套枚举别名）。
        // 90 → 89：HomeScreen 首页直读收拢到 HomeViewModel（9 项 + 底栏可见性别名）。
        // 89 → 88：SearchScreen 搜索页直读收拢到 SearchViewModel（9 项 + 2 setter）。
        // 88 → 86：CommonListScreen 收拢到 BaseListViewModel + WatchLaterViewModel 提取
        // 为独立 *ViewModel.kt（设置直读一并收拢）。
        // 86 → 84：FavoriteCategoryScreen / FollowingListScreen 内嵌 VM 提取为
        // 独立 *ViewModel.kt 文件。
        // 84 → 83：DynamicScreen 动态页直读收拢到 DynamicViewModel（4 项 + 布局别名）。
        // 83 → 77：直播系 6 屏收拢——LiveList/LiveArea/LiveAreaDetail/LiveFollowing/
        // LiveSearch 逻辑收拢到独立 VM，LivePlayer 直读收拢到 LivePlayerViewModel。
        // 77 → 75：SpaceScreen 空间页直读收拢到 SpaceViewModel（AndroidViewModel，
        // homeSettings + 拉黑开关 + 定位提示），ProfileScreen 个人中心页直读收拢到
        // ProfileViewModel（隐私模式/主题/动态预览文本可见性 + 4 设置 StateFlow）。
        // 75 → 72：护栏规则对齐 §3.3——*UseCase.kt 与 *ViewModel.kt 同属 Coordinator 层，
        // 一并豁免；移除 video 三个 UseCase 白名单条目（SponsorBlock/Interaction/Playback）。
        // 72 → 63：video 详情页切片——StateHolder + 5 个 overlay 适配器 + PhoneContent +
        // ContentSection + TabletCinema/TabletVideo 共 9 个非播放器文件清零移出白名单。
        // 63 → 62：VideoInfoSection 清零——BGM 发现音乐经 VM 走 ViewGrpcRepository，
        // 信息默认展开/首页卡片样式/播放器控件可见性参数化下传。
        // 62 → 61：VideoPlayerSection 清零——播放器控制层设置快照
        // VideoPlayerSettingsSnapshot/Actions 参数化，弹幕面板读写、长按倍速提示、
        // 字幕偏移、云同步与评分弹幕全部经 VM 落盘。
        // 61 → 59：PortraitSubtitleOverlay + PlayerOverlayModels 清零——字幕偏移经 VM、洞察模式走 typealias。
        // 59 → 58：VideoSettingsPanel 清零——播放器设置快照与写入 action 参数化。
        const val MAX_CORE_GLOBAL_OBJECT_FILES = 58

        const val CORE_GLOBAL_OBJECT_FILES_SHA256 =
            "36b5b75933c6a67cc7b3bede12c303b416e1e36ac506a183cfb5841d959fc907"
    }
}
