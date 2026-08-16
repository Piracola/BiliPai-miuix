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
        const val MAX_CORE_GLOBAL_OBJECT_FILES = 88

        const val CORE_GLOBAL_OBJECT_FILES_SHA256 =
            "6f995d9ee261961358d1ee149ade6a27933c6f16912de0f261df94def762a384"
    }
}
