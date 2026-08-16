package com.android.purebilibili.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 阶段 1 架构护栏：Core 业务层不得依赖 Compose。
 *
 * FULL_REFACTOR_PLAN.md §3.1 的依赖方向是
 * `App Shell → Lite UI → Feature Coordinator → Upstream Core`，
 * Core 处于最底层，只能被依赖。若 `core/network`、`core/store`、
 * `core/database` 之类业务包 import 了 `androidx.compose.*`，它们就
 * 反向拽住了 UI 层，任何 UI 改动都会触发数据层重编译，接缝和模块化
 * （阶段 2/6）也失去意义。
 *
 * 允许例外（设计系统与 UI 基础设施，天然属于 UI 层）：
 * `core/ui`、`core/theme`、`core/util`（UI 工具）、`core/plugin`（插件 API 含 UI 扩展）。
 *
 * 接入时实测这些包对 `androidx.compose.*` 的引用为 **0**，因此采用零容忍。
 */
class CoreLayerMustNotDependOnComposeStructureTest {

    @Test
    fun `core business packages do not import compose`() {
        val offenders = coreBusinessSources()
            .map { it to it.readText() }
            .flatMap { (file, text) ->
                text.lineSequence()
                    .filter { it.startsWith("import ") }
                    .filter { it.contains("androidx.compose.") }
                    .map { line -> "${file.invariantPath}: $line" }
            }
            .sorted()

        assertTrue(
            offenders.isEmpty(),
            buildString {
                append("Core 业务包引用了 Compose（${offenders.size} 处）：\n")
                offenders.forEach { append("  ").append(it).append('\n') }
                append("\n受检目录：").append(CORE_BUSINESS_PREFIXES.joinToString())
                append("\n业务逻辑不得依赖具体 UI。含 Compose 的类应移到 core/ui、core/theme 或对应 feature 层。")
            },
        )
    }

    @Test
    fun `data layer does not import compose`() {
        val offenders = dataSources()
            .map { it to it.readText() }
            .flatMap { (file, text) ->
                text.lineSequence()
                    .filter { it.startsWith("import ") }
                    .filter { it.contains("androidx.compose.") }
                    .map { line -> "${file.invariantPath}: $line" }
            }
            .sorted()

        assertTrue(
            offenders.isEmpty(),
            buildString {
                append("data 层引用了 Compose（${offenders.size} 处）：\n")
                offenders.forEach { append("  ").append(it).append('\n') }
                append("\ndata/model 与 data/repository 不得依赖 UI 框架。")
            },
        )
    }

    @Test
    fun scannerActuallyReadsSources() {
        val coreFiles = coreBusinessSources()
        val dataFiles = dataSources()
        assertTrue(coreFiles.size > 40, "core 业务源码只扫到 ${coreFiles.size} 个文件，扫描路径可能已失效")
        assertTrue(dataFiles.size > 70, "data 源码只扫到 ${dataFiles.size} 个文件，扫描路径可能已失效")
    }

    private fun coreBusinessSources(): List<File> =
        cachedMain.filter { file ->
            CORE_BUSINESS_PREFIXES.any { prefix -> file.invariantPath.contains("/$prefix/") }
        }

    private fun dataSources(): List<File> =
        cachedMain.filter { file ->
            file.invariantPath.contains("/data/")
        }

    private companion object {
        val CORE_BUSINESS_PREFIXES: List<String> = listOf(
            "core/network",
            "core/store",
            "core/player",
            "core/database",
            "core/cache",
            "core/cooldown",
            "core/coroutines",
            "core/lifecycle",
            "core/refresh",
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
