package com.android.purebilibili.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 阶段 6：目标模块包边界就绪度棘轮。
 *
 * FULL_REFACTOR_PLAN.md §3.2 规定：模块化以「先包级边界，后 Gradle 模块化」
 * 为序，只有包级边界稳定后才抽 `:feature-contracts`、`:core-model`、
 * `:core-data`、`:core-player`。本测试把每个候选模块包的就绪度**固化**：
 *
 * 1. `data/model`（→ core-model）与 `core/player`（→ core-player）当前
 *    零 feature 依赖，可直接抽取——一旦出现反向依赖立即失败。
 * 2. `data/repository`、`core/store` 存在少量反向依赖，逐条列入冻结清单
 *    （带归属），抽模块前必须先消除；清单只减不增（文件级 SHA 快照）。
 * 3. `core/network`、`core/database`、`core/lifecycle` 零 feature 依赖。
 *
 * 反向依赖 = 该包内文件 import 了 `com.android.purebilibili.feature.*`。
 */
class ModuleBoundaryReadinessStructureTest {

    @Test
    fun coreModelCandidate_hasNoFeatureDependency() {
        val offenders = sourcesUnder("data/model")
            .filter { it.readText().contains("purebilibili.feature.") }
            .map { it.invariantPath }
        assertTrue(
            offenders.isEmpty(),
            "core-model 候选包 data/model 出现 feature 依赖：${offenders}。" +
                "纯模型不得引用 feature 层，否则无法抽成独立模块。",
        )
    }

    @Test
    fun corePlayerCandidate_hasNoFeatureDependency() {
        val offenders = sourcesUnder("core/player")
            .filter { it.readText().contains("purebilibili.feature.") }
            .map { it.invariantPath }
        assertTrue(
            offenders.isEmpty(),
            "core-player 候选包 core/player 出现 feature 依赖：${offenders}。",
        )
    }

    @Test
    fun coreNetworkDatabaseLifecycle_haveNoFeatureDependency() {
        listOf("core/network", "core/database", "core/lifecycle").forEach { prefix ->
            val offenders = sourcesUnder(prefix)
                .filter { it.readText().contains("purebilibili.feature.") }
                .map { it.invariantPath }
            assertTrue(
                offenders.isEmpty(),
                "$prefix 出现 feature 依赖：${offenders}。",
            )
        }
    }

    @Test
    fun repositoryFeatureDependencies_doNotGrow() {
        val offenders = sourcesUnder("data/repository")
            .filter { it.readText().contains("purebilibili.feature.") }
            .map { it.invariantPath }
            .toSet()

        assertEquals(
            FROZEN_REPOSITORY_FEATURE_DEPENDENCIES,
            offenders,
            "data/repository 的 feature 依赖集合发生变化（冻结快照）。" +
                "抽 core-data 前必须先消除这些反向依赖；只减不增。",
        )
    }

    @Test
    fun storeFeatureDependencies_doNotGrow() {
        val offenders = sourcesUnder("core/store")
            .filter { it.readText().contains("purebilibili.feature.") }
            .map { it.invariantPath }
            .toSet()

        assertEquals(
            FROZEN_STORE_FEATURE_DEPENDENCIES,
            offenders,
            "core/store 的 feature 依赖集合发生变化（冻结快照）。" +
                "抽 core-data 前必须先消除这些反向依赖；只减不增。",
        )
    }

    @Test
    fun scannerActuallyReadsSources() {
        assertTrue(sourcesUnder("data/model").size > 30, "data/model 只扫到少量文件，路径可能失效")
        assertTrue(sourcesUnder("data/repository").size > 30, "data/repository 只扫到少量文件，路径可能失效")
        assertTrue(sourcesUnder("core/player").size >= 4, "core/player 只扫到少量文件，路径可能失效")
    }

    private fun sourcesUnder(relativePrefix: String): List<File> =
        cachedMain.filter { it.invariantPath.contains("/$relativePrefix/") }

    private companion object {
        /**
         * 冻结于 2026-08-16 的实测反向依赖（相对 app 模块根），只减不增。
         * 消除后从快照中移除并同步本集合。
         * 2026-08-16：移除 ArticleRepository（下沉 core.article）、
         * DanmakuRepository（下沉 core.danmaku：DanmakuProto/Parser/Data 簇）。
         */
        val FROZEN_REPOSITORY_FEATURE_DEPENDENCIES: Set<String> = setOf(
            "src/main/java/com/android/purebilibili/data/repository/VideoRepository.kt",
        )

        val FROZEN_STORE_FEATURE_DEPENDENCIES: Set<String> = setOf(
            "src/main/java/com/android/purebilibili/core/store/SettingsManager.kt",
            "src/main/java/com/android/purebilibili/core/store/SettingsReader.kt",
        )

        val cachedMain: List<File> by lazy {
            val roots = listOf(
                "src/main/java/com/android/purebilibili",
                "app/src/main/java/com/android/purebilibili",
            )
            val root = roots.map { File(it) }.firstOrNull { it.isDirectory }
                ?: error("找不到 main 源码根目录，cwd=" + File(".").absoluteFile.canonicalPath)
            root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        }

        val File.invariantPath: String get() = path.replace('\\', '/')
    }
}
