package com.android.purebilibili.feature.settings.share

import com.android.purebilibili.core.store.settingsshare.SettingsShareApplyResult
import com.android.purebilibili.core.store.settingsshare.SettingsShareEntryDefinition
import com.android.purebilibili.core.store.settingsshare.SettingsShareSection
import com.android.purebilibili.core.store.settingsshare.SettingsShareProfile
import com.android.purebilibili.core.store.settingsshare.SettingsShareSections
import com.android.purebilibili.core.store.settingsshare.SettingsShareImportPreview
import com.android.purebilibili.core.store.settingsshare.SettingsShareImportSession
import com.android.purebilibili.core.store.settingsshare.SettingsShareExportArtifact
import com.android.purebilibili.core.store.settingsshare.SETTINGS_SHARE_SCHEMA_VERSION
import com.android.purebilibili.core.store.settingsshare.SettingsShareDeviceDebugInfo

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SETTINGS_SHARE_FILE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.US)

/** 旧格式文件携带的遗留主题键，仅用于旧文件导入兼容。 */
internal const val SHARE_KEY_LEGACY_UI_PRESET = "ui_preset"
internal const val SHARE_KEY_LEGACY_ANDROID_NATIVE_VARIANT = "android_native_variant_v1"

/**
 * 旧文件导入兼容：把旧格式文件中的 ui_preset / android_native_variant_v1
 * 归一化为新键 theme_selection_v1（运行时仅保留 MIUIX 单值）。
 * - 新键已存在：新键优先，仅清理冗余旧键（避免旧键被误报为跳过）。
 * - 仅旧键：运行时主题恒为 MIUIX，直接写入新键。
 * - 无主题键：原样返回。
 */
internal fun normalizeThemeSelectionForImport(
    sections: SettingsShareSections
): SettingsShareSections {
    val appearance = sections.appearance
    val hasLegacyKeys = appearance.containsKey(SHARE_KEY_LEGACY_UI_PRESET) ||
        appearance.containsKey(SHARE_KEY_LEGACY_ANDROID_NATIVE_VARIANT)
    if (!hasLegacyKeys) return sections

    val newAppearance = buildMap {
        putAll(appearance)
        remove(SHARE_KEY_LEGACY_UI_PRESET)
        remove(SHARE_KEY_LEGACY_ANDROID_NATIVE_VARIANT)
    }
    if (newAppearance == appearance) return sections
    return sections.copy(appearance = newAppearance)
}

/**
 * 设备调试块的主题值：运行时仅保留 MIUIX，返回遗留 (uiPresetValue, androidNativeVariantValue)
 * 编码（MD3 + Miuix）。
 */
internal fun resolveDebugThemeValues(
    rawSettings: Map<String, JsonElement>
): Pair<Int, Int> {
    @Suppress("UNUSED_PARAMETER")
    val unusedRawSettings = rawSettings
    return 1 to 1 // MD3 + Miuix
}

internal fun flattenSettingsShareSections(
    sections: SettingsShareSections
): Map<String, JsonElement> {
    return buildMap {
        putAll(sections.appearance)
        putAll(sections.playback)
        putAll(sections.gesture)
        putAll(sections.danmaku)
        putAll(sections.navigation)
    }
}

fun buildSettingsShareProfile(
    profileName: String,
    appVersion: String,
    exportedAtIso: String,
    rawSettings: Map<String, JsonElement>,
    definitions: List<SettingsShareEntryDefinition>,
    deviceDebug: SettingsShareDeviceDebugInfo? = null,
): SettingsShareProfile {
    val byKey = definitions.associateBy { it.storageKey }
    val appearance = linkedMapOf<String, JsonElement>()
    val playback = linkedMapOf<String, JsonElement>()
    val gesture = linkedMapOf<String, JsonElement>()
    val danmaku = linkedMapOf<String, JsonElement>()
    val navigation = linkedMapOf<String, JsonElement>()

    rawSettings.forEach { (key, value) ->
        when (byKey[key]?.section) {
            SettingsShareSection.APPEARANCE -> appearance[key] = value
            SettingsShareSection.PLAYBACK -> playback[key] = value
            SettingsShareSection.GESTURE -> gesture[key] = value
            SettingsShareSection.DANMAKU -> danmaku[key] = value
            SettingsShareSection.NAVIGATION -> navigation[key] = value
            null -> Unit
        }
    }

    return SettingsShareProfile(
        appVersion = appVersion,
        exportedAtIso = exportedAtIso,
        profileName = profileName,
        sections = SettingsShareSections(
            appearance = appearance,
            playback = playback,
            gesture = gesture,
            danmaku = danmaku,
            navigation = navigation
        ),
        deviceDebug = deviceDebug,
    )
}

/**
 * 从 raw settings 与显示指标组装设备调试块。
 * [uiPresetValue]/[androidNativeVariantValue] 优先用快照中的值，便于和导出外观设置对齐。
 */
fun buildSettingsShareDeviceDebugInfo(
    androidSdkInt: Int,
    androidRelease: String,
    securityPatch: String,
    manufacturer: String,
    brand: String,
    model: String,
    device: String,
    product: String,
    hardware: String,
    displayId: String,
    widthPixels: Int,
    heightPixels: Int,
    density: Float,
    densityDpi: Int,
    scaledDensity: Float,
    xdpi: Float,
    ydpi: Float,
    widthDp: Float,
    heightDp: Float,
    smallestWidthDp: Int,
    fontScale: Float,
    uiModeNight: Boolean,
    uiPresetValue: Int,
    uiPresetName: String,
    androidNativeVariantValue: Int,
    androidNativeVariantName: String,
    appVersionName: String,
    appVersionCode: Long,
): SettingsShareDeviceDebugInfo {
    return SettingsShareDeviceDebugInfo(
        androidSdkInt = androidSdkInt,
        androidRelease = androidRelease,
        securityPatch = securityPatch,
        manufacturer = manufacturer,
        brand = brand,
        model = model,
        device = device,
        product = product,
        hardware = hardware,
        displayId = displayId,
        widthPixels = widthPixels,
        heightPixels = heightPixels,
        density = density,
        densityDpi = densityDpi,
        scaledDensity = scaledDensity,
        xdpi = xdpi,
        ydpi = ydpi,
        widthDp = widthDp,
        heightDp = heightDp,
        smallestWidthDp = smallestWidthDp,
        fontScale = fontScale,
        uiModeNight = uiModeNight,
        uiPresetValue = uiPresetValue,
        uiPresetName = uiPresetName,
        androidNativeVariantValue = androidNativeVariantValue,
        androidNativeVariantName = androidNativeVariantName,
        appVersionName = appVersionName,
        appVersionCode = appVersionCode,
    )
}

internal fun resolveUiPresetNameFromValue(value: Int): String {
    return when (value) {
        0 -> "iOS"
        1 -> "安卓原生"
        else -> "unknown($value)"
    }
}

internal fun resolveAndroidNativeVariantNameFromValue(value: Int): String {
    return when (value) {
        0 -> "Material 3"
        1 -> "Miuix"
        2 -> "Material 3" // legacy removed
        else -> "unknown($value)"
    }
}

internal fun jsonElementAsInt(element: JsonElement?): Int? {
    val primitive = element as? kotlinx.serialization.json.JsonPrimitive ?: return null
    return primitive.content.toIntOrNull()
}

fun resolveSettingsShareImportPreview(
    profile: SettingsShareProfile,
    definitions: List<SettingsShareEntryDefinition>
): SettingsShareImportPreview {
    val importableKeys = definitions.mapTo(linkedSetOf()) { it.storageKey }
    val allKeys = flattenSettingsShareSections(profile.sections).keys
    val importableSections = definitions
        .filter { definition -> allKeys.contains(definition.storageKey) }
        .map { it.section }
        .distinct()
    val skippedKeys = allKeys
        .filterNot { importableKeys.contains(it) }
        .sorted()

    return SettingsShareImportPreview(
        profileName = profile.profileName,
        importableSections = importableSections,
        skippedKeys = skippedKeys
    )
}

fun buildSettingsShareFileName(
    appVersion: String,
    epochMs: Long
): String {
    val timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneOffset.UTC)
    return "bilipai-settings-$appVersion-${timestamp.format(SETTINGS_SHARE_FILE_TIME_FORMATTER)}.json"
}
