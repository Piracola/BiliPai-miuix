package com.android.purebilibili.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.rememberIsNativeMiuixEnabled
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator as MiuixLinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.window.WindowDialog as MiuixWindowDialog
import java.io.File

/** Native update dialog renderer. M3/iOS use Material 3; Android Miuix uses WindowDialog. */
@Composable
internal fun AppUpdateDialogHost(
    update: AppUpdateCheckResult,
    showReleaseNotesOnly: Boolean = false,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val useMiuix = rememberIsNativeMiuixEnabled()
    val preferredAsset = remember(update.assets) { selectPreferredAppUpdateAsset(update.assets) }
    val expectedSha256 = remember(preferredAsset, update.buildMetadata) {
        preferredAsset?.let { resolveAppUpdateExpectedSha256(it, update.buildMetadata) }
    }
    var downloadState by remember(update.latestVersion) { mutableStateOf(AppUpdateDownloadState()) }

    LaunchedEffect(update.latestVersion, expectedSha256) {
        while (true) {
            downloadState = AppUpdateDownloadCoordinator.readState(
                context = context,
                releaseVersion = update.latestVersion,
                checksumProvided = expectedSha256 != null,
            )
            delay(500)
        }
    }

    val onPrimaryAction: () -> Unit = {
        val completedFile = downloadState.filePath
            ?.takeIf { downloadState.status == AppUpdateDownloadStatus.COMPLETED }
            ?.let(::File)
            ?.takeIf(File::exists)
        when {
            completedFile != null -> {
                val action = installDownloadedAppUpdate(context, completedFile)
                if (action == AppUpdateInstallAction.OPEN_UNKNOWN_SOURCES_SETTINGS) {
                    Toast.makeText(context, "请先允许安装未知来源应用", Toast.LENGTH_SHORT).show()
                }
            }
            preferredAsset != null -> AppUpdateDownloadCoordinator.enqueue(
                context = context,
                asset = preferredAsset,
                releaseVersion = update.latestVersion,
                expectedSha256 = expectedSha256,
            )
            else -> uriHandler.openUri(update.releaseUrl)
        }
        Unit
    }
    val onCancelDownload: () -> Unit = {
        preferredAsset?.let { AppUpdateDownloadCoordinator.cancel(context, it.name) }
        Unit
    }
    val title = if (showReleaseNotesOnly) {
        "更新日志 v${update.latestVersion}"
    } else {
        "发现新版本 v${update.latestVersion}"
    }

    if (useMiuix) {
        MiuixWindowDialog(
            show = true,
            title = title,
            summary = "当前版本 v${update.currentVersion}",
            onDismissRequest = onDismissRequest,
        ) {
            UpdateDialogBody(
                update = update,
                state = downloadState,
                showReleaseNotesOnly = showReleaseNotesOnly,
                useMiuix = true,
                modifier = Modifier.fillMaxWidth(),
            )
            MiuixUpdateActions(
                state = downloadState,
                showReleaseNotesOnly = showReleaseNotesOnly,
                hasAsset = preferredAsset != null,
                onPrimaryAction = onPrimaryAction,
                onCancelDownload = onCancelDownload,
                onDismissRequest = onDismissRequest,
            )
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(title) },
            text = {
                UpdateDialogBody(
                    update = update,
                    state = downloadState,
                    showReleaseNotesOnly = showReleaseNotesOnly,
                    useMiuix = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                if (!showReleaseNotesOnly) {
                    Button(
                        onClick = onPrimaryAction,
                        enabled = downloadState.status != AppUpdateDownloadStatus.DOWNLOADING,
                        modifier = Modifier.sizeIn(minHeight = 48.dp),
                    ) { Text(updatePrimaryLabel(downloadState, preferredAsset != null)) }
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (downloadState.status == AppUpdateDownloadStatus.DOWNLOADING ||
                        downloadState.status == AppUpdateDownloadStatus.QUEUED
                    ) {
                        OutlinedButton(
                            onClick = onCancelDownload,
                            modifier = Modifier.sizeIn(minHeight = 48.dp),
                        ) { Text("取消下载") }
                    }
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.sizeIn(minHeight = 48.dp),
                    ) { Text(if (showReleaseNotesOnly) "关闭" else "稍后") }
                }
            },
        )
    }
}

@Composable
private fun UpdateDialogBody(
    update: AppUpdateCheckResult,
    state: AppUpdateDownloadState,
    showReleaseNotesOnly: Boolean,
    useMiuix: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.heightIn(max = 360.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        UpdateDialogText("当前版本 v${update.currentVersion}", useMiuix, MaterialTheme.typography.bodyMedium)
        if (!showReleaseNotesOnly) {
            selectPreferredAppUpdateAsset(update.assets)?.let { asset ->
                UpdateDialogText("安装包：${asset.name}", useMiuix, MaterialTheme.typography.bodySmall)
            }
            UpdateDownloadProgress(state, useMiuix)
        }
        HorizontalDivider()
        UpdateReleaseNotesContent(update.releaseNotes, useMiuix)
    }
}

@Composable
private fun UpdateDownloadProgress(state: AppUpdateDownloadState, useMiuix: Boolean) {
    if (state.status == AppUpdateDownloadStatus.IDLE) return
    val statusText = when (state.status) {
        AppUpdateDownloadStatus.QUEUED -> "等待网络后开始下载"
        AppUpdateDownloadStatus.DOWNLOADING -> "下载中 ${(state.progress * 100).toInt()}%"
        AppUpdateDownloadStatus.COMPLETED -> "下载完成，可安装"
        AppUpdateDownloadStatus.FAILED -> state.errorMessage ?: "下载失败"
        AppUpdateDownloadStatus.IDLE -> ""
    }
    UpdateDialogText(statusText, useMiuix, MaterialTheme.typography.bodySmall)
    if (state.status == AppUpdateDownloadStatus.DOWNLOADING || state.status == AppUpdateDownloadStatus.QUEUED) {
        if (useMiuix) {
            MiuixLinearProgressIndicator(
                progress = state.progress.takeIf { state.totalBytes > 0L },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            if (state.totalBytes > 0L) {
                LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
    if (state.status == AppUpdateDownloadStatus.COMPLETED) {
        UpdateDialogText(
            if (state.checksumProvided) "SHA-256 校验通过" else "Release 未提供 SHA-256 校验信息",
            useMiuix,
            MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun UpdateReleaseNotesContent(releaseNotes: String, useMiuix: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        parseUpdateReleaseNotes(releaseNotes).forEach { block ->
            when (block) {
                is AppUpdateReleaseNotesBlock.Heading -> UpdateDialogText(
                    block.text,
                    useMiuix,
                    if (block.level == 1) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    FontWeight.SemiBold,
                )
                is AppUpdateReleaseNotesBlock.Bullet -> UpdateDialogText(
                    text = if (block.ordered) "• ${block.text}" else "• ${block.text}",
                    useMiuix = useMiuix,
                    style = MaterialTheme.typography.bodyMedium,
                )
                AppUpdateReleaseNotesBlock.Divider -> HorizontalDivider()
                is AppUpdateReleaseNotesBlock.Paragraph -> UpdateDialogText(
                    block.text,
                    useMiuix,
                    MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun UpdateDialogText(
    text: String,
    useMiuix: Boolean,
    style: androidx.compose.ui.text.TextStyle,
    fontWeight: FontWeight? = null,
) {
    if (useMiuix) {
        MiuixText(text = text, fontWeight = fontWeight)
    } else {
        Text(text = text, style = style, fontWeight = fontWeight)
    }
}

@Composable
private fun MiuixUpdateActions(
    state: AppUpdateDownloadState,
    showReleaseNotesOnly: Boolean,
    hasAsset: Boolean,
    onPrimaryAction: () -> Unit,
    onCancelDownload: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.status == AppUpdateDownloadStatus.DOWNLOADING || state.status == AppUpdateDownloadStatus.QUEUED) {
            MiuixTextButton(
                text = "取消下载",
                onClick = onCancelDownload,
                modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp),
            )
        }
        MiuixTextButton(
            text = if (showReleaseNotesOnly) "关闭" else "稍后",
            onClick = onDismissRequest,
            modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp),
        )
        if (!showReleaseNotesOnly) {
            MiuixButton(
                onClick = onPrimaryAction,
                enabled = state.status != AppUpdateDownloadStatus.DOWNLOADING,
                colors = MiuixButtonDefaults.buttonColorsPrimary(),
                modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp),
            ) {
                MiuixText(updatePrimaryLabel(state, hasAsset))
            }
        }
    }
}

private fun updatePrimaryLabel(state: AppUpdateDownloadState, hasAsset: Boolean): String = when {
    state.status == AppUpdateDownloadStatus.COMPLETED -> "安装更新"
    state.status == AppUpdateDownloadStatus.QUEUED -> "等待下载"
    state.status == AppUpdateDownloadStatus.DOWNLOADING -> "下载中"
    !hasAsset -> "前往下载"
    else -> "下载更新"
}
