// 文件路径: feature/video/ui/components/VideoSettingsPanel.kt
package com.android.purebilibili.feature.video.ui.components
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppHorizontalDivider

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoCamera
import com.android.purebilibili.core.store.LONG_PRESS_SPEED_OPTIONS
import com.android.purebilibili.core.ui.AppModalBottomSheet
import com.android.purebilibili.core.ui.rememberAppPlayerChromeProfile
import com.android.purebilibili.core.ui.rememberAppCodecIcon
import com.android.purebilibili.core.ui.rememberAppDownloadIcon
import com.android.purebilibili.core.ui.rememberAppFlipHorizontalIcon
import com.android.purebilibili.core.ui.rememberAppFlipVerticalIcon
import com.android.purebilibili.core.ui.rememberAppGestureTapIcon
import com.android.purebilibili.core.ui.rememberAppSettingsIcon
import com.android.purebilibili.core.ui.rememberAppMusicIcon
import com.android.purebilibili.core.ui.rememberAppPhotoIcon
import com.android.purebilibili.core.ui.rememberAppQualityIcon
import com.android.purebilibili.core.ui.rememberAppRefreshIcon
import com.android.purebilibili.core.ui.rememberAppSpeedIcon
import com.android.purebilibili.core.ui.rememberAppTimerIcon
import com.android.purebilibili.core.ui.rememberAppWifiIcon
import com.android.purebilibili.core.ui.components.DefaultPlaybackSpeedPreferenceControl
import com.android.purebilibili.core.ui.components.AppButton
import com.android.purebilibili.core.ui.components.AppPreference
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppSwitchPreference
import com.android.purebilibili.core.ui.components.formatDefaultPlaybackSpeed
import com.android.purebilibili.data.model.response.AiAudioInfo
import com.android.purebilibili.feature.plugin.CdnLineDiagnostic
import com.android.purebilibili.feature.video.playback.audio.AudioQualityOption
import com.android.purebilibili.feature.video.screen.VideoPlayerSettingsActions
import com.android.purebilibili.feature.video.screen.VideoPlayerSettingsSnapshot
import com.android.purebilibili.feature.video.screen.resolveDefaultVideoPlayerSettingsSnapshot
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

private data class VideoSettingsPanelVisualSpec(
    val rowHorizontalPadding: androidx.compose.ui.unit.Dp,
    val rowVerticalPadding: androidx.compose.ui.unit.Dp,
    val rowMinHeight: androidx.compose.ui.unit.Dp,
    val iconSize: androidx.compose.ui.unit.Dp,
    val iconGap: androidx.compose.ui.unit.Dp,
    val dividerHorizontalPadding: androidx.compose.ui.unit.Dp,
    val dividerAlpha: Float,
    val chipHeight: androidx.compose.ui.unit.Dp,
    val chipCornerRadius: androidx.compose.ui.unit.Dp,
    val chipHorizontalPadding: androidx.compose.ui.unit.Dp,
    val chipSpacing: androidx.compose.ui.unit.Dp
)

private fun resolveVideoSettingsPanelVisualSpec(
    usesTonalContainerTreatment: Boolean,
): VideoSettingsPanelVisualSpec {
    return if (usesTonalContainerTreatment) {
        VideoSettingsPanelVisualSpec(
            rowHorizontalPadding = 16.dp,
            rowVerticalPadding = 12.dp,
            rowMinHeight = 56.dp,
            iconSize = 20.dp,
            iconGap = 12.dp,
            dividerHorizontalPadding = 16.dp,
            dividerAlpha = 0.18f,
            chipHeight = 34.dp,
            chipCornerRadius = 17.dp,
            chipHorizontalPadding = 13.dp,
            chipSpacing = 7.dp
        )
    } else {
        VideoSettingsPanelVisualSpec(
            rowHorizontalPadding = 16.dp,
            rowVerticalPadding = 14.dp,
            rowMinHeight = 52.dp,
            iconSize = 24.dp,
            iconGap = 16.dp,
            dividerHorizontalPadding = 16.dp,
            dividerAlpha = 0.5f,
            chipHeight = 32.dp,
            chipCornerRadius = 16.dp,
            chipHorizontalPadding = 12.dp,
            chipSpacing = 8.dp
        )
    }
}

@Composable
private fun rememberVideoSettingsPanelVisualSpec(): VideoSettingsPanelVisualSpec {
    val usesTonalContainerTreatment = rememberAppPlayerChromeProfile()
        .effects
        .usesTonalContainerTreatment
    return remember(usesTonalContainerTreatment) {
        resolveVideoSettingsPanelVisualSpec(usesTonalContainerTreatment)
    }
}

@Composable
private fun videoSettingsChipContainerColor(isSelected: Boolean): Color {
    val usesTonalContainerTreatment = rememberAppPlayerChromeProfile()
        .effects
        .usesTonalContainerTreatment
    return if (usesTonalContainerTreatment) {
        if (isSelected) AppSurfaceTokens.secondaryContainer() else AppSurfaceTokens.surfaceContainerHigh()
    } else {
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
private fun videoSettingsChipContentColor(isSelected: Boolean): Color {
    val usesTonalContainerTreatment = rememberAppPlayerChromeProfile()
        .effects
        .usesTonalContainerTreatment
    return if (usesTonalContainerTreatment) {
        if (isSelected) AppSurfaceTokens.onSecondaryContainer() else AppSurfaceTokens.onSurfaceVariantSummary()
    } else {
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 *  视频设置面板 - 竖屏模式下的高级设置底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoSettingsPanel(
    // 定时关闭
    sleepTimerMinutes: Int?,
    onSleepTimerChange: (Int?) -> Unit,
    
    // 视频控制
    onReload: () -> Unit,
    
    // 画质 - 内联选择
    currentQualityLabel: String,
    qualityLabels: List<String> = emptyList(),
    qualityIds: List<Int> = emptyList(),
    switchableQualityIds: List<Int> = emptyList(),
    isLoggedIn: Boolean = false,
    isVip: Boolean = false,
    onQualitySelected: (Int) -> Unit = {},
    
    // 倍速
    currentSpeed: Float = 1.0f,
    onSpeedChange: (Float) -> Unit = {},
    
    // 镜像翻转
    isFlippedHorizontal: Boolean = false,
    isFlippedVertical: Boolean = false,
    onFlipHorizontal: () -> Unit = {},
    onFlipVertical: () -> Unit = {},
    
    //  CDN 线路切换
    currentCdnIndex: Int = 0,
    cdnCount: Int = 1,
    cdnLineDiagnostics: List<CdnLineDiagnostic> = emptyList(),
    isCdnProbing: Boolean = false,
    onSwitchCdn: () -> Unit = {},
    onSwitchCdnTo: (Int) -> Unit = {},
    onProbeCdnCandidates: () -> Unit = {},

    // [New] Codec & Audio Quality
    // Passed from player settings contract
    currentCodec: String = "hev1", 
    onCodecChange: (String) -> Unit = {},
    currentSecondCodec: String = "avc1",
    onSecondCodecChange: (String) -> Unit = {},
    currentAudioQuality: Int = -1,
    availableAudioQualities: List<AudioQualityOption> = emptyList(),
    onAudioQualityChange: (Int) -> Unit = {},
    // [New] 音频语言 (AI Translation)
    aiAudioInfo: AiAudioInfo? = null,
    currentAudioLang: String? = null,
    onAudioLangChange: (String) -> Unit = {},
    
    // 资源下载
    onSaveCover: () -> Unit = {},
    onCaptureScreenshot: () -> Unit = {},
    onDownloadAudio: () -> Unit = {},
    
    // 关闭面板
    onDismiss: () -> Unit,
    settings: VideoPlayerSettingsSnapshot = resolveDefaultVideoPlayerSettingsSnapshot(),
    settingsActions: VideoPlayerSettingsActions = VideoPlayerSettingsActions.NoOp
) {
    fun hasPermissionForQuality(qualityId: Int): Boolean {
        return when {
            qualityId >= 112 -> isVip
            qualityId >= 80 -> isLoggedIn
            else -> true
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val panelSpec = rememberVideoSettingsPanelVisualSpec()
    val usesTonalContainerTreatment = rememberAppPlayerChromeProfile()
        .effects
        .usesTonalContainerTreatment
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val actionPolicy = remember(configuration.screenWidthDp) {
        resolveVideoSettingsPanelActionPolicy(widthDp = configuration.screenWidthDp)
    }
    val playerInteractionSettings = settings.playerInteractionSettings
    val seekForwardSeconds = playerInteractionSettings.seekForwardSeconds
    val seekBackwardSeconds = playerInteractionSettings.seekBackwardSeconds
    val longPressSpeed = playerInteractionSettings.longPressSpeed
    val longPressSpeedLockEnabled = playerInteractionSettings.longPressSpeedLockEnabled
    val twoFingerVerticalSpeedEnabled = playerInteractionSettings.twoFingerVerticalSpeedEnabled
    val twoFingerHorizontalSpeedEnabled = playerInteractionSettings.twoFingerHorizontalSpeedEnabled
    val defaultPlaybackSpeed = settings.defaultPlaybackSpeed
    val rememberLastPlaybackSpeed = settings.rememberLastPlaybackSpeed
    val timerIcon = rememberAppTimerIcon()
    val refreshIcon = rememberAppRefreshIcon()
    val photoIcon = rememberAppPhotoIcon()
    val downloadIcon = rememberAppDownloadIcon()
    val musicIcon = rememberAppMusicIcon()
    val flipHorizontalIcon = rememberAppFlipHorizontalIcon()
    val flipVerticalIcon = rememberAppFlipVerticalIcon()
    val qualityIcon = rememberAppQualityIcon()
    val codecIcon = rememberAppCodecIcon()
    val wifiIcon = rememberAppWifiIcon()
    val speedIcon = rememberAppSpeedIcon()
    val gestureTapIcon = rememberAppGestureTapIcon()
    val settingsIcon = rememberAppSettingsIcon()
    
    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(
                if (usesTonalContainerTreatment) 4.dp else 8.dp
            )
        ) {
            //  定时关闭 - 垂直布局，选项在下一行
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            imageVector = timerIcon,
                            contentDescription = null,
                            tint = if (usesTonalContainerTreatment) {
                                AppSurfaceTokens.onSurfaceVariantActions()
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(panelSpec.iconSize)
                        )
                        Spacer(modifier = Modifier.width(panelSpec.iconGap))
                        AppText(
                            text = "定时关闭",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // 定时选项按钮组 - 支持横向滚动
                    SleepTimerOptions(
                        currentMinutes = sleepTimerMinutes,
                        onSelect = onSleepTimerChange
                    )
                }
                SettingsDivider()
            }

            item {
                //  重载视频
                SettingsItem(
                    icon = refreshIcon,
                    title = "重载视频",
                    onClick = {
                        onReload()
                        onDismiss()
                    }
                )
                SettingsDivider()
            }

            // [New] 资源下载
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            imageVector = downloadIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        AppText(
                            text = "资源下载",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(actionPolicy.rowItemSpacingDp.dp)
                    ) {
                        SettingsActionPill(
                            icon = photoIcon,
                            label = "保存封面",
                            onClick = {
                                onSaveCover()
                                onDismiss()
                            },
                            policy = actionPolicy
                        )
                        SettingsActionPill(
                            icon = Icons.Filled.PhotoCamera,
                            label = "视频截图",
                            onClick = {
                                onCaptureScreenshot()
                                onDismiss()
                            },
                            policy = actionPolicy
                        )
                        SettingsActionPill(
                            icon = musicIcon,
                            label = "下载音频",
                            onClick = {
                                onDownloadAudio()
                                onDismiss()
                            },
                            policy = actionPolicy
                        )
                    }
                }
                SettingsDivider()
            }

            item {
                //  镜像翻转按钮组
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(actionPolicy.rowItemSpacingDp.dp)
                ) {
                    // 左右翻转
                    FlipButton(
                        icon = flipHorizontalIcon,
                        label = "左右翻转",
                        isActive = isFlippedHorizontal,
                        onClick = {
                            onFlipHorizontal()
                            onDismiss()
                        },
                        policy = actionPolicy
                    )
                    
                    // 上下翻转
                    FlipButton(
                        icon = flipVerticalIcon,
                        label = "上下翻转",
                        isActive = isFlippedVertical,
                        onClick = {
                            onFlipVertical()
                            onDismiss()
                        },
                        policy = actionPolicy
                    )
                }
                SettingsDivider()
            }

            //  选择画质 - 内联选择
            if (qualityLabels.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(
                                imageVector = qualityIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            AppText(
                                text = "选择画质",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AppText(
                                text = "当前 $currentQualityLabel",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // 画质选项
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(panelSpec.chipSpacing)
                ) {
                            qualityLabels.forEachIndexed { index, label ->
                                val isSelected = label == currentQualityLabel
                                val qualityId = qualityIds.getOrNull(index) ?: 0
                                val isSwitchable = qualityId in switchableQualityIds
                                val hasPermission = hasPermissionForQuality(qualityId)
                                val isEnabled = isSwitchable && hasPermission
                                val containerColor = when {
                                    isSelected -> videoSettingsChipContainerColor(true)
                                    isEnabled -> videoSettingsChipContainerColor(false)
                                    else -> videoSettingsChipContainerColor(false).copy(alpha = 0.55f)
                                }
                                val contentColor = when {
                                    isSelected -> videoSettingsChipContentColor(true)
                                    isEnabled -> videoSettingsChipContentColor(false)
                                    else -> videoSettingsChipContentColor(false).copy(alpha = 0.45f)
                                }

                                AppSurface(
                                    onClick = {
                                        if (!isSelected && isEnabled) {
                                            onQualitySelected(index)
                                        }
                                    },
                                    enabled = isSelected || isEnabled,
                                    shape = RoundedCornerShape(panelSpec.chipCornerRadius),
                                    color = containerColor,
                                    modifier = Modifier.height(panelSpec.chipHeight)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = panelSpec.chipHorizontalPadding)
                                    ) {
                                        AppText(
                                            text = label,
                                            fontSize = 13.sp,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                    SettingsDivider()
                }
            }
            
            // [New] 编码格式选择
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            imageVector = codecIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        AppText(
                            text = "编码格式",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val codecLabel = when(currentCodec) {
                            "avc1" -> "AVC (兼容)"
                            "hev1" -> "HEVC (推荐)"
                            "av01" -> "AV1 (极致)"
                            else -> "未知"
                        }
                        AppText(
                            text = codecLabel,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val codecs = listOf("avc1" to "AVC (H.264)", "hev1" to "HEVC (H.265)", "av01" to "AV1")
                        codecs.forEach { (codec, label) ->
                            val isSelected = currentCodec == codec
                            AppSurface(
                                onClick = { onCodecChange(codec) },
                                shape = AppShapes.container(ContainerLevel.Card),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.height(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    AppText(
                                        text = label,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                SettingsDivider()
            }

            // [New] 次选编码格式选择
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            imageVector = codecIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        AppText(
                            text = "次选编码",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        val secondCodecLabel = when(currentSecondCodec) {
                            "avc1" -> "AVC (兼容)"
                            "hev1" -> "HEVC (高效)"
                            "av01" -> "AV1 (高压缩)"
                            else -> "未知"
                        }
                        AppText(
                            text = secondCodecLabel,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val codecs = listOf("avc1" to "AVC (H.264)", "hev1" to "HEVC (H.265)", "av01" to "AV1")
                        codecs.forEach { (codec, label) ->
                            val isSelected = currentSecondCodec == codec
                            AppSurface(
                                onClick = { onSecondCodecChange(codec) },
                                shape = AppShapes.container(ContainerLevel.Card),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.height(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    AppText(
                                        text = label,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                SettingsDivider()
            }

            // 音质入口始终保留，具体选项仍以当前播放资源真实返回的数据为准。
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            imageVector = musicIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        AppText(
                            text = "音频音质",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val audioLabel = availableAudioQualities
                            .firstOrNull { it.preferenceId == currentAudioQuality }
                            ?.label
                            ?: "自动"
                        AppText(
                            text = audioLabel,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableAudioQualities.forEach { option ->
                            val isSelected = currentAudioQuality == option.preferenceId
                            AppSurface(
                                onClick = { onAudioQualityChange(option.preferenceId) },
                                shape = AppShapes.container(ContainerLevel.Card),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                ) {
                                    AppText(
                                        text = option.label,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (option.isHiRes) {
                                        HiResBadge()
                                    }
                                    if (option.isDolby) {
                                        DolbyBadge()
                                    }
                                }
                            }
                        }
                    }
                }
                SettingsDivider()
            }

             // [New] 音频语言选择 (AI Translation)
            if (aiAudioInfo?.items?.isNotEmpty() == true) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppText(
                                text = "AI原生翻译",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            val currentLangItem = aiAudioInfo.items.find { it.langCode == currentAudioLang }
                            val langLabel = currentLangItem?.langDoc ?: "原声"
                            
                            AppText(
                                text = langLabel,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            aiAudioInfo.items.forEach { item ->
                                val isSelected = currentAudioLang == item.langCode
                                AppSurface(
                                    onClick = { onAudioLangChange(item.langCode) },
                                    shape = AppShapes.container(ContainerLevel.Card),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                        AppText(
                                            text = item.langDoc,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    SettingsDivider()
                }
            }

            //  播放线路 (CDN) - 仅在有多个线路时显示
            if (cdnCount > 1) {
                item {
                    val diagnosticsByIndex = cdnLineDiagnostics.associateBy { it.index }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(
                                imageVector = wifiIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            AppText(
                                text = "播放线路",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AppText(
                                text = "当前 线路${currentCdnIndex + 1}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        AppButton(
                            enabled = !isCdnProbing,
                            onClick = onProbeCdnCandidates,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AppText(if (isCdnProbing) "检测中..." else "检测当前候选线路")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(cdnCount) { index ->
                                val diagnostic = diagnosticsByIndex[index]
                                CdnLineRow(
                                    index = index,
                                    isSelected = index == currentCdnIndex,
                                    diagnostic = diagnostic,
                                    onClick = {
                                        if (index != currentCdnIndex) onSwitchCdnTo(index)
                                    }
                                )
                            }
                        }
                    }
                    SettingsDivider()
                }
            }

            //  播放倍速 - 内联选择
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            imageVector = speedIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        AppText(
                            text = "播放倍速",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AppText(
                            text = if (currentSpeed == 1.0f) "正常" else "${currentSpeed}x",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SpeedOptions(
                        currentSpeed = currentSpeed,
                        onSelect = onSpeedChange
                    )
                }
                SettingsDivider()
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    VideoSettingsSwitchRow(
                        icon = settingsIcon,
                        title = "默认播放速度",
                        subtitle = if (rememberLastPlaybackSpeed) {
                            "已开启记忆上次速度（当前优先）"
                        } else {
                            "当前默认 ${formatDefaultPlaybackSpeed(defaultPlaybackSpeed)}"
                        },
                        checked = rememberLastPlaybackSpeed,
                        onCheckedChange = { checked ->
                            settingsActions.setRememberLastPlaybackSpeed(checked)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DefaultPlaybackSpeedPreferenceControl(
                        currentSpeed = defaultPlaybackSpeed,
                        onSpeedChange = { speed ->
                            settingsActions.setDefaultPlaybackSpeed(speed)
                        },
                        title = null,
                        subtitle = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                SettingsDivider()
            }

            //  [新增] 双击跳转秒数设置 (带开关)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val doubleTapSeekEnabled = playerInteractionSettings.doubleTapSeekEnabled

                    VideoSettingsSwitchRow(
                        icon = speedIcon,
                        title = "双击跳转",
                        subtitle = if (doubleTapSeekEnabled) {
                            "快进 ${seekForwardSeconds}s / 后退 ${seekBackwardSeconds}s"
                        } else {
                            "已关闭"
                        },
                        checked = doubleTapSeekEnabled,
                        onCheckedChange = { checked ->
                            settingsActions.setDoubleTapSeekEnabled(checked)
                        }
                    )

                    // 仅当开启时显示秒数选项
                    AnimatedVisibility(
                        visible = doubleTapSeekEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 快进秒数选择
                            AppText(
                                text = "快进秒数（双击右侧）",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            SeekSecondsOptions(
                                currentSeconds = seekForwardSeconds,
                                onSelect = { seconds ->
                                    settingsActions.setSeekForwardSeconds(seconds)
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 后退秒数选择
                            AppText(
                                text = "后退秒数（双击左侧）",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            SeekSecondsOptions(
                                currentSeconds = seekBackwardSeconds,
                                onSelect = { seconds ->
                                    settingsActions.setSeekBackwardSeconds(seconds)
                                }
                            )
                        }
                    }
                }
                SettingsDivider()
            }

            //  [新增] 长按倍速设置
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp) // 优化：减少垂直间距
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            imageVector = gestureTapIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            AppText(
                                text = "长按倍速",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            AppText(
                                text = "当前 ${longPressSpeed}x",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 长按倍速选项
                    LongPressSpeedOptions(
                        currentSpeed = longPressSpeed,
                        onSelect = { speed ->
                            settingsActions.setLongPressSpeed(speed)
                        }
                    )
                }
                SettingsDivider()
            }

            item {
                VideoSettingsSwitchRow(
                    icon = gestureTapIcon,
                    title = "长按倍速锁定",
                    subtitle = "长按后拖至上下区域保持倍速",
                    checked = longPressSpeedLockEnabled,
                    onCheckedChange = { checked ->
                        settingsActions.setLongPressSpeedLockEnabled(checked)
                        if (checked) {
                            settingsActions.setLongPressSpeedLockHintShown(true)
                        }
                    }
                )
                SettingsDivider()
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    VideoSettingsSwitchRow(
                        icon = flipVerticalIcon,
                        title = "双指上下滑动调倍速",
                        subtitle = "仅全屏生效，开启一项时会关闭另一项",
                        checked = twoFingerVerticalSpeedEnabled,
                        onCheckedChange = { checked ->
                            settingsActions.setTwoFingerVerticalSpeedEnabled(checked)
                        }
                    )

                    VideoSettingsSwitchRow(
                        icon = flipHorizontalIcon,
                        title = "双指左右滑动调倍速",
                        subtitle = "仅全屏生效，开启一项时会关闭另一项",
                        checked = twoFingerHorizontalSpeedEnabled,
                        onCheckedChange = { checked ->
                            settingsActions.setTwoFingerHorizontalSpeedEnabled(checked)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 设置项组件
 */
@Composable
private fun VideoSettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    AppSwitchPreference(
        icon = icon,
        title = title,
        subtitle = subtitle,
        checked = checked,
        onCheckedChange = onCheckedChange,
        iconTint = AppSurfaceTokens.onSurfaceVariantActions(),
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    AppPreference(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        iconTint = AppSurfaceTokens.onSurfaceVariantActions(),
        showChevron = trailing == null,
        trailingContent = trailing,
    )
}

@Composable
private fun SettingsDivider() {
    val spec = rememberVideoSettingsPanelVisualSpec()
    AppHorizontalDivider(
        modifier = Modifier.padding(horizontal = spec.dividerHorizontalPadding),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = spec.dividerAlpha)
    )
}

@Composable
private fun CdnLineRow(
    index: Int,
    isSelected: Boolean,
    diagnostic: CdnLineDiagnostic?,
    onClick: () -> Unit
) {
    val status = listOfNotNull(diagnostic?.sourceLabel, diagnostic?.statusLabel ?: "未检测")
        .joinToString(" · ")
    val host = diagnostic?.host ?: "线路${index + 1}"
    val metric = buildString {
        diagnostic?.latencyMs?.let { append("${it}ms") }
        diagnostic?.speedKbps?.let {
            if (isNotEmpty()) append(" · ")
            append("${it / 1024}Mbps")
        }
        if ((diagnostic?.errorCount ?: 0) > 0) {
            if (isNotEmpty()) append(" · ")
            append("失败${diagnostic?.errorCount}")
        }
        if ((diagnostic?.bufferingCount ?: 0) > 0) {
            if (isNotEmpty()) append(" · ")
            append("缓冲${diagnostic?.bufferingCount}")
        }
    }.ifBlank { "手动检测后显示延迟/速度" }

    AppSurface(
        onClick = onClick,
        shape = AppShapes.container(ContainerLevel.Chip),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = "线路${index + 1} · $status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AppText(
                    text = host,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AppText(
                    text = metric,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isSelected) {
                AppText(
                    text = "当前",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 定时关闭选项
 */
@Composable
private fun SleepTimerOptions(
    currentMinutes: Int?,
    onSelect: (Int?) -> Unit
) {
    val spec = rememberVideoSettingsPanelVisualSpec()
    val options = listOf(
        null to "关闭",
        15 to "15分钟",
        30 to "30分钟",
        60 to "1小时",
        90 to "1.5小时"
    )
    
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spec.chipSpacing)
    ) {
        options.forEach { (minutes, label) ->
            val isSelected = currentMinutes == minutes
            AppSurface(
                onClick = { onSelect(minutes) },
                shape = RoundedCornerShape(spec.chipCornerRadius),
                color = videoSettingsChipContainerColor(isSelected),
                modifier = Modifier.height(spec.chipHeight)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = spec.chipHorizontalPadding)
                ) {
                    AppText(
                        text = label,
                        fontSize = 13.sp,
                        color = videoSettingsChipContentColor(isSelected)
                    )
                }
            }
        }
    }
}

/**
 * 播放倍速选项
 */
@Composable
private fun SpeedOptions(
    currentSpeed: Float,
    onSelect: (Float) -> Unit
) {
    val spec = rememberVideoSettingsPanelVisualSpec()
    val options = listOf(
        0.5f to "0.5x",
        0.75f to "0.75x",
        1.0f to "正常",
        1.25f to "1.25x",
        1.3f to "1.3x",
        1.5f to "1.5x",
        2.0f to "2x"
    )
    
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spec.chipSpacing)
    ) {
        options.forEach { (speed, label) ->
            val isSelected = currentSpeed == speed
            AppSurface(
                onClick = { onSelect(speed) },
                shape = RoundedCornerShape(spec.chipCornerRadius),
                color = videoSettingsChipContainerColor(isSelected),
                modifier = Modifier.height(spec.chipHeight)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = spec.chipHorizontalPadding)
                ) {
                    AppText(
                        text = label,
                        fontSize = 13.sp,
                        color = videoSettingsChipContentColor(isSelected)
                    )
                }
            }
        }
    }
}

/**
 * 翻转/模式切换按钮
 */
@Composable
private fun FlipButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    policy: VideoSettingsPanelActionPolicy
) {
    val spec = rememberVideoSettingsPanelVisualSpec()
    AppSurface(
        onClick = onClick,
        shape = RoundedCornerShape(policy.pillHeightDp.dp),
        color = if (isActive) videoSettingsChipContainerColor(true) else videoSettingsChipContainerColor(false).copy(alpha = 0.78f),
        border = if (isActive) null else null,
        modifier = Modifier
            .height(policy.pillHeightDp.dp)
            .defaultMinSize(minWidth = policy.pillMinWidthDp.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = policy.pillHorizontalPaddingDp.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) 
                    videoSettingsChipContentColor(true)
                else 
                    videoSettingsChipContentColor(false),
                modifier = Modifier.size(policy.pillIconSizeDp.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            AppText(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                color = if (isActive) 
                    videoSettingsChipContentColor(true)
                else 
                    videoSettingsChipContentColor(false)
            )
        }
    }
}

@Composable
private fun SettingsActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    policy: VideoSettingsPanelActionPolicy
) {
    val usesTonalContainerTreatment = rememberAppPlayerChromeProfile()
        .effects
        .usesTonalContainerTreatment
    AppSurface(
        onClick = onClick,
        shape = RoundedCornerShape(policy.pillHeightDp.dp),
        color = if (usesTonalContainerTreatment) {
            AppSurfaceTokens.secondaryContainer()
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        modifier = Modifier
            .height(policy.pillHeightDp.dp)
            .defaultMinSize(minWidth = policy.pillMinWidthDp.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = policy.pillHorizontalPaddingDp.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(policy.pillIconSizeDp.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AppText(
                text = label,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 双击跳转秒数选项
 */
@Composable
private fun SeekSecondsOptions(
    currentSeconds: Int,
    onSelect: (Int) -> Unit
) {
    val spec = rememberVideoSettingsPanelVisualSpec()
    val options = listOf(5, 10, 15, 20, 30)
    
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spec.chipSpacing)
    ) {
        options.forEach { seconds ->
            val isSelected = currentSeconds == seconds
            AppSurface(
                onClick = { onSelect(seconds) },
                shape = RoundedCornerShape(spec.chipCornerRadius),
                color = videoSettingsChipContainerColor(isSelected),
                modifier = Modifier.height(spec.chipHeight)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = spec.chipHorizontalPadding)
                ) {
                    AppText(
                        text = "${seconds}s",
                        fontSize = 13.sp,
                        color = videoSettingsChipContentColor(isSelected)
                    )
                }
            }
        }
    }
}

/**
 * 长按倍速选项
 */
@Composable
private fun LongPressSpeedOptions(
    currentSpeed: Float,
    onSelect: (Float) -> Unit
) {
    val spec = rememberVideoSettingsPanelVisualSpec()
    val options = LONG_PRESS_SPEED_OPTIONS
    
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spec.chipSpacing)
    ) {
        options.forEach { speed ->
            val isSelected = currentSpeed == speed
            AppSurface(
                onClick = { onSelect(speed) },
                shape = RoundedCornerShape(spec.chipCornerRadius),
                color = videoSettingsChipContainerColor(isSelected),
                modifier = Modifier.height(spec.chipHeight)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = spec.chipHorizontalPadding)
                ) {
                    AppText(
                        text = "${speed}x",
                        fontSize = 13.sp,
                        color = videoSettingsChipContentColor(isSelected)
                    )
                }
            }
        }
    }
}
