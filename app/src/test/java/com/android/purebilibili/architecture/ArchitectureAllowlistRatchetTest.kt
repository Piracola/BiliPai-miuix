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
        // 116 → 115：阶段 4 首个垂直切片 PartitionScreen 完成（设置读取移入
        // ViewModel、M3 颜色换 AppSurfaceTokens），从白名单移除。
        // 115 → 95：护栏规则修正——*ViewModel.kt 属 Coordinator 层（依赖 Core
        // 合法），由规则豁免，20 个 ViewModel 文件移出白名单。
        const val MAX_CORE_GLOBAL_OBJECT_FILES = 95

        const val CORE_GLOBAL_OBJECT_FILES_SHA256 =
            "6c8cb8a16f2c35c7c734bd1e97ded5c7087f63bbb3ed70b1220cad61cb7ca968"
    }
}
