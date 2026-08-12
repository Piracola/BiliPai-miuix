package com.android.purebilibili.feature.settings.share

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import com.android.purebilibili.core.ui.AdaptiveLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import com.android.purebilibili.core.ui.components.AppIconButton
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.BuildConfig
import com.android.purebilibili.R
import com.android.purebilibili.core.theme.iOSBlue
import com.android.purebilibili.core.theme.iOSGreen
import com.android.purebilibili.core.theme.iOSOrange
import com.android.purebilibili.core.theme.iOSPink
import com.android.purebilibili.core.theme.iOSPurple
import com.android.purebilibili.feature.settings.SettingsPageScrollHost
import com.android.purebilibili.feature.settings.ui.SettingsPageScaffold
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.AppDialogAction
import com.android.purebilibili.core.ui.components.AppPreference
import com.android.purebilibili.core.ui.components.AppPreferenceDivider
import com.android.purebilibili.core.ui.components.AppPreferenceGroup
import com.android.purebilibili.core.ui.components.AppPreferenceSectionTitle
import com.android.purebilibili.core.ui.components.AppSwitchPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsShareScreen(
    onBack: () -> Unit,
    viewModel: SettingsShareViewModel = viewModel()
) {
    val context = LocalContext.current
    val screenTitle = stringResource(R.string.settings_share_title)
    val backLabel = stringResource(R.string.common_back)
    val shareChooserTitle = stringResource(R.string.settings_share_chooser_title)
    val shareOpenFailed = stringResource(R.string.settings_share_open_failed)
    val importConfirmLabel = stringResource(R.string.settings_share_import_confirm)
    val viewSkippedLabel = stringResource(R.string.settings_share_view_skipped)
    val cancelLabel = stringResource(R.string.common_cancel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToUri(uri)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.loadImportPreview(uri)
        }
    }

    LaunchedEffect(uiState.pendingShareUri) {
        val shareUri = uiState.pendingShareUri ?: return@LaunchedEffect
        runCatching {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
        }.onFailure {
            Toast.makeText(
                context,
                it.message ?: shareOpenFailed,
                Toast.LENGTH_SHORT
            ).show()
        }
        viewModel.consumeShareUri()
    }

    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    SettingsPageScaffold(
        title = screenTitle,
        onBack = onBack,
        backContentDescription = backLabel,
        bottomContentPadding = bottomContentPadding,
        scrollHost = SettingsPageScrollHost.External,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
            item {
                AppPreferenceSectionTitle("执行状态")
                AppPreferenceGroup {
                    // 状态放 subtitle，避免右侧 value 窄列把长文案拆成「操作」单独一行。
                    AppPreference(
                        icon = if (uiState.isBusy) Icons.Filled.Pending else Icons.Filled.History,
                        title = if (uiState.isBusy) "正在处理" else "最近状态",
                        subtitle = uiState.statusMessage ?: "尚未执行导入导出操作",
                        onClick = if (uiState.statusMessage != null) ({ viewModel.clearStatus() }) else null,
                        iconTint = if (uiState.isBusy) iOSOrange else iOSGreen,
                        showChevron = false
                    )
                }
            }

            item {
                AppPreferenceSectionTitle("说明")
                AppPreferenceGroup {
                    AppPreference(
                        icon = Icons.Filled.CheckCircle,
                        title = "会一起分享的内容",
                        subtitle = "外观、播放、手势、弹幕、导航等可交流设置",
                        onClick = null,
                        iconTint = iOSGreen,
                        showChevron = false
                    )
                    AppPreferenceDivider(startIndent = 66.dp)
                    AppPreference(
                        icon = Icons.Filled.Lock,
                        title = "会自动跳过的内容",
                        subtitle = "账号、下载路径、WebDAV、隐私与设备相关配置",
                        onClick = null,
                        iconTint = iOSPurple,
                        showChevron = false
                    )
                }
            }

            item {
                AppPreferenceSectionTitle("导出选项")
                AppPreferenceGroup {
                    AppSwitchPreference(
                        title = "包含设备调试信息",
                        subtitle = "安卓版本、UI 名称、密度、分辨率等，便于排查界面问题（导入时忽略）",
                        checked = uiState.includeDeviceDebug,
                        onCheckedChange = viewModel::setIncludeDeviceDebug,
                        icon = Icons.Filled.BugReport,
                        iconTint = iOSOrange,
                    )
                }
            }

            item {
                AppPreferenceSectionTitle("操作")
                AppPreferenceGroup {
                    AppPreference(
                        icon = Icons.Filled.Download,
                        title = "导出到文件",
                        subtitle = if (uiState.includeDeviceDebug) {
                            "生成可分享的设置文件（含设备调试信息）"
                        } else {
                            "生成可分享的设置文件（JSON）"
                        },
                        onClick = {
                            exportLauncher.launch(
                                buildSettingsShareFileName(
                                    appVersion = BuildConfig.VERSION_NAME,
                                    epochMs = System.currentTimeMillis()
                                )
                            )
                        },
                        iconTint = iOSBlue
                    )
                    AppPreferenceDivider(startIndent = 66.dp)
                    AppPreference(
                        icon = Icons.Filled.Share,
                        title = "分享导出文件",
                        subtitle = "导出后直接调起系统分享",
                        onClick = { viewModel.prepareShare() },
                        iconTint = iOSGreen
                    )
                    AppPreferenceDivider(startIndent = 66.dp)
                    AppPreference(
                        icon = Icons.Filled.UploadFile,
                        title = "从文件导入",
                        subtitle = "预览可导入内容后再一键应用",
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                        iconTint = iOSPink
                    )
                }
            }

            item {
                AppPreferenceSectionTitle("文件格式")
                AppPreferenceGroup {
                    AppPreference(
                        icon = Icons.Filled.DataObject,
                        title = "设置包（JSON）",
                        subtitle = "支持用户查看，也支持应用内一键导入",
                        value = "格式版本 v$SETTINGS_SHARE_SCHEMA_VERSION",
                        onClick = null,
                        iconTint = iOSOrange,
                        showChevron = false
                    )
                }
            }
            }

            if (uiState.isBusy) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    AdaptiveLoadingIndicator()
                }
            }
        }
    }

    val pendingImportSession = uiState.pendingImportSession
    if (pendingImportSession != null) {
        var showRawKeys by remember(pendingImportSession) { mutableStateOf(false) }
        AppAlertDialog(
            onDismissRequest = { viewModel.dismissImportPreview() },
            title = {
                AppText(
                    text = "导入设置",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppText(
                        text = buildImportPreviewSummary(pendingImportSession),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (showRawKeys && pendingImportSession.preview.skippedKeys.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AppText(
                            text = pendingImportSession.preview.skippedKeys.joinToString(separator = "\n"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                AppDialogAction(onClick = { viewModel.confirmImport() }) {
                    AppText(importConfirmLabel)
                }
            },
            dismissButton = {
                AppDialogAction(
                    onClick = {
                        if (pendingImportSession.preview.skippedKeys.isNotEmpty() && !showRawKeys) {
                            showRawKeys = true
                        } else {
                            viewModel.dismissImportPreview()
                        }
                    }
                ) {
                    AppText(
                        if (pendingImportSession.preview.skippedKeys.isNotEmpty() && !showRawKeys) {
                            viewSkippedLabel
                        } else {
                            cancelLabel
                        }
                    )
                }
            }
        )
    }
}

private fun buildImportPreviewSummary(session: SettingsShareImportSession): String {
    val sectionSummary = session.preview.importableSections
        .joinToString(separator = " / ") { it.label }
        .ifBlank { "无可导入分类" }
    val skippedCount = session.preview.skippedKeys.size
    val skippedSummary = if (skippedCount > 0) {
        "将跳过 $skippedCount 项本机专属或未知配置"
    } else {
        "没有需要跳过的项目"
    }
    return buildString {
        appendLine("配置名：${session.profile.profileName}")
        appendLine("来源版本：${session.profile.appVersion}")
        appendLine("导出时间：${session.profile.exportedAtIso}")
        appendLine("可导入分类：$sectionSummary")
        append(skippedSummary)
    }.trim()
}
