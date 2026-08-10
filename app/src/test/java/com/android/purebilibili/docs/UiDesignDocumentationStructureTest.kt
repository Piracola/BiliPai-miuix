package com.android.purebilibili.docs

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiDesignDocumentationStructureTest {

    private val repositoryRoot: File by lazy {
        generateSequence(File(".").canonicalFile) { it.parentFile }
            .firstOrNull { candidate ->
                File(candidate, NAV_KEY_SOURCE).isFile &&
                    File(candidate, UI_DESIGN_ROOT).isDirectory
            }
            ?: error("Cannot locate repository root from ${File(".").canonicalPath}")
    }

    @Test
    fun requiredDocumentsExistAndHaveVersionHeaders() {
        val missing = requiredDocuments.filterNot { relativePath -> file(relativePath).isFile }
        assertTrue(missing.isEmpty(), "Missing UI design documents: ${missing.joinToString()}")

        requiredDocuments.forEach { relativePath ->
            val content = file(relativePath).readText()
            requiredHeaderFields.forEach { field ->
                assertTrue(
                    content.contains(field),
                    "$relativePath is missing version header field '$field'"
                )
            }
            requiredBodySections.forEach { section ->
                assertTrue(
                    content.contains(section),
                    "$relativePath is missing documentation contract section '$section'"
                )
            }
        }
    }

    @Test
    fun localMarkdownLinksResolveToTrackedDocumentsAndAnchors() {
        val failures = mutableListOf<String>()
        markdownDocuments().forEach { source ->
            val content = source.readText()
            markdownLinkRegex.findAll(content).forEach { match ->
                val rawTarget = match.groupValues[1].trim().removeSurrounding("<", ">")
                if (
                    rawTarget.isBlank() ||
                    rawTarget.startsWith("http://") ||
                    rawTarget.startsWith("https://") ||
                    rawTarget.startsWith("mailto:")
                ) {
                    return@forEach
                }

                val pathPart = rawTarget.substringBefore('#')
                val fragment = rawTarget.substringAfter('#', missingDelimiterValue = "")
                val target = if (pathPart.isBlank()) source else File(source.parentFile, pathPart).canonicalFile
                if (!target.exists()) {
                    failures += "${relative(source)} -> $rawTarget (target missing)"
                    return@forEach
                }
                if (fragment.isNotBlank() && target.extension.equals("md", ignoreCase = true)) {
                    val targetContent = target.readText()
                    val explicitAnchor = "id=\"$fragment\""
                    if (!targetContent.contains(explicitAnchor) && !hasMarkdownHeadingAnchor(targetContent, fragment)) {
                        failures += "${relative(source)} -> $rawTarget (anchor missing)"
                    }
                }
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString(prefix = "Broken Markdown links:\n", separator = "\n"))
    }

    @Test
    fun pageCatalogCoversEveryNavigationKeyExactlyOnce() {
        val source = file(NAV_KEY_SOURCE).readText()
        val sourceKeys = navKeyDeclarationRegex.findAll(source)
            .map { it.groupValues[1] }
            .toSet()
        val catalogMarkers = navKeyCatalogRegex.findAll(file(PAGE_CATALOG).readText())
            .map { it.groupValues[1] }
            .toList()
        val duplicates = catalogMarkers.groupingBy { it }.eachCount().filterValues { it != 1 }
        val catalogKeys = catalogMarkers.toSet()

        assertTrue(duplicates.isEmpty(), "Duplicate NavKey catalog entries: $duplicates")
        assertEquals(
            sourceKeys,
            catalogKeys,
            "NavKey catalog mismatch. Missing=${sourceKeys - catalogKeys}, extra=${catalogKeys - sourceKeys}"
        )
    }

    @Test
    fun everyCatalogPageHasOneCompleteDossier() {
        val catalog = file(PAGE_CATALOG).readText()
        val catalogIds = pageCatalogIdRegex.findAll(catalog).map { it.groupValues[1] }.toList()
        assertTrue(catalogIds.isNotEmpty(), "No page IDs found in $PAGE_CATALOG")
        assertEquals(catalogIds.size, catalogIds.toSet().size, "PAGE_CATALOG contains duplicate page IDs")

        val domainDocuments = pageDomainDocuments.associateWith { file(it).readText() }
        catalogIds.forEach { pageId ->
            val catalogLine = catalog.lineSequence().firstOrNull { it.startsWith("| $pageId ") }
                ?: error("Cannot find catalog row for $pageId")
            val dossierLink = dossierLinkRegex.find(catalogLine)
                ?: error("Catalog row $pageId has no dossier link")
            val linkedId = dossierLink.groupValues[1]
            val linkedFileName = dossierLink.groupValues[2]
            val linkedAnchor = dossierLink.groupValues[3]
            assertEquals(pageId, linkedId, "$pageId links with the wrong label")
            assertEquals(pageId.lowercase(), linkedAnchor, "$pageId links to the wrong anchor")

            val relativePath = "$UI_DESIGN_ROOT/pages/$linkedFileName"
            val content = domainDocuments[relativePath]
                ?: error("$pageId links outside the declared page domain documents: $relativePath")
            val marker = "<a id=\"${pageId.lowercase()}\"></a>"
            assertEquals(1, content.windowed(marker.length).count { it == marker }, "$pageId dossier marker count")
            val start = content.indexOf(marker)
            val next = content.indexOf("<a id=\"p", startIndex = start + marker.length)
            val dossier = content.substring(start, if (next >= 0) next else content.length)
            pageDossierFields.forEach { field ->
                assertTrue(dossier.contains("**$field**"), "$pageId dossier is missing '$field'")
            }
        }

        val allDomainContent = domainDocuments.values.joinToString("\n")
        val dossierIds = dossierAnchorRegex.findAll(allDomainContent)
            .map { it.groupValues[1].uppercase() }
            .toList()
        assertEquals(catalogIds.sorted(), dossierIds.sorted(), "Domain dossier IDs differ from PAGE_CATALOG")
    }

    @Test
    fun componentCatalogUsesUniqueTargetEntriesAndCompleteCategories() {
        val componentCatalog = file("$UI_DESIGN_ROOT/components/README.md").readText()
        val targets = componentTargetRegex.findAll(componentCatalog)
            .map { it.groupValues[1] }
            .toList()
        val duplicates = targets.groupingBy { it }.eachCount().filterValues { it > 1 }
        assertTrue(targets.isNotEmpty(), "No target component entries found")
        assertTrue(duplicates.isEmpty(), "Duplicate target component entries: $duplicates")

        componentCategoryDocuments.forEach { relativePath ->
            val content = file(relativePath).readText()
            componentDossierTerms.forEach { term ->
                assertTrue(content.contains(term), "$relativePath is missing component dossier term '$term'")
            }
        }
    }

    @Test
    fun activeDesignContractsUseDualThemeTerminology() {
        val staleTerms = requiredDocuments
            .filterNot { it.endsWith("CHANGELOG.md") }
            .flatMap { relativePath ->
                file(relativePath).readLines().mapIndexedNotNull { index, line ->
                    line.takeIf { it.contains("三风格") }
                        ?.let { "$relativePath:${index + 1}: $it" }
                }
            }

        assertTrue(
            staleTerms.isEmpty(),
            staleTerms.joinToString(
                prefix = "Active UI design contracts still use the removed three-style model:\n",
                separator = "\n",
            ),
        )
    }

    private fun markdownDocuments(): List<File> = (requiredDocuments + wikiIntegrationDocuments)
        .distinct()
        .map(::file)

    private fun hasMarkdownHeadingAnchor(content: String, expected: String): Boolean = content
        .lineSequence()
        .filter { it.startsWith("#") }
        .map { heading ->
            heading.trimStart('#').trim().lowercase()
                .replace(Regex("[^\\p{L}\\p{N} -]"), "")
                .replace(' ', '-')
                .replace(Regex("-+"), "-")
        }
        .any { it == expected.lowercase() }

    private fun file(relativePath: String): File = File(repositoryRoot, relativePath)

    private fun relative(file: File): String = file.relativeTo(repositoryRoot).invariantSeparatorsPath

    companion object {
        private const val UI_DESIGN_ROOT = "docs/wiki/ui-design"
        private const val PAGE_CATALOG = "$UI_DESIGN_ROOT/pages/PAGE_CATALOG.md"
        private const val NAV_KEY_SOURCE =
            "app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavKey.kt"

        private val requiredDocuments = listOf(
            "$UI_DESIGN_ROOT/README.md",
            "$UI_DESIGN_ROOT/00_GLOSSARY.md",
            "$UI_DESIGN_ROOT/01_DIRECTION.md",
            "$UI_DESIGN_ROOT/02_FOUNDATIONS.md",
            "$UI_DESIGN_ROOT/03_THEMES.md",
            "$UI_DESIGN_ROOT/04_TYPOGRAPHY_CONTENT.md",
            "$UI_DESIGN_ROOT/05_LAYOUT_ADAPTIVE.md",
            "$UI_DESIGN_ROOT/06_MOTION_EFFECTS.md",
            "$UI_DESIGN_ROOT/07_ACCESSIBILITY.md",
            "$UI_DESIGN_ROOT/08_PAGE_TEMPLATES.md",
            "$UI_DESIGN_ROOT/09_ACCEPTANCE.md",
            "$UI_DESIGN_ROOT/10_GAP_LEDGER.md",
            "$UI_DESIGN_ROOT/11_MAINTENANCE.md",
            "$UI_DESIGN_ROOT/12_SETTINGS_UI_OPTIMIZATION_PLAN.md",
            "$UI_DESIGN_ROOT/CHANGELOG.md",
            "$UI_DESIGN_ROOT/components/README.md",
            "$UI_DESIGN_ROOT/components/PRIMITIVES.md",
            "$UI_DESIGN_ROOT/components/INPUT_SELECTION.md",
            "$UI_DESIGN_ROOT/components/NAVIGATION_CHROME.md",
            "$UI_DESIGN_ROOT/components/CARDS_LISTS_IDENTITY.md",
            "$UI_DESIGN_ROOT/components/OVERLAYS_FEEDBACK.md",
            "$UI_DESIGN_ROOT/components/MEDIA_PLAYER.md",
            PAGE_CATALOG,
            "$UI_DESIGN_ROOT/pages/APP_SHELL_HOME.md",
            "$UI_DESIGN_ROOT/pages/SEARCH_DISCOVERY.md",
            "$UI_DESIGN_ROOT/pages/COMMUNITY_MESSAGE.md",
            "$UI_DESIGN_ROOT/pages/PROFILE_LIBRARY.md",
            "$UI_DESIGN_ROOT/pages/VIDEO_PLAYBACK.md",
            "$UI_DESIGN_ROOT/pages/LIVE_BANGUMI_AUDIO.md",
            "$UI_DESIGN_ROOT/pages/SETTINGS.md",
            "$UI_DESIGN_ROOT/pages/ACCOUNT_TOOLS_WEB.md",
        )

        private val pageDomainDocuments = requiredDocuments.filter {
            it.startsWith("$UI_DESIGN_ROOT/pages/") && it != PAGE_CATALOG
        }

        private val componentCategoryDocuments = requiredDocuments.filter {
            it.startsWith("$UI_DESIGN_ROOT/components/") && !it.endsWith("/README.md")
        }

        private val requiredHeaderFields = listOf(
            "文档编号：",
            "规范版本：",
            "状态：",
            "最后核对日期：",
            "适用提交：",
            "维护角色：",
            "相关文档：",
        )

        private val requiredBodySections = listOf(
            "## 初学者解释",
            "## 规范要求",
            "## 代码映射",
            "## 当前差距",
            "## 验收方法",
        )

        private val wikiIntegrationDocuments = listOf(
            "docs/wiki/README.md",
            "docs/wiki/ARCHITECTURE.md",
            "docs/wiki/MIUIX_ALIGNMENT.md",
            "docs/wiki/QA.md",
        )

        private val pageDossierFields = listOf(
            "用户目标",
            "入口与出口",
            "信息层级",
            "文字线框",
            "设备适配",
            "组件清单",
            "状态矩阵",
            "交互和返回",
            "权限/登录",
            "性能",
            "无障碍",
            "当前差距",
            "验收",
        )

        private val componentDossierTerms = listOf(
            "用途",
            "禁用场景",
            "结构",
            "变体",
            "尺寸",
            "Token",
            "状态",
            "交互",
            "文案",
            "无障碍",
            "响应式",
            "双主题",
            "Compose 入口",
            "当前差距",
            "验收",
        )

        private val markdownLinkRegex = Regex("(?<!!)\\[[^]]+]\\(([^)]+)\\)")
        private val navKeyDeclarationRegex = Regex("(?m)^\\s*data\\s+(?:object|class)\\s+(\\w+)")
        private val navKeyCatalogRegex = Regex("\\[NAVKEY:([A-Za-z0-9_]+)]")
        private val pageCatalogIdRegex = Regex("(?m)^\\| (P\\d{3}) ")
        private val dossierLinkRegex = Regex("\\[(P\\d{3})]\\(([^)#]+)#(p\\d{3})\\)")
        private val dossierAnchorRegex = Regex("<a id=\"(p\\d{3})\"></a>")
        private val componentTargetRegex =
            Regex("(?m)^\\| C\\d{3} \\|[^|\\r\\n]*\\| `([^`]+)` \\|")
    }
}
