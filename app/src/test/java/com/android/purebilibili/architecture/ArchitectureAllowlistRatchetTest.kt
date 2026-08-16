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
        // 冻结于接入护栏时的实测值（116 个 feature 文件引用 Core 全局对象），只能调小。
        const val MAX_CORE_GLOBAL_OBJECT_FILES = 116

        const val CORE_GLOBAL_OBJECT_FILES_SHA256 =
            "8c765f955b091d1eb50cf56ceef92eb448e549f7e9c3bc063c23a83f4d6aebc2"
    }
}
