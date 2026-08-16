package com.android.purebilibili.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 阶段 6 前置：split-package 边界守卫。
 *
 * `settings-core` 与 `network-core` 模块的源码已并入 app（阶段 6 前置完成），
 * 其 split-package 已消除。`design-system` 仍是独立 Gradle 模块，其源码包
 * `core.ui` / `core.theme` 与 app 的 `core/ui`、`core/theme` 同包共存——
 * 这是本项目的宿主架构（design-system 承载 app 的 UI 基础设施）。该同包
 * 设计当前靠「类名不重名」维持，本测试固化这一点：任何一方新增与另一方
 * 同名的顶层声明都会立即失败。
 */
class SplitPackageGuardStructureTest {

    @Test
    fun designSystemAndApp_corePackagesDoNotDeclareSameTopLevelNames() {
        val appTopLevel = collectTopLevelDeclarations(appCoreSources())
        val dsTopLevel = collectTopLevelDeclarations(designSystemSources())
        val conflicts = appTopLevel.intersect(dsTopLevel).toSortedSet()

        assertTrue(
            conflicts.isEmpty(),
            "design-system 与 app 的 core.ui/core.theme 出现同名顶层声明：$conflicts。" +
                "split-package 靠类名不重名维持，新增同名类会引发重复类冲突。",
        )
    }

    @Test
    fun scannerActuallyReadsSources() {
        assertTrue(appCoreSources().size > 40, "app core 源码只扫到 ${appCoreSources().size} 个文件")
        assertTrue(designSystemSources().size > 60, "design-system 只扫到 ${designSystemSources().size} 个文件")
    }

    private fun collectTopLevelDeclarations(files: List<File>): Set<String> {
        return files
            .map { it.readText() }
            .flatMap { text ->
                TOP_LEVEL_DECL.findAll(text)
                    .map { match -> match.groupValues[1] }
                    .toList()
            }
            .filter { name -> name.isNotBlank() }
            .toSet()
    }

    private fun appCoreSources(): List<File> =
        cachedAppMain.filter { file ->
            file.invariantPath.contains("/core/ui/") || file.invariantPath.contains("/core/theme/")
        }

    private fun designSystemSources(): List<File> = cachedDesignSystem

    private companion object {
        // 只对比「类型与属性」顶层声明：类/接口/对象/enum/数据类/val/var。
        // fun 有意排除——不同签名的顶层函数重载合法，不算 split-package 冲突。
        val TOP_LEVEL_DECL = Regex(
            """(?m)^(?:public\s+|internal\s+|private\s+|protected\s+)*(?:data\s+|sealed\s+|enum\s+|abstract\s+|open\s+)*(?:class|interface|object|val|var|const\s+val)\s+([A-Za-z_][A-Za-z0-9_]*)"""
        )

        val cachedAppMain: List<File> by lazy {
            val roots = listOf(
                "src/main/java/com/android/purebilibili",
                "app/src/main/java/com/android/purebilibili",
            )
            val root = roots.map { File(it) }.firstOrNull { it.isDirectory }
                ?: error("找不到 app main 源码根目录")
            root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        }

        val cachedDesignSystem: List<File> by lazy {
            val roots = listOf(
                "design-system/src/main/java",
                "../design-system/src/main/java",
            )
            val root = roots.map { File(it) }.firstOrNull { it.isDirectory }
                ?: error("找不到 design-system 源码根目录")
            root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        }

        val File.invariantPath: String get() = path.replace('\\', '/')
    }
}
