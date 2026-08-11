package com.android.purebilibili.feature.home.components
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.components.AppButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppSwitch

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.util.CrashReporter
import com.android.purebilibili.core.theme.BiliPink
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.appContentDialogWidth
import com.android.purebilibili.core.ui.resolveAppContentDialogLayoutPolicy
import com.android.purebilibili.core.ui.resolveAppContentDialogProperties
import kotlinx.coroutines.launch

/**
 *  首次启动隐私提示弹窗
 * 告知用户关于崩溃追踪的用途，并让用户选择是否开启
 */
@Composable
fun CrashTrackingConsentDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isEnabled by remember { mutableStateOf(true) }  // 默认开启
    val dialogLayout = remember { resolveAppContentDialogLayoutPolicy(maxWidthDp = 420) }
    
    Dialog(
        onDismissRequest = { /* 不允许点击外部关闭 */ },
        properties = resolveAppContentDialogProperties(
            base = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
            usePlatformDefaultWidth = dialogLayout.usePlatformDefaultWidth,
        ),
    ) {
        AppSurface(
            modifier = Modifier.appContentDialogWidth(policy = dialogLayout),
            shape = AppShapes.container(ContainerLevel.Dialog),
            color = AppSurfaceTokens.cardContainer(),
            tonalElevation = AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro,
            shadowElevation = AppSpacingTokens.Small
        ) {
            Column(
                modifier = Modifier
                    .padding(AppSpacingTokens.ExtraLarge)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                AppText(
                    text = "🛡️ 帮助我们改进应用",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(AppSpacingTokens.Large))
                
                // 说明文字
                AppText(
                    text = "为了快速发现和修复应用问题，BiliPai 会收集崩溃报告和错误日志。\n\n" +
                           "默认仅启用崩溃追踪；使用情况统计默认关闭。播放器诊断日志保持可用，方便排查黑屏、卡顿等播放问题。\n\n" +
                           "这些数据仅用于改善应用稳定性，你也可以随时在「设置」中调整相关开关。",
                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
                
                Spacer(modifier = Modifier.height(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall))
                
                // 开关选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText(
                        text = "启用崩溃追踪",
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AppSwitch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                    )
                }
                
                Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraLarge))
                
                // 确认按钮
                AppButton(
                    onClick = {
                        scope.launch {
                            // 保存用户选择
                            SettingsManager.setCrashTrackingEnabled(context, isEnabled)
                            SettingsManager.setCrashTrackingConsentShown(context, true)
                            
                            // 应用设置到 Crashlytics
                            CrashReporter.setEnabled(isEnabled)
                            
                            //  [修复] 确保设置保存后再关闭弹窗
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppSpacingTokens.TripleExtraLarge),
                    shape = AppShapes.container(ContainerLevel.Card),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiliPink
                    )
                ) {
                    AppText(
                        text = "确定",
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
