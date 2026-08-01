package com.android.purebilibili.feature.plugin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.android.purebilibili.core.ui.components.AppFilterChip
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.core.plugin.Plugin
import com.android.purebilibili.core.plugin.PluginCapabilityManifest
import com.android.purebilibili.core.plugin.PluginManager
import com.android.purebilibili.core.plugin.PluginStore
import com.android.purebilibili.core.util.Logger
import com.android.purebilibili.feature.anime4k.Anime4KConfig
import com.android.purebilibili.feature.anime4k.Anime4KPreset
import com.android.purebilibili.feature.anime4k.resolveAnime4KPresetLabel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val TAG = "Anime4KPlugin"

/**
 * Anime4K 是内置插件：插件层负责配置和可见性，播放器输出仍由 VideoOutputRouter 管理。
 */
class Anime4KPlugin : Plugin {

    override val id: String = PLUGIN_ID
    override val name: String = "Anime4K 超分辨率"
    override val description: String = "使用参考Kazumi的Anime4K CNN链实时重建动漫画面"
    override val version: String = "0.2.3"
    override val author: String = "BiliPai项目组"
    override val capabilityManifest: PluginCapabilityManifest = PluginCapabilityManifest(
        pluginId = id,
        displayName = name,
        version = version,
        apiVersion = 1,
        entryClassName = Anime4KPlugin::class.java.name,
        capabilities = emptySet()
    )

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var config = Anime4KConfig()
    private val _configState = MutableStateFlow(config)
    val configState: StateFlow<Anime4KConfig> = _configState.asStateFlow()

    override suspend fun onEnable() {
        loadConfig()
        Logger.d(TAG, "Anime4K 插件已启用")
    }

    fun setPreset(preset: Anime4KPreset) {
        if (config.preset == preset) return
        config = config.copy(preset = preset)
        _configState.value = config
        ioScope.launch {
            runCatching {
                PluginStore.setConfigJson(
                    context = PluginManager.getContext(),
                    pluginId = id,
                    configJson = Json.encodeToString(config)
                )
            }.onFailure { error ->
                Logger.e(TAG, "保存 Anime4K 配置失败", error)
            }
        }
    }

    private suspend fun loadConfig() {
        config = runCatching {
            val raw = PluginStore.getConfigJson(PluginManager.getContext(), id)
            if (raw.isNullOrBlank()) {
                Anime4KConfig()
            } else {
                val storedPreset = Json.parseToJsonElement(raw)
                    .jsonObject["preset"]
                    ?.jsonPrimitive
                    ?.content
                if (storedPreset == "BALANCED") {
                    // 2.0.0 的中间档与 Kazumi 效率链相同，升级后归并到效率档。
                    Anime4KConfig(preset = Anime4KPreset.FAST)
                } else {
                    Json.decodeFromString<Anime4KConfig>(raw)
                }
            }
        }.onFailure { error ->
            Logger.e(TAG, "读取 Anime4K 配置失败", error)
        }.getOrDefault(Anime4KConfig())
        _configState.value = config
    }

    @Composable
    override fun SettingsContent() {
        val configSnapshot by configState.collectAsStateWithLifecycle()
        val options = remember {
            listOf(
                Anime4KPreset.FAST,
                Anime4KPreset.QUALITY
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppText("CNN 模型", style = MaterialTheme.typography.titleSmall)
            AppText(
                text = "HDR、杜比视界、小窗和后台播放会自动使用原始视频输出。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            options.forEach { preset ->
                AppFilterChip(
                    selected = configSnapshot.preset == preset,
                    onClick = { setPreset(preset) },
                    label = { AppText(resolveAnime4KPresetLabel(preset)) }
                )
            }
        }
    }

    companion object {
        const val PLUGIN_ID: String = "anime4k"

        fun getInstance(): Anime4KPlugin? {
            return PluginManager.plugins
                .firstOrNull { it.plugin.id == PLUGIN_ID }
                ?.plugin as? Anime4KPlugin
        }
    }
}
