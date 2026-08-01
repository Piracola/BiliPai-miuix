package com.android.purebilibili.feature.video.danmaku

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource

private const val REGEX_RULE_PREFIX = "regex:"
private const val SHORT_REGEX_RULE_PREFIX = "re:"
private const val USER_HASH_RULE_PREFIX = "uid:"
private const val USER_RULE_PREFIX = "user:"
private const val HASH_RULE_PREFIX = "hash:"
private val DANMAKU_RULE_SPLITTER = Regex("[\\n,，]+")
private val DANMAKU_BLOCK_RULE_JSON = Json { ignoreUnknownKeys = true }

enum class DanmakuBlockRuleGroup {
    KEYWORD,
    REGEX,
    USER_HASH
}

data class DanmakuBlockRuleSections(
    val keywordRules: List<String> = emptyList(),
    val regexRules: List<String> = emptyList(),
    val userHashRules: List<String> = emptyList()
)

data class DanmakuBlockRuleImportResult(
    val rules: List<String> = emptyList(),
    val invalidEntries: List<String> = emptyList(),
    val skippedDisabledCount: Int = 0,
    val errorMessage: String? = null
) {
    val sections: DanmakuBlockRuleSections
        get() = partitionDanmakuBlockRules(rules)

    val canImport: Boolean
        get() = errorMessage == null && rules.isNotEmpty()
}

internal sealed interface DanmakuBlockRuleMatcher {
    fun matches(content: String, userHash: String = ""): Boolean
}

internal data class DanmakuKeywordMatcher(
    val keyword: String
) : DanmakuBlockRuleMatcher {
    override fun matches(content: String, userHash: String): Boolean {
        return content.contains(keyword, ignoreCase = true)
    }
}

internal data class DanmakuRegexMatcher(
    val regex: Regex
) : DanmakuBlockRuleMatcher {
    override fun matches(content: String, userHash: String): Boolean {
        return regex.containsMatchIn(content)
    }
}

internal data class DanmakuUserHashMatcher(
    val userHashRule: String
) : DanmakuBlockRuleMatcher {
    override fun matches(content: String, userHash: String): Boolean {
        if (userHash.isBlank()) return false
        return userHash.equals(userHashRule, ignoreCase = true)
    }
}

fun parseDanmakuBlockRules(raw: String): List<String> {
    parseDanmakuBlockRulesJson(raw)?.let { return it }
    return raw.split(DANMAKU_RULE_SPLITTER)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
}

fun parseDanmakuBlockRuleImport(raw: String): DanmakuBlockRuleImportResult {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) {
        return DanmakuBlockRuleImportResult(errorMessage = "文件内容为空")
    }
    return when {
        trimmed.startsWith("<") -> parseDanmakuBlockRuleXmlImport(trimmed)
        trimmed.startsWith("{") || trimmed.startsWith("[") -> {
            val rules = parseDanmakuBlockRulesJson(trimmed)
                ?: return DanmakuBlockRuleImportResult(errorMessage = "JSON 屏蔽规则格式无效")
            buildDanmakuBlockRuleImportResult(rules)
        }
        else -> buildDanmakuBlockRuleImportResult(
            trimmed.split(DANMAKU_RULE_SPLITTER)
        )
    }
}

private fun parseDanmakuBlockRuleXmlImport(raw: String): DanmakuBlockRuleImportResult {
    if (raw.contains("<!DOCTYPE", ignoreCase = true)) {
        return DanmakuBlockRuleImportResult(errorMessage = "XML 屏蔽规则不支持 DOCTYPE")
    }
    val candidates = mutableListOf<String>()
    var skippedDisabledCount = 0
    return try {
        val document = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isExpandEntityReferences = false
            isXIncludeAware = false
        }.newDocumentBuilder().parse(InputSource(StringReader(raw)))
        val nodes = document.getElementsByTagName("*")
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as? Element ?: continue
            if (!element.tagName.equals("item", ignoreCase = true) &&
                !element.tagName.equals("filter", ignoreCase = true)
            ) {
                continue
            }
            val enabled = element.getAttribute("enabled")
                .trim()
                .lowercase()
            val value = element.textContent.orEmpty().trim()
            if (enabled == "false" || enabled == "0") {
                skippedDisabledCount++
            } else if (value.isNotEmpty()) {
                candidates += value
            }
        }
        buildDanmakuBlockRuleImportResult(
            candidates = candidates,
            skippedDisabledCount = skippedDisabledCount
        )
    } catch (error: Exception) {
        DanmakuBlockRuleImportResult(
            skippedDisabledCount = skippedDisabledCount,
            errorMessage = "XML 屏蔽规则格式无效：${error.message.orEmpty()}".trimEnd('：')
        )
    }
}

private fun buildDanmakuBlockRuleImportResult(
    candidates: List<String>,
    skippedDisabledCount: Int = 0
): DanmakuBlockRuleImportResult {
    val valid = mutableListOf<String>()
    val invalid = mutableListOf<String>()
    candidates.forEach { candidate ->
        val normalized = normalizeDanmakuImportedRule(candidate)
        if (normalized == null || resolveDanmakuBlockRuleMatcher(normalized) == null) {
            candidate.trim().takeIf { it.isNotEmpty() }?.let(invalid::add)
        } else {
            valid += normalized
        }
    }
    return DanmakuBlockRuleImportResult(
        rules = valid.distinct(),
        invalidEntries = invalid.distinct(),
        skippedDisabledCount = skippedDisabledCount
    )
}

private fun normalizeDanmakuImportedRule(rule: String): String? {
    val normalized = rule.trim()
    if (normalized.isEmpty()) return null
    val prefix = normalized.take(2).lowercase()
    val body = normalized.drop(2).trim()
    return when (prefix) {
        "t=" -> body.takeIf(String::isNotEmpty)
        "r=" -> body.takeIf(String::isNotEmpty)?.let { "$REGEX_RULE_PREFIX$it" }
        "u=" -> body.takeIf(String::isNotEmpty)?.let { "$USER_HASH_RULE_PREFIX$it" }
        else -> normalizeDanmakuBlockRuleForAppend(normalized)
    }
}

private fun parseDanmakuBlockRulesJson(raw: String): List<String>? {
    val trimmed = raw.trim()
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return null
    val element = runCatching { DANMAKU_BLOCK_RULE_JSON.parseToJsonElement(trimmed) }.getOrNull()
        ?: return null
    return when (element) {
        is JsonArray -> element.toRuleStrings()
        is JsonObject -> element.toRuleStrings()
        else -> null
    }?.distinct()?.takeIf { it.isNotEmpty() }
}

private fun JsonObject.toRuleStrings(): List<String> {
    val keywordRules = readStringArray("keywords", "keywordRules", "keyword", "words")
        .map(String::trim)
        .filter(String::isNotEmpty)
    val regexRules = readStringArray("regex", "regexRules", "regexp")
        .mapNotNull(::normalizeDanmakuRegexImportRule)
    val userHashRules = readStringArray("userHashes", "userHashRules", "uids", "users")
        .mapNotNull(::normalizeDanmakuUserHashManagerInput)
    val directRules = readStringArray("rules", "items", "blockRules")
        .mapNotNull(::normalizeDanmakuBlockRuleForAppend)
    return keywordRules + regexRules + userHashRules + directRules
}

private fun JsonObject.readStringArray(vararg keys: String): List<String> {
    return keys.firstNotNullOfOrNull { key ->
        when (val value = this[key]) {
            is JsonArray -> value.toRuleStrings()
            is JsonPrimitive -> value.contentOrNull?.let(::parseDanmakuBlockRules)
            else -> null
        }
    }.orEmpty()
}

private fun JsonArray.toRuleStrings(): List<String> {
    return mapNotNull { element ->
        when (element) {
            is JsonPrimitive -> element.contentOrNull?.trim()?.takeIf(String::isNotEmpty)
            is JsonObject -> element["value"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf(String::isNotEmpty)
            else -> null
        }
    }
}

private fun normalizeDanmakuRegexImportRule(rule: String): String? {
    val normalized = rule.trim()
    if (normalized.isEmpty()) return null
    return if (isDanmakuRegexRule(normalized)) normalized else "$REGEX_RULE_PREFIX$normalized"
}

fun matchesDanmakuBlockRule(content: String, rule: String, userHash: String = ""): Boolean {
    val matcher = resolveDanmakuBlockRuleMatcher(rule) ?: return false
    return matcher.matches(content, userHash)
}

fun shouldBlockDanmakuByRules(
    content: String,
    rules: List<String>,
    userHash: String = ""
): Boolean {
    if (content.isBlank() && userHash.isBlank() || rules.isEmpty()) return false
    val matchers = compileDanmakuBlockRules(rules)
    if (matchers.isEmpty()) return false
    return matchers.any { it.matches(content, userHash) }
}

internal fun shouldBlockDanmakuByMatchers(
    content: String,
    matchers: List<DanmakuBlockRuleMatcher>,
    userHash: String = ""
): Boolean {
    if ((content.isBlank() && userHash.isBlank()) || matchers.isEmpty()) return false
    return matchers.any { it.matches(content, userHash) }
}

internal fun compileDanmakuBlockRules(rules: List<String>): List<DanmakuBlockRuleMatcher> {
    return rules.asSequence()
        .mapNotNull(::resolveDanmakuBlockRuleMatcher)
        .toList()
}

fun partitionDanmakuBlockRules(rules: List<String>): DanmakuBlockRuleSections {
    val normalizedRules = rules.map(String::trim).filter(String::isNotEmpty).distinct()
    return DanmakuBlockRuleSections(
        keywordRules = normalizedRules.filter { resolveDanmakuBlockRuleGroup(it) == DanmakuBlockRuleGroup.KEYWORD },
        regexRules = normalizedRules.filter { resolveDanmakuBlockRuleGroup(it) == DanmakuBlockRuleGroup.REGEX },
        userHashRules = normalizedRules.filter { resolveDanmakuBlockRuleGroup(it) == DanmakuBlockRuleGroup.USER_HASH }
    )
}

fun mergeDanmakuBlockRuleSections(
    keywordRules: List<String>,
    regexRules: List<String>,
    userHashRules: List<String>
): List<String> {
    val normalizedKeywords = keywordRules.map(String::trim).filter(String::isNotEmpty)
    val normalizedRegexRules = regexRules.map(String::trim).filter(String::isNotEmpty)
    val normalizedUserHashRules = userHashRules.mapNotNull(::normalizeDanmakuUserHashManagerInput)
    return (normalizedKeywords + normalizedRegexRules + normalizedUserHashRules).distinct()
}

fun appendDanmakuBlockRule(
    rawRules: String,
    rule: String
): String {
    val normalizedRule = normalizeDanmakuBlockRuleForAppend(rule) ?: return parseDanmakuBlockRules(rawRules)
        .joinToString(separator = "\n")
    return (parseDanmakuBlockRules(rawRules) + normalizedRule)
        .distinct()
        .joinToString(separator = "\n")
}

fun appendDanmakuKeywordBlockRule(
    rawRules: String,
    keyword: String
): String = appendDanmakuBlockRule(rawRules = rawRules, rule = keyword)

fun appendDanmakuUserHashBlockRule(
    rawRules: String,
    userHash: String
): String {
    val normalizedUserHash = normalizeDanmakuUserHashManagerInput(userHash) ?: return parseDanmakuBlockRules(rawRules)
        .joinToString(separator = "\n")
    return appendDanmakuBlockRule(rawRules = rawRules, rule = normalizedUserHash)
}

private fun resolveDanmakuBlockRuleMatcher(rule: String): DanmakuBlockRuleMatcher? {
    val normalized = rule.trim()
    if (normalized.isEmpty()) return null

    val normalizedUserHashRule = normalizeDanmakuUserHashRule(normalized)
    if (normalizedUserHashRule != null) {
        return DanmakuUserHashMatcher(
            userHashRule = normalizedUserHashRule.substringAfter(USER_HASH_RULE_PREFIX)
        )
    }

    val regexBody = when {
        normalized.startsWith(REGEX_RULE_PREFIX, ignoreCase = true) -> {
            normalized.substring(REGEX_RULE_PREFIX.length).trim()
        }
        normalized.startsWith(SHORT_REGEX_RULE_PREFIX, ignoreCase = true) -> {
            normalized.substring(SHORT_REGEX_RULE_PREFIX.length).trim()
        }
        normalized.length >= 2 && normalized.startsWith("/") && normalized.endsWith("/") -> {
            normalized.substring(1, normalized.length - 1).trim()
        }
        else -> null
    }

    if (regexBody != null) {
        if (regexBody.isBlank()) return null
        val compiled = runCatching { Regex(regexBody, setOf(RegexOption.IGNORE_CASE)) }.getOrNull()
            ?: return null
        return DanmakuRegexMatcher(compiled)
    }

    return DanmakuKeywordMatcher(normalized)
}

private fun resolveDanmakuBlockRuleGroup(rule: String): DanmakuBlockRuleGroup {
    val normalized = rule.trim()
    return when {
        normalizeDanmakuUserHashRule(normalized) != null -> DanmakuBlockRuleGroup.USER_HASH
        isDanmakuRegexRule(normalized) -> DanmakuBlockRuleGroup.REGEX
        else -> DanmakuBlockRuleGroup.KEYWORD
    }
}

private fun normalizeDanmakuBlockRuleForAppend(rule: String): String? {
    val normalized = rule.trim()
    if (normalized.isEmpty()) return null
    return normalizeDanmakuUserHashRule(normalized) ?: normalized
}

private fun isDanmakuRegexRule(rule: String): Boolean {
    return rule.startsWith(REGEX_RULE_PREFIX, ignoreCase = true) ||
        rule.startsWith(SHORT_REGEX_RULE_PREFIX, ignoreCase = true) ||
        (rule.length >= 2 && rule.startsWith("/") && rule.endsWith("/"))
}

private fun normalizeDanmakuUserHashRule(rule: String): String? {
    val normalized = rule.trim()
    val body = when {
        normalized.startsWith(USER_HASH_RULE_PREFIX, ignoreCase = true) ->
            normalized.substring(USER_HASH_RULE_PREFIX.length)
        normalized.startsWith(USER_RULE_PREFIX, ignoreCase = true) ->
            normalized.substring(USER_RULE_PREFIX.length)
        normalized.startsWith(HASH_RULE_PREFIX, ignoreCase = true) ->
            normalized.substring(HASH_RULE_PREFIX.length)
        else -> normalized.takeIf { it.startsWith("@") }?.substring(1)
    }?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return "$USER_HASH_RULE_PREFIX$body"
}

private fun normalizeDanmakuUserHashManagerInput(rule: String): String? {
    val normalized = rule.trim()
    if (normalized.isEmpty()) return null
    return normalizeDanmakuUserHashRule(normalized) ?: "$USER_HASH_RULE_PREFIX$normalized"
}
