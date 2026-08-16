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

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import androidx.core.content.FileProvider
import com.android.purebilibili.BuildConfig
import com.android.purebilibili.core.store.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant

internal const val DEFAULT_SETTINGS_SHARE_PROFILE_NAME = "BiliPai 设置分享"

interface SettingsShareServiceContract {
    suspend fun exportToUri(
        uri: Uri,
        profileName: String = DEFAULT_SETTINGS_SHARE_PROFILE_NAME,
        includeDeviceDebug: Boolean = true,
    ): Result<SettingsShareExportArtifact>

    suspend fun createShareUri(
        profileName: String = DEFAULT_SETTINGS_SHARE_PROFILE_NAME,
        includeDeviceDebug: Boolean = true,
    ): Result<Uri>

    suspend fun readImportSession(uri: Uri): Result<SettingsShareImportSession>

    suspend fun applyImport(session: SettingsShareImportSession): Result<SettingsShareApplyResult>
}

class SettingsShareService(private val context: Context) : SettingsShareServiceContract {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun createExportArtifact(
        profileName: String = DEFAULT_SETTINGS_SHARE_PROFILE_NAME,
        includeDeviceDebug: Boolean = true,
    ): SettingsShareExportArtifact = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val rawSettings = SettingsManager.exportShareableSettingsSnapshot(context)
        val definitions = SettingsManager.getShareableSettingsEntryDefinitions()
        val deviceDebug = if (includeDeviceDebug) {
            captureDeviceDebugInfo(context, rawSettings)
        } else {
            null
        }
        val profile = buildSettingsShareProfile(
            profileName = profileName,
            appVersion = BuildConfig.VERSION_NAME,
            exportedAtIso = Instant.ofEpochMilli(now).toString(),
            rawSettings = rawSettings,
            definitions = definitions,
            deviceDebug = deviceDebug,
        )
        SettingsShareExportArtifact(
            fileName = buildSettingsShareFileName(
                appVersion = BuildConfig.VERSION_NAME,
                epochMs = now
            ),
            json = json.encodeToString(SettingsShareProfile.serializer(), profile),
            profile = profile
        )
    }

    override suspend fun exportToUri(
        uri: Uri,
        profileName: String,
        includeDeviceDebug: Boolean,
    ): Result<SettingsShareExportArtifact> = withContext(Dispatchers.IO) {
        runCatching {
            val artifact = createExportArtifact(
                profileName = profileName,
                includeDeviceDebug = includeDeviceDebug,
            )
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(artifact.json.toByteArray(Charsets.UTF_8))
            } ?: error("无法写入导出文件")
            artifact
        }
    }

    override suspend fun createShareUri(
        profileName: String,
        includeDeviceDebug: Boolean,
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val artifact = createExportArtifact(
                profileName = profileName,
                includeDeviceDebug = includeDeviceDebug,
            )
            val shareDir = File(context.cacheDir, "logs/settings-share").apply { mkdirs() }
            val shareFile = File(shareDir, artifact.fileName)
            shareFile.writeText(artifact.json, Charsets.UTF_8)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                shareFile
            )
        }
    }

    private fun captureDeviceDebugInfo(
        context: Context,
        rawSettings: Map<String, kotlinx.serialization.json.JsonElement>,
    ): SettingsShareDeviceDebugInfo {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        val config: Configuration = context.resources.configuration
        val (uiPresetValue, nativeVariantValue) = resolveDebugThemeValues(rawSettings)
        val nightMask = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return buildSettingsShareDeviceDebugInfo(
            androidSdkInt = Build.VERSION.SDK_INT,
            androidRelease = Build.VERSION.RELEASE.orEmpty(),
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH.orEmpty()
            } else {
                ""
            },
            manufacturer = Build.MANUFACTURER.orEmpty(),
            brand = Build.BRAND.orEmpty(),
            model = Build.MODEL.orEmpty(),
            device = Build.DEVICE.orEmpty(),
            product = Build.PRODUCT.orEmpty(),
            hardware = Build.HARDWARE.orEmpty(),
            displayId = Build.DISPLAY.orEmpty(),
            widthPixels = metrics.widthPixels,
            heightPixels = metrics.heightPixels,
            density = metrics.density,
            densityDpi = metrics.densityDpi,
            scaledDensity = metrics.scaledDensity,
            xdpi = metrics.xdpi,
            ydpi = metrics.ydpi,
            widthDp = config.screenWidthDp.toFloat(),
            heightDp = config.screenHeightDp.toFloat(),
            smallestWidthDp = config.smallestScreenWidthDp,
            fontScale = config.fontScale,
            uiModeNight = nightMask == Configuration.UI_MODE_NIGHT_YES,
            uiPresetValue = uiPresetValue,
            uiPresetName = resolveUiPresetNameFromValue(uiPresetValue),
            androidNativeVariantValue = nativeVariantValue,
            androidNativeVariantName = resolveAndroidNativeVariantNameFromValue(nativeVariantValue),
            appVersionName = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE.toLong(),
        )
    }

    override suspend fun readImportSession(uri: Uri): Result<SettingsShareImportSession> =
        withContext(Dispatchers.IO) {
            runCatching {
                val rawJson = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                } ?: error("无法读取导入文件")
                val profile = decodeProfile(rawJson)
                // 旧格式文件在导入边界归一化为新键 theme_selection_v1，
                // 预览与回写都基于归一化后的 profile（旧键仅用于旧文件导入兼容）。
                val normalizedProfile = normalizeThemeSelectionForImport(profile.sections)
                    .let { sections ->
                        if (sections == profile.sections) profile else profile.copy(sections = sections)
                    }
                val preview = resolveSettingsShareImportPreview(
                    profile = normalizedProfile,
                    definitions = SettingsManager.getShareableSettingsEntryDefinitions()
                )
                SettingsShareImportSession(
                    profile = normalizedProfile,
                    preview = preview,
                    rawJson = rawJson
                )
            }
        }

    override suspend fun applyImport(session: SettingsShareImportSession): Result<SettingsShareApplyResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val settings = flattenSettingsShareSections(session.profile.sections)
                SettingsManager.applyShareableSettingsSnapshot(
                    context = context,
                    settings = settings
                )
            }
        }

    private fun decodeProfile(rawJson: String): SettingsShareProfile {
        return json.decodeFromString(SettingsShareProfile.serializer(), rawJson)
    }
}
