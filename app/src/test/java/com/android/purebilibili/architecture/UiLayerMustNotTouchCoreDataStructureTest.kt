package com.android.purebilibili.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 阶段 1 架构护栏：UI（feature）不得直接访问 Core 全局对象。
 *
 * 全局对象（SettingsManager / NetworkModule / TokenManager / AccountSessionStore /
 * DatabaseModule / AppDatabase / data/repository 单例）是业务层资产。Composable
 * 与 feature 页面直接引用它们，等于把持久化、网络、账号状态绑死在具体实现上——
 * 这是 FULL_REFACTOR_PLAN.md 阶段 2 接缝要拆掉的耦合，本测试负责让它在接入后
 * **不再扩大**。
 *
 * 判定口径：**文件级**豁免而非调用点级。一个文件只要落在
 * [ArchitectureAllowlist.CORE_GLOBAL_OBJECT_FILES] 里，本测试就放行整个文件；
 * 白名单外的 feature 文件出现任何一个被禁止符号的调用，立即失败。
 * `*ViewModel.kt` 文件属于 Coordinator 层（依赖 Core 合法），由规则豁免。
 * 文件被阶段 2/4 的切片迁移干净后，从白名单移除——那是一个必须同步调小
 * 棘轮上限、更新 SHA 摘要的显眼动作。
 */
class UiLayerMustNotTouchCoreDataStructureTest {

    @Test
    fun `feature files outside allowlist do not reference core global objects`() {
        val offenders = featureSources()
            .filterNot { it.invariantPath in ArchitectureAllowlist.CORE_GLOBAL_OBJECT_FILES }
            .filterNot { it.name.endsWith("ViewModel.kt") }
            .map { it to it.readText() }
            .flatMap { (file, text) ->
                FORBIDDEN_SYMBOLS
                    .filter { symbol -> wordBoundary(symbol).containsMatchIn(text) }
                    .map { symbol -> "${file.invariantPath} -> $symbol" }
            }
            .sorted()

        assertTrue(
            offenders.isEmpty(),
            buildString {
                append("白名单外的 feature 文件直接引用 Core 全局对象（${offenders.size} 处）：\n")
                offenders.take(60).forEach { append("  ").append(it).append('\n') }
                if (offenders.size > 60) append("  … 共 ${offenders.size} 处\n")
                append("\n被禁止的符号：").append(FORBIDDEN_SYMBOLS.joinToString())
                append("\n处置方式：把该文件的调用迁到 Coordinator/ViewModel/UseCase，" +
                    "或按阶段 2 规则登记进 ArchitectureAllowlist（需同步调小棘轮并更新 SHA）。")
            },
        )
    }

    /**
     * 扫描器自检：防止"路径失效导致一个文件都没扫到，于是全绿"。
     */
    @Test
    fun scannerActuallyReadsSources() {
        assertTrue(featureSources().size > 700, "feature 源码只扫到 ${featureSources().size} 个文件，扫描路径可能已失效")
        assertTrue(
            ArchitectureAllowlist.CORE_GLOBAL_OBJECT_FILES.size >= 60,
            "白名单只读到了 ${ArchitectureAllowlist.CORE_GLOBAL_OBJECT_FILES.size} 条，允许清单可能已失效",
        )
    }

    private fun featureSources(): List<File> = cachedFeature

    /**
     * 单词边界匹配，与白名单生成口径一致：`by SettingsManager`（Kotlin 委托）、
     * 全限定名 `com.android.purebilibili.core.store.SettingsManager`、import 语句
     * 都能命中；同时不会误伤 `SettingsManagerSizeRatchetTest` 这类含子串的标识符。
     */
    private fun wordBoundary(symbol: String): Regex = Regex("\\b$symbol\\b")

    private companion object {
        val FORBIDDEN_SYMBOLS: List<String> = listOf(
            "NetworkModule",
            "TokenManager",
            "AccountSessionStore",
            "DatabaseModule",
            "AppDatabase",
            "SettingsManager",
            "PlayerSettingsStore",
            "NetworkProxyStore",
            "VideoRepository",
            "ActionRepository",
            "ArticleRepository",
            "BangumiRepository",
            "CommentRepository",
            "DanmakuRepository",
            "DynamicRepository",
            "FavoriteRepository",
            "HistoryRepository",
            "LikedVideosRepository",
            "LiveRepository",
            "MessageRepository",
            "MessageSendPayloadFactory",
            "PersonalFavoriteRepository",
            "SearchRepository",
            "SplashRepository",
            "SponsorBlockRepository",
            "TopicRepository",
            "VideoNoteRepository",
            "WatchLaterRepository",
        )

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
