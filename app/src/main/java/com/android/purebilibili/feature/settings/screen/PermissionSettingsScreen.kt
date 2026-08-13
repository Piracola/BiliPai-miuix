// 文件路径: feature/settings/PermissionSettingsScreen.kt
package com.android.purebilibili.feature.settings
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppHorizontalDivider

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.purebilibili.R
import com.android.purebilibili.core.ui.components.*
import com.android.purebilibili.core.ui.adaptiveSquircleBackground
import com.android.purebilibili.feature.settings.ui.SettingsPageScaffold
import com.android.purebilibili.core.theme.iOSPink  // 存储权限图标色
import com.android.purebilibili.core.theme.iOSBlue
import com.android.purebilibili.core.theme.iOSGreen
import com.android.purebilibili.core.theme.iOSOrange
import com.android.purebilibili.core.theme.iOSPurple
import com.android.purebilibili.core.theme.iOSTeal
import com.android.purebilibili.feature.settings.SettingsPageScrollHost

/**
 *  权限管理页面
 * 显示应用所有权限的用途说明和当前状态
 */
/**
 *  权限管理页面内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionSettingsScreen(
    onBack: () -> Unit
) {
    val screenTitle = stringResource(R.string.permission_management_title)
    val backLabel = stringResource(R.string.common_back)
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        SettingsPageScaffold(
            title = screenTitle,
            onBack = onBack,
            backContentDescription = backLabel,
            bottomContentPadding = bottomPadding,
            scrollHost = SettingsPageScrollHost.External,
        ) {
            PermissionSettingsContent()
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionSettingsContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    //  [修复] 设置导航栏透明，确保底部手势栏沉浸式效果
    androidx.compose.runtime.DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val originalNavBarColor = window?.navigationBarColor ?: android.graphics.Color.TRANSPARENT
        
        if (window != null) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
        
        onDispose {
            if (window != null) {
                window.navigationBarColor = originalNavBarColor
            }
        }
    }
    
    // 权限列表数据
    val permissions = remember {
        listOf(
            PermissionInfo(
                name = "网络访问",
                permission = Manifest.permission.INTERNET,
                description = "加载视频、图片和用户数据",
                icon = Icons.Outlined.Wifi,
                iconTint = iOSBlue,
                isNormal = true,
                alwaysGranted = true
            ),
            PermissionInfo(
                name = "网络状态",
                permission = Manifest.permission.ACCESS_NETWORK_STATE,
                description = "检测网络连接状态，优化加载体验",
                icon = Icons.Outlined.BarChart,
                iconTint = iOSGreen,
                isNormal = true,
                alwaysGranted = true
            ),
            PermissionInfo(
                name = "通知权限",
                permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else {
                    "android.permission.POST_NOTIFICATIONS"
                },
                description = "显示媒体播放控制通知，方便后台控制播放",
                icon = Icons.Outlined.Notifications,
                iconTint = iOSOrange,
                isNormal = false,
                alwaysGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ),
            PermissionInfo(
                name = "前台服务",
                permission = Manifest.permission.FOREGROUND_SERVICE,
                description = "支持后台播放视频时保持服务运行",
                icon = Icons.Outlined.PlayCircle,
                iconTint = iOSPurple,
                isNormal = true,
                alwaysGranted = true
            ),
            PermissionInfo(
                name = "媒体播放服务",
                permission = "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK",
                description = "允许应用在后台继续播放视频",
                icon = Icons.Outlined.MusicNote,
                iconTint = iOSTeal,
                isNormal = true,
                alwaysGranted = true
            ),
            //  DLNA 投屏所需权限
            PermissionInfo(
                name = "设备发现（DLNA）",
                permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.NEARBY_WIFI_DEVICES
                } else {
                    Manifest.permission.ACCESS_FINE_LOCATION
                },
                description = "用于扫描和连接附近的投屏设备（DLNA）",
                icon = Icons.Outlined.Tv,
                iconTint = iOSBlue,
                isNormal = false,
                alwaysGranted = false
            ),
             // 📁 存储写入（使用 MediaStore/SAF，不申请所有文件访问）
            PermissionInfo(
                name = "媒体文件写入",
                permission = "scoped_storage",
                description = "保存图片/截图时使用系统媒体库，下载导出使用系统文件夹授权",
                icon = Icons.Outlined.Folder,
                iconTint = iOSPink,
                isNormal = true,
                alwaysGranted = true
            ),

        )
    }
    
    // 检查权限状态
    var permissionStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    
    LaunchedEffect(Unit) {
        permissionStates = permissions.associate { info ->
            info.permission to if (info.alwaysGranted) {
                true
            } else {
                info.customCheck?.invoke(context)
                    ?: (ContextCompat.checkSelfPermission(context, info.permission) == PackageManager.PERMISSION_GRANTED)
            }
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
            Box(modifier = Modifier) {
                AppText(
                    text = "以下是应用所需的权限及其用途说明。普通权限在安装时自动授予，无需手动操作。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            Box(modifier = Modifier) {
                AppPreferenceSectionTitle("需要授权的权限")
            }
            Box(modifier = Modifier) {
                AppPreferenceGroup {
                    permissions.filter { !it.isNormal }.forEachIndexed { index, info ->
                        if (index > 0) AppHorizontalDivider()
                        PermissionItem(
                            info = info,
                            isGranted = permissionStates[info.permission] ?: false,
                            onOpenSettings = { openAppSettings(context) },
                        )
                    }
                }
            }

            Box(modifier = Modifier) {
                AppPreferenceSectionTitle("自动授予的权限")
            }
            Box(modifier = Modifier) {
                AppPreferenceGroup {
                    permissions.filter { it.isNormal }.forEachIndexed { index, info ->
                        if (index > 0) AppHorizontalDivider()
                        PermissionItem(
                            info = info,
                            isGranted = true,
                            onOpenSettings = null,
                        )
                    }
                }
            }

            Box(modifier = Modifier) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    AppText(
                        text = "BiliPai 仅在必要功能的前提下申请部分敏感权限。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
}

private data class PermissionInfo(
    val name: String,
    val permission: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val isNormal: Boolean,  // 是否是普通权限（自动授予）
    val alwaysGranted: Boolean = false,  // 是否总是被授予
    val customCheck: ((Context) -> Boolean)? = null // 自定义检查逻辑
)

/**
 * 单个权限项
 */
@Composable
private fun PermissionItem(
    info: PermissionInfo,
    isGranted: Boolean,
    onOpenSettings: (() -> Unit)?
) {
    val visualSpec = rememberAdaptiveListVisualCapabilities().componentSpec
    val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(info.iconTint)
    val iconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
    val grantedTint = rememberAdaptiveSemanticIconTint(iOSGreen)
    val deniedTint = rememberAdaptiveSemanticIconTint(com.android.purebilibili.core.theme.iOSRed)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onOpenSettings != null) { onOpenSettings?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(visualSpec.iconContainerSizeDp.dp)
                .adaptiveSquircleBackground(
                    color = effectiveIconTint,
                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                ),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                info.icon,
                contentDescription = null,
                tint = iconContentColor,
                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        // 名称和描述
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = info.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            AppText(
                text = info.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 状态指示器
        if (isGranted) {
            AppIcon(
                Icons.Outlined.CheckCircleOutline,
                contentDescription = "已授权",
                tint = grantedTint,
                modifier = Modifier.size(22.dp)
            )
        } else {
            // 未授权时显示红色的 X
            AppIcon(
                Icons.Outlined.Cancel,
                contentDescription = "未授权",
                tint = deniedTint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * 打开应用设置页面
 */
private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
