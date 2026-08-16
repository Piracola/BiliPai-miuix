package com.android.purebilibili.architecture

import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 阶段 1 架构护栏：Feature 之间不得直接访问对方内部实现。
 *
 * 目标架构里 Feature 只能通过共享契约（`feature-contracts`，阶段 6 落地）和
 * Coordinator 相互协作；跨 feature 的 `import com.android.purebilibili.feature.*`
 * 会让页面互相绑死，任何一方迁移（阶段 4 垂直切片）都变成蝴蝶效应。
 *
 * 接入时仓库已有大量跨 feature 引用存量（video→plugin、bangumi→video、
 * dynamic→video 等 53 个 feature 对），本测试用**文件级棘轮**冻结现状：
 * 每个源 feature 允许直接访问其他 feature 的文件数不得增长。迁移干净一个
 * 文件就调小对应上限——那是一个必须显式改数字、在 PR diff 里显眼的动作。
 */
class FeatureLayerIsolationStructureTest {

    @Test
    fun `per feature cross imports do not grow`() {
        val actual = countCrossFeatureFiles()
        actual.forEach { (feature, count) ->
            val limit = CROSS_FEATURE_LIMITS[feature]
            assertTrue(
                limit != null,
                "「$feature」不在冻结清单里，但存在 ${count} 个跨 feature 文件。" +
                    "新增 feature 若确需访问其他 feature，请先加入 CROSS_FEATURE_LIMITS 并写明理由。",
            )
            assertTrue(
                count <= (limit ?: 0),
                "「$feature」当前 $count 个文件跨 feature 访问，超过冻结上限 ${limit ?: 0} 个。" +
                    "迁移到共享契约后请调小上限；确需新增，请同步改数字并在 PR 里写明理由。",
            )
        }
    }

    /**
     * 内容快照：防止"同数量换文件"——一个 feature 的跨引用文件不能静默换成另一批。
     */
    @Test
    fun crossFeatureFileSnapshotMatchesReviewed() {
        assertEquals(
            SNAPSHOT_SHA256,
            sha256(crossFeatureFiles().map { it.invariantPath }.toSet()),
            "跨 feature 引用文件集合发生变化。迁移或新增引用时请审查具体路径并更新摘要。",
        )
    }

    @Test
    fun scannerActuallyReadsSources() {
        assertTrue(featureSources().size > 700, "feature 源码只扫到 ${featureSources().size} 个文件，扫描路径可能已失效")
    }

    private fun countCrossFeatureFiles(): Map<String, Int> =
        crossFeatureFiles()
            .groupingBy { file -> featureOf(file) }
            .eachCount()

    /** 所有"import 了其他 feature 包"的 feature 文件，按源 feature 分组。 */
    private fun crossFeatureFiles(): List<File> =
        featureSources().filter { file ->
            val own = featureOf(file)
            file.readText().lineSequence()
                .filter { it.startsWith("import com.android.purebilibili.feature.") }
                .any { line ->
                    val target = CROSS_FEATURE_IMPORT.find(line)?.groupValues?.get(1) ?: return@any false
                    target != own && target != "common"
                }
        }

    private fun featureOf(file: File): String =
        file.invariantPath
            .substringAfter("/feature/")
            .substringBefore("/")

    private fun featureSources(): List<File> = cachedFeature

    private fun sha256(values: Set<String>): String {
        val bytes = values.sorted().joinToString("\n").toByteArray(Charsets.UTF_8)
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }
    }

    private companion object {
        val CROSS_FEATURE_IMPORT = Regex("""feature\.([a-z]+)\.[A-Za-z0-9_.]+""")

        /** 冻结于接入棘轮时的实测文件数，只能调小。 */
        val CROSS_FEATURE_LIMITS: Map<String, Int> = mapOf(
            "video" to 24,
            "dynamic" to 8,
            "bangumi" to 7,
            "space" to 6,
            "settings" to 6,
            "list" to 6,
            "profile" to 4,
            "plugin" to 4,
            "home" to 4,
            "watchlater" to 3,
            "live" to 3,
            "search" to 2,
            "partition" to 1,
            "onboarding" to 1,
            "download" to 1,
            "category" to 1,
            "article" to 1,
        )

        val SNAPSHOT_SHA256 =
            "7045586594e8efdc3e0a75e72d11cd83569943fceb56612987f90492ebe124c9"

        val cachedFeature: List<File> by lazy {
            val roots = listOf(
                "src/main/java/com/android/purebilibili/feature",
                "app/src/main/java/com/android/purebilibili/feature",
            )
            val root = roots.map { File(it) }.firstOrNull { it.isDirectory }
                ?: error("找不到 feature 源码根目录，cwd=" + File(".").absoluteFile.canonicalPath)
            root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        }

        val File.invariantPath: String get() = path.replace('\\', '/')
    }
}
