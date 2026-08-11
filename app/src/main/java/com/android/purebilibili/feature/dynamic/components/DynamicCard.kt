// 文件路径: feature/dynamic/components/DynamicCard.kt
package com.android.purebilibili.feature.dynamic.components
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppHorizontalDivider

import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens

import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//  Material Icons
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppDropdownMenu
import com.android.purebilibili.core.ui.components.AppDropdownMenuItem
import com.android.purebilibili.core.ui.components.AppIconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.common.CopySelectionDialog
import com.android.purebilibili.core.ui.rememberAppMoreIcon
import com.android.purebilibili.core.ui.rememberAppVisibilityOffIcon
import com.android.purebilibili.core.ui.rememberAppWarningIcon
import com.android.purebilibili.core.ui.rememberAppChevronDownIcon
import com.android.purebilibili.core.ui.rememberAppChevronUpIcon
import com.android.purebilibili.core.ui.rememberAppCommentIcon
import com.android.purebilibili.core.ui.rememberAppVisibilityOnIcon
import com.android.purebilibili.core.ui.rememberAppShareIcon
import com.android.purebilibili.core.theme.resolveAccessibleContainerColors
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.AppDialogAction
import com.android.purebilibili.core.ui.rememberAppHistoryIcon
import com.android.purebilibili.core.ui.rememberAppDeleteIcon
import com.android.purebilibili.core.ui.rememberAppLinkIcon
import com.android.purebilibili.data.model.response.DynamicDesc
import com.android.purebilibili.data.model.response.DynamicItem
import com.android.purebilibili.data.model.response.DrawItem
import com.android.purebilibili.data.model.response.ReplyInteractionData
import com.android.purebilibili.feature.dynamic.resolveDynamicActionButtonSlotWeight
import com.android.purebilibili.feature.dynamic.resolveDynamicActionButtonSpacing
import com.android.purebilibili.feature.dynamic.resolveDynamicCardContentPadding
import com.android.purebilibili.feature.dynamic.resolveDynamicCardOuterPadding
import com.android.purebilibili.data.model.response.DynamicStatModule
import com.android.purebilibili.data.model.response.DynamicType
import com.android.purebilibili.data.model.response.OpusContentBlock
import com.android.purebilibili.feature.dynamic.DynamicDeleteAction
import com.android.purebilibili.feature.dynamic.resolveDynamicDeleteAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 *  动态卡片V2 - 官方风格
 */
@Composable
fun DynamicCardV2(
    item: DynamicItem,
    onVideoClick: (String) -> Unit,
    onBangumiClick: (Long, Long) -> Unit = { _, _ -> },
    onUserClick: (Long) -> Unit,
    onLiveClick: (roomId: Long, title: String, uname: String) -> Unit = { _, _, _ -> },
    onArticleClick: ((articleId: Long, title: String) -> Unit)? = null,
    onDynamicDetailClick: ((dynamicId: String) -> Unit)? = null,
    isDetail: Boolean = false,
    onPrimaryClickOverride: ((DynamicItem) -> Unit)? = null,
    gifImageLoader: ImageLoader,
    //  [新增] 评论/转发/点赞回调
    onCommentClick: (dynamicId: String) -> Unit = {},
    onRepostClick: (dynamicId: String) -> Unit = {},
    onLikeClick: (dynamicId: String) -> Unit = {},
    onWatchLaterClick: ((aid: Long) -> Unit)? = null,
    onDeleteClick: ((DynamicDeleteAction) -> Unit)? = null,
    onManageAction: (DynamicManageAction) -> Unit = {},
    onLoadReplyInteractionStatus: ((oid: Long, type: Int, onLoaded: (ReplyInteractionData?) -> Unit) -> Unit)? = null,
    isLiked: Boolean = false,
    forwardCountDelta: Int = 0
) {
    val author = item.modules.module_author
    val content = item.modules.module_dynamic
    val stat = item.modules.module_stat
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val dynamicPreviewTextVisible by SettingsManager.getDynamicImagePreviewTextVisible(context)
        .collectAsStateWithLifecycle(initialValue = true)
    val authorTimeText = remember(author?.pub_time, author?.pub_ts) {
        author?.let {
            resolveDynamicAuthorTimeText(
                pubTime = it.pub_time,
                pubTs = it.pub_ts
            )
        }.orEmpty()
    }
    val contentHasImages = content?.major?.draw?.items?.isNotEmpty() == true ||
        content?.major?.opus?.pics?.isNotEmpty() == true
    val visibleDynamicDesc = content?.desc?.let { desc ->
        resolveDynamicDescForImages(desc, hasImages = contentHasImages)
    }
    val fullOpusContentBlocks = content?.major?.opus?.let { opus ->
        resolveDynamicOpusPresentationBlocks(opus = opus, isDetail = isDetail)
    }.orEmpty()
    val hasFullOpusDetailContent = fullOpusContentBlocks.isNotEmpty()
    val type = DynamicType.fromApiValue(item.type)
    val cardClickAction = remember(item) { resolveDynamicCardPrimaryAction(item) }
    val watchLaterAid = remember(item) { resolveDynamicWatchLaterAid(item) }
    val deleteAction = remember(item) { resolveDynamicDeleteAction(item) }
    var pendingDeleteAction by remember(item.id_str) { mutableStateOf<DynamicDeleteAction?>(null) }
    //  [新增] 评论互动设置弹窗状态
    var showReplyInteractionDialog by remember(item.id_str) { mutableStateOf<ReplyInteractionData?>(null) }
    var replyInteractionOid by remember(item.id_str) { mutableStateOf(0L) }
    var replyInteractionType by remember(item.id_str) { mutableStateOf(0) }
    val isCurrentlyTop = item.modules.module_tag?.text == "置顶"
    val isPrimaryClickEnabled = remember(cardClickAction, onArticleClick, onDynamicDetailClick, onPrimaryClickOverride) {
        shouldEnableDynamicCardPrimaryClick(
            action = cardClickAction,
            hasArticleClick = onArticleClick != null,
            hasDynamicDetailClick = onDynamicDetailClick != null,
            hasPrimaryClickOverride = onPrimaryClickOverride != null
        )
    }

    pendingDeleteAction?.let { action ->
        AppAlertDialog(
            onDismissRequest = { pendingDeleteAction = null },
            title = { AppText(action.title) },
            text = { AppText(action.content) },
            confirmButton = {
                AppDialogAction(
                    onClick = {
                        pendingDeleteAction = null
                        onDeleteClick?.invoke(action)
                    }
                ) {
                    AppText(action.confirmText, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                AppDialogAction(onClick = { pendingDeleteAction = null }) {
                    AppText(action.cancelText)
                }
            }
        )
    }

    //  [新增] 评论互动设置弹窗（评论精选 / 评论开关，对齐 PiliPlus）
    showReplyInteractionDialog?.let { interactionData ->
        val selection = interactionData.up_reply_selection
        val reply = interactionData.up_reply
        val selectionEnabled = selection?.status == 1
        val replyEnabled = reply?.status == 1
        AppAlertDialog(
            onDismissRequest = { showReplyInteractionDialog = null },
            title = { AppText("评论互动设置") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.container(ContainerLevel.Chip))
                            .clickable(enabled = selection?.can_modify == true) {
                                showReplyInteractionDialog = null
                                onManageAction(
                                    DynamicManageAction.SetReplySubject(
                                        oid = replyInteractionOid,
                                        replyType = replyInteractionType,
                                        action = resolveDynamicReplySelectionAction(selectionEnabled)
                                    )
                                )
                            }
                            .padding(vertical = AppSpacingTokens.Medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            rememberAppCommentIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(AppSpacingTokens.Large),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                        AppText(
                            if (selectionEnabled) "停止评论精选" else "开启评论精选",
                            color = if (selection?.can_modify == true) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.container(ContainerLevel.Chip))
                            .clickable(enabled = reply?.can_modify == true) {
                                showReplyInteractionDialog = null
                                onManageAction(
                                    DynamicManageAction.SetReplySubject(
                                        oid = replyInteractionOid,
                                        replyType = replyInteractionType,
                                        action = resolveDynamicReplyOpenAction(replyEnabled)
                                    )
                                )
                            }
                            .padding(vertical = AppSpacingTokens.Medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            rememberAppVisibilityOffIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(AppSpacingTokens.Large),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                        AppText(
                            if (replyEnabled) "关闭评论" else "恢复评论",
                            color = if (reply?.can_modify == true) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                AppDialogAction(onClick = { showReplyInteractionDialog = null }) {
                    AppText("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = resolveDynamicCardOuterPadding())
            .clickable(enabled = isPrimaryClickEnabled) {
                dispatchDynamicCardPrimaryClick(
                    item = item,
                    action = cardClickAction,
                    onPrimaryClickOverride = onPrimaryClickOverride,
                    onVideoClick = onVideoClick,
                    onBangumiClick = onBangumiClick,
                    onArticleClick = onArticleClick,
                    onDynamicDetailClick = onDynamicDetailClick,
                    onUserClick = onUserClick,
                    onLiveClick = onLiveClick
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = resolveDynamicCardContentPadding())
                .padding(top = AppSpacingTokens.Medium, bottom = if (isDetail) AppSpacingTokens.None else AppSpacingTokens.Small + AppSpacingTokens.Micro)
        ) {
        //  [新增] 更多菜单状态
        var showMoreMenu by remember { mutableStateOf(false) }
        val context = LocalContext.current
        
        //  用户头部（头像 + 名称 + 时间 + 更多）
        if (author != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                Box(
                    modifier = Modifier
                        .size(AppChromeSizeTokens.MinimumTouchTarget)
                        .clip(CircleShape)
                        .semantics { contentDescription = "查看${author.name}的个人主页" }
                        .clickable(enabled = author.mid > 0) {
                            dispatchDynamicCardPrimaryAction(
                                action = DynamicCardPrimaryAction.OpenUser(author.mid),
                                onVideoClick = onVideoClick,
                                onBangumiClick = onBangumiClick,
                                onArticleClick = onArticleClick,
                                onDynamicDetailClick = onDynamicDetailClick,
                                onUserClick = onUserClick,
                                onLiveClick = onLiveClick
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(author.face.let { if (it.startsWith("http://")) it.replace("http://", "https://") else it })
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Small)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
                
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        author.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        color = if (author.vip?.status == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    AppText(
                        authorTimeText,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                    )
                }
                
                //  置顶标（module_tag：B 站固定返回 "置顶"，对齐 PiliPlus 作者区头部样式）
                if (shouldShowDynamicPinnedTag(item.modules.module_tag)) {
                    Box(
                        modifier = Modifier
                            .padding(start = AppSpacingTokens.ExtraSmall)
                            .border(
                                width = AppSurfaceTokens.OutlineWidth,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(
                                horizontal = AppSpacingTokens.ExtraSmall,
                                vertical = AppSpacingTokens.Micro
                            )
                    ) {
                        AppText(
                            item.modules.module_tag?.text.orEmpty(),
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                //  [修复] 更多按钮 + 下拉菜单
                Box {
                    AppIconButton(onClick = { showMoreMenu = true }) {
                        AppIcon(
                            rememberAppMoreIcon(),
                            contentDescription = "更多",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                        )
                    }
                    
                    // 下拉菜单 - 自适应背景
                    AppDropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer) // 使用 surfaceContainer 获得微略不同的背景
                    ) {
                        // 复制链接
                        AppDropdownMenuItem(
                            text = { AppText("复制链接", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { 
                                AppIcon(
                                    rememberAppLinkIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
                                    tint = MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                showMoreMenu = false
                                // 复制动态链接到剪贴板
                                val dynamicUrl = "https://t.bilibili.com/${item.id_str}"
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("动态链接", dynamicUrl))
                                android.widget.Toast.makeText(context, "已复制链接", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )

                        //  [新增] 分享动态（系统分享，对齐 PiliPlus 分享面板）
                        AppDropdownMenuItem(
                            text = { AppText("分享动态", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = {
                                AppIcon(
                                    rememberAppShareIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                val dynamicUrl = "https://t.bilibili.com/${item.id_str}"
                                val descText = content?.desc?.text?.take(100).orEmpty()
                                val shareText = if (descText.isNotBlank()) {
                                    "$descText\n$dynamicUrl"
                                } else {
                                    "分享动态\n$dynamicUrl"
                                }
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    this.type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(
                                    android.content.Intent.createChooser(shareIntent, "分享动态")
                                )
                            }
                        )
                        
                        if (watchLaterAid != null && onWatchLaterClick != null) {
                            AppDropdownMenuItem(
                                text = { AppText("稍后再看", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = {
                                    AppIcon(
                                        rememberAppHistoryIcon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onWatchLaterClick(watchLaterAid)
                                }
                            )
                        }

                        if (deleteAction != null && onDeleteClick != null) {
                            AppDropdownMenuItem(
                                text = { AppText(deleteAction.label, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    AppIcon(
                                        rememberAppDeleteIcon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    pendingDeleteAction = deleteAction
                                }
                            )
                        }

                        // 不感兴趣
                        AppDropdownMenuItem(
                            text = { AppText("不感兴趣", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { 
                                AppIcon(
                                    rememberAppVisibilityOffIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
                                    tint = MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                showMoreMenu = false
                                android.widget.Toast.makeText(context, "已标记为不感兴趣", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )

                        //  [新增] 置顶 / 取消置顶（对齐 PiliPlus morePanel）
                        AppDropdownMenuItem(
                            text = { AppText(resolveDynamicPinnedMenuLabel(isCurrentlyTop), color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = {
                                AppIcon(
                                    rememberAppChevronUpIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onManageAction(DynamicManageAction.ToggleTop(item.id_str, isCurrentlyTop))
                            }
                        )

                        //  [新增] 可见范围（公开 / 仅自己可见）
                        AppDropdownMenuItem(
                            text = { AppText(resolveDynamicVisibilityMenuLabel(isPrivate = false), color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = {
                                AppIcon(
                                    rememberAppVisibilityOnIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onManageAction(
                                    DynamicManageAction.SetVisibility(
                                        dynamicId = item.id_str,
                                        dynType = resolveDynamicDynType(item),
                                        isPrivate = true
                                    )
                                )
                            }
                        )

                        //  [新增] 评论互动设置（评论精选 / 评论开关）
                        AppDropdownMenuItem(
                            text = { AppText("评论互动设置", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = {
                                AppIcon(
                                    rememberAppCommentIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                val oid = resolveDynamicReplySubjectOid(item)
                                if (oid == null || onLoadReplyInteractionStatus == null) {
                                    android.widget.Toast.makeText(context, "该动态暂不支持互动设置", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    onLoadReplyInteractionStatus(oid, resolveDynamicReplySubjectType(item)) { data ->
                                        if (data == null) {
                                            android.widget.Toast.makeText(context, "获取互动设置失败", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            replyInteractionOid = oid
                                            replyInteractionType = resolveDynamicReplySubjectType(item)
                                            showReplyInteractionDialog = data
                                        }
                                    }
                                }
                            }
                        )

                        //  [新增] 临时屏蔽（仅内存，重启恢复）
                        AppDropdownMenuItem(
                            text = { AppText("临时屏蔽", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = {
                                AppIcon(
                                    rememberAppVisibilityOffIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onManageAction(DynamicManageAction.TempBlock(item.id_str))
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
        }
        
        //  风险提示条（module_dispute：如“视频内含有危险行为，请勿模仿”，点击打开 jump_url）
        item.modules.module_dispute?.let { dispute ->
            if (shouldShowDynamicDispute(dispute)) {
                val disputeColors = resolveAccessibleContainerColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    fallbackContentColors = listOf(
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.onBackground,
                    ),
                )
                val disputeClickModifier = if (dispute.jump_url.isNotBlank()) {
                    Modifier.clickable {
                        val target = if (dispute.jump_url.startsWith("//")) {
                            "https:${dispute.jump_url}"
                        } else {
                            dispute.jump_url
                        }
                        uriHandler.openUri(target)
                    }
                } else {
                    Modifier
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AppSpacingTokens.Medium)
                        .clip(RoundedCornerShape(6.dp))
                        .background(disputeColors.containerColor)
                        .then(disputeClickModifier)
                        .padding(
                            horizontal = AppSpacingTokens.Small,
                            vertical = AppSpacingTokens.Small
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        rememberAppWarningIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(AppSpacingTokens.Small + AppSpacingTokens.Micro),
                        tint = disputeColors.contentColor
                    )
                    Spacer(modifier = Modifier.width(AppSpacingTokens.ExtraSmall))
                    AppText(
                        dispute.title.ifBlank { dispute.desc },
                        fontSize = MaterialTheme.typography.labelMedium.fontSize,
                        color = disputeColors.contentColor
                    )
                }
            }
        }
        
        //  动态内容文字（支持@高亮 / 表情）；优先可渲染表情的 desc 或 opus summary
        val visibleOpusSummaryDescForBody = remember(content?.major?.opus?.summary, content?.major?.opus?.pics) {
            val opus = content?.major?.opus ?: return@remember null
            opus.summary?.let { summary ->
                resolveDynamicOpusSummaryDescForImages(
                    text = summary.text,
                    richTextNodes = summary.rich_text_nodes,
                    hasImages = opus.pics.isNotEmpty()
                )
            }
        }
        val preferredBodyDesc = resolvePreferredDynamicDesc(
            primary = visibleDynamicDesc,
            fallback = visibleOpusSummaryDescForBody
        )
        if (!hasFullOpusDetailContent) preferredBodyDesc?.let { desc ->
            if (shouldRenderDynamicRichText(desc)) {
                RichTextContent(
                    desc = desc,
                    onUserClick = onUserClick
                )
                Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
            }
        }
        
        //  视频类型动态 - 大图预览
        content?.major?.archive?.let { archive ->
            val playableBvid = resolveArchivePlayableBvid(archive)
            VideoCardLarge(
                archive = archive,
                publishTs = author?.pub_ts ?: 0L,
                cornerBadgeText = resolveDynamicArchiveBadgeLabel(archive),
                onClick = {
                    playableBvid?.let(onVideoClick)
                        ?: onDynamicDetailClick?.invoke(item.id_str)
                },
                sharedElementKey = com.android.purebilibili.core.ui.transition.videoPlayerSharedElementKey(archive.bvid)
            )
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
        }

        content?.major?.pgc?.let { pgc ->
            val bangumiTarget = resolveArchiveBangumiTarget(pgc)
            VideoCardLarge(
                archive = pgc,
                publishTs = author?.pub_ts ?: 0L,
                cornerBadgeText = "番剧",
                onClick = {
                    bangumiTarget?.let { onBangumiClick(it.seasonId, it.epId) }
                        ?: onDynamicDetailClick?.invoke(item.id_str)
                },
                sharedElementKey = com.android.purebilibili.core.ui.transition.videoPlayerSharedElementKey(
                    pgc.bvid.ifBlank { item.id_str }
                )
            )
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
        }
        
        //  图片类型动态（支持GIF + 点击预览）
        content?.major?.draw?.let { draw ->
            var selectedImageIndex by remember { mutableIntStateOf(-1) }
            var sourceRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
            val drawPreviewText = remember(author?.name, visibleDynamicDesc?.text) {
                ImagePreviewTextContent(
                    headline = author?.name.orEmpty(),
                    body = visibleDynamicDesc?.text.orEmpty()
                )
            }
            
            DrawGridV2(
                items = draw.items,
                gifImageLoader = gifImageLoader,
                maxDisplayImages = resolveDynamicOpusPreviewImageLimit(isDetail),
                onImageClick = { index, rect ->
                    val action = resolveDynamicCardMediaAction(item, index)
                    if (action is DynamicCardMediaAction.PreviewImages) {
                        selectedImageIndex = action.initialIndex
                        sourceRect = rect
                    }
                }
            )
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
            
            // 全屏图片预览
            if (selectedImageIndex >= 0) {
                ImagePreviewDialog(
                    images = draw.items.map { it.src },
                    initialIndex = selectedImageIndex,
                    sourceRect = sourceRect,  //  [新增] 传递源位置用于展开动画
                    textContent = drawPreviewText,
                    defaultTextVisible = dynamicPreviewTextVisible,
                    onDismiss = { selectedImageIndex = -1 }
                )
            }
        }
        
        //  [新增] Opus 图文动态 (新版格式)
        content?.major?.opus?.let { opus ->
            var selectedImageIndex by remember { mutableIntStateOf(-1) }
            var sourceRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
            val visibleOpusSummaryDesc = remember(opus.summary, opus.pics) {
                opus.summary?.let { summary ->
                    resolveDynamicOpusSummaryDescForImages(
                        text = summary.text,
                        richTextNodes = summary.rich_text_nodes,
                        hasImages = opus.pics.isNotEmpty()
                    )
                }
            }
            val opusPreviewText = remember(author?.name, visibleDynamicDesc?.text, visibleOpusSummaryDesc?.text) {
                val body = visibleDynamicDesc?.text.takeUnless { it.isNullOrBlank() }
                    ?: visibleOpusSummaryDesc?.text.orEmpty()
                ImagePreviewTextContent(
                    headline = author?.name.orEmpty(),
                    body = body
                )
            }
            
            // 显示标题 (如果有)
            opus.title?.let { title ->
                if (title.isNotEmpty()) {
                    AppText(
                        title,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = AppSpacingTokens.Small)
                    )
                }
            }
            
            // 正文已在上方 preferredBodyDesc 渲染；此处不再重复摘要
            
            // 显示图片 (转换为 DrawItem 格式复用现有组件)
            if (fullOpusContentBlocks.isNotEmpty()) {
                val previewImages = remember(opus.pics) { opus.pics.map { it.url } }
                var fullContentSelectedImageIndex by remember { mutableIntStateOf(-1) }
                var fullContentImageIndex = 0
                fullOpusContentBlocks.forEach { block ->
                    when (block) {
                        is OpusContentBlock.Text -> {
                            AppText(
                                text = block.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = AppSpacingTokens.Medium)
                            )
                        }
                        is OpusContentBlock.Image -> {
                            val currentImageIndex = fullContentImageIndex
                            fullContentImageIndex += 1
                            val aspectRatio = remember(block.pic.width, block.pic.height) {
                                if (block.pic.width > 0 && block.pic.height > 0) {
                                    block.pic.width.toFloat() / block.pic.height.toFloat()
                                } else {
                                    null
                                }
                            }
                            AsyncImage(
                                model = block.pic.url,
                                contentDescription = opus.title.orEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (aspectRatio != null) {
                                            Modifier.aspectRatio(aspectRatio)
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clip(AppShapes.container(ContainerLevel.Card))
                                    .clickable(enabled = currentImageIndex in previewImages.indices) {
                                        fullContentSelectedImageIndex = currentImageIndex
                                    },
                                contentScale = ContentScale.FillWidth
                            )
                            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
                        }
                        is OpusContentBlock.LinkCard -> {
                            DynamicOpusLinkCard(
                                card = block.card,
                                modifier = Modifier.padding(bottom = AppSpacingTokens.Medium),
                                onClick = {
                                    when (val action = resolveDynamicOpusLinkCardAction(block.card)) {
                                        is DynamicOpusLinkCardAction.OpenVideo -> onVideoClick(action.videoId)
                                        is DynamicOpusLinkCardAction.OpenDynamicDetail -> onDynamicDetailClick?.invoke(action.dynamicId)
                                        is DynamicOpusLinkCardAction.OpenArticle -> onArticleClick?.invoke(action.articleId, action.title)
                                        is DynamicOpusLinkCardAction.OpenLive -> onLiveClick(
                                            action.roomId,
                                            block.card.title.ifBlank { "直播间" },
                                            author?.name.orEmpty()
                                        )
                                        is DynamicOpusLinkCardAction.OpenUser -> onUserClick(action.mid)
                                        is DynamicOpusLinkCardAction.OpenBangumi -> onBangumiClick(action.seasonId, action.epId)
                                        is DynamicOpusLinkCardAction.OpenExternalUrl -> runCatching {
                                            uriHandler.openUri(action.url)
                                        }
                                        DynamicOpusLinkCardAction.None -> Unit
                                    }
                                }
                            )
                        }
                    }
                }

                if (fullContentSelectedImageIndex >= 0) {
                    ImagePreviewDialog(
                        images = previewImages,
                        initialIndex = fullContentSelectedImageIndex,
                        textContent = opusPreviewText,
                        defaultTextVisible = dynamicPreviewTextVisible,
                        onDismiss = { fullContentSelectedImageIndex = -1 }
                    )
                }
            } else if (opus.pics.isNotEmpty()) {
                val drawItems = opus.pics.map { pic ->
                    DrawItem(
                        src = pic.url,
                        width = pic.width,
                        height = pic.height
                    )
                }
                DrawGridV2(
                    items = drawItems,
                    gifImageLoader = gifImageLoader,
                    maxDisplayImages = resolveDynamicOpusPreviewImageLimit(isDetail),
                    onImageClick = { index, rect ->
                        val action = resolveDynamicCardMediaAction(item, index)
                        if (action is DynamicCardMediaAction.PreviewImages) {
                            selectedImageIndex = action.initialIndex
                            sourceRect = rect
                        }
                    }
                )
                Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
                
                // 全屏图片预览
                if (selectedImageIndex >= 0) {
                    ImagePreviewDialog(
                        images = opus.pics.map { it.url },
                        initialIndex = selectedImageIndex,
                        sourceRect = sourceRect,  //  [新增] 传递源位置用于展开动画
                        textContent = opusPreviewText,
                        defaultTextVisible = dynamicPreviewTextVisible,
                        onDismiss = { selectedImageIndex = -1 }
                    )
                }
            }
        }

        content?.major?.article?.let { article ->
            val articleCovers = remember(article.covers) { resolveArticleCoverUrls(article) }
            if (articleCovers.isNotEmpty()) {
                var selectedImageIndex by remember { mutableIntStateOf(-1) }
                var sourceRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
                val articlePreviewText = remember(author?.name, visibleDynamicDesc?.text, article.title, article.desc) {
                    val body = visibleDynamicDesc?.text
                        .takeUnless { it.isNullOrBlank() }
                        ?: article.desc.ifBlank { article.title }
                    ImagePreviewTextContent(
                        headline = author?.name.orEmpty(),
                        body = body
                    )
                }
                val drawItems = remember(article.covers) { resolveArticleCoverDrawItems(article) }
                DrawGridV2(
                    items = drawItems,
                    gifImageLoader = gifImageLoader,
                    maxDisplayImages = resolveDynamicOpusPreviewImageLimit(isDetail),
                    onImageClick = { index, rect ->
                        when (val action = resolveDynamicCardMediaAction(item, index, isDetail = isDetail)) {
                            is DynamicCardMediaAction.PreviewImages -> {
                                selectedImageIndex = action.initialIndex
                                sourceRect = rect
                            }
                            is DynamicCardMediaAction.OpenDynamicDetail -> {
                                onDynamicDetailClick?.invoke(action.dynamicId)
                            }
                            DynamicCardMediaAction.None -> Unit
                        }
                    }
                )
                Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))

                if (selectedImageIndex >= 0) {
                    ImagePreviewDialog(
                        images = articleCovers,
                        initialIndex = selectedImageIndex,
                        sourceRect = sourceRect,
                        textContent = articlePreviewText,
                        defaultTextVisible = dynamicPreviewTextVisible,
                        onDismiss = { selectedImageIndex = -1 }
                    )
                }
            }
        }
        
        //  [新增] 合集/剧集动态
        content?.major?.ugc_season?.let { season ->
            val seasonArchive = resolveUgcSeasonArchiveFallback(season)
            val playableBvid = resolveUgcSeasonPlayableBvid(season)
            if (seasonArchive != null) {
                VideoCardLarge(
                    archive = seasonArchive,
                    publishTs = author?.pub_ts ?: 0L,
                    onClick = {
                        playableBvid?.let(onVideoClick)
                            ?: onDynamicDetailClick?.invoke(item.id_str)
                    },
                    isCollection = true,
                    collectionTitle = season.title,
                    sharedElementKey = com.android.purebilibili.core.ui.transition.videoPlayerSharedElementKey(
                        seasonArchive.bvid
                    )
                )
                Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
            } else {
                AppText(
                     "合集：${season.title}", 
                     fontWeight = FontWeight.Bold,
                     color = MaterialTheme.colorScheme.primary
                )
                 Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
            }
        }
        
        //  直播推荐动态
        content?.major?.live_rcmd?.let { liveRcmd ->
            LiveCard(
                liveRcmd = liveRcmd,
                onLiveClick = { roomId, title, uname ->
                    dispatchDynamicCardPrimaryAction(
                        action = DynamicCardPrimaryAction.OpenLive(roomId, title, uname),
                        onVideoClick = onVideoClick,
                        onBangumiClick = onBangumiClick,
                        onArticleClick = onArticleClick,
                        onDynamicDetailClick = onDynamicDetailClick,
                        onUserClick = onUserClick,
                        onLiveClick = onLiveClick
                    )
                }
            )
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
        }
        
        //  转发动态 - 嵌套显示原始内容
        if (type == DynamicType.FORWARD && item.orig != null) {
            ForwardedContent(
                orig = item.orig,
                onVideoClick = onVideoClick,
                onBangumiClick = onBangumiClick,
                onUserClick = onUserClick,
                onDynamicDetailClick = onDynamicDetailClick,
                gifImageLoader = gifImageLoader,
                defaultPreviewTextVisible = dynamicPreviewTextVisible
            )
            Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
        }
        
        //  [修复] 底部操作栏：转发、评论、点赞 - 始终显示
        val statModule = stat ?: DynamicStatModule()  // 使用默认值避免按钮消失
        val actionButtonWeight = resolveDynamicActionButtonSlotWeight()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacingTokens.ExtraSmall),
            horizontalArrangement = Arrangement.spacedBy(resolveDynamicActionButtonSpacing())
        ) {
            // 转发按钮
            ActionButton(
                count = (statModule.forward.count + forwardCountDelta).coerceAtLeast(0),
                label = "转发",
                enabled = !statModule.forward.forbidden,
                onClick = { onRepostClick(item.id_str) },
                modifier = Modifier.weight(actionButtonWeight)
            )
            
            // 评论按钮
            ActionButton(
                count = statModule.comment.count,
                label = "评论",
                enabled = !statModule.comment.forbidden,
                onClick = { onCommentClick(item.id_str) },
                modifier = Modifier.weight(actionButtonWeight)
            )
            
            // 点赞按钮
            ActionButton(
                count = statModule.like.count,
                label = "点赞",
                isActive = isLiked,
                onClick = { onLikeClick(item.id_str) },
                modifier = Modifier.weight(actionButtonWeight)
            )
        }
        
        //  相关动态折叠条（module_fold：如“展开3条相关动态”，点击进动态详情）
        if (!isDetail && onDynamicDetailClick != null) {
            val foldStatement = resolveDynamicFoldStatement(item.modules.module_fold)
            if (foldStatement != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDynamicDetailClick(item.id_str) }
                        .padding(vertical = AppSpacingTokens.Small),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val foldUsers = item.modules.module_fold?.users.orEmpty().take(3)
                    if (foldUsers.isNotEmpty()) {
                        Box(modifier = Modifier.height(22.dp)) {
                            foldUsers.forEachIndexed { index, user ->
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(user.face.let { if (it.startsWith("http://")) it.replace("http://", "https://") else it })
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .offset(x = (index * 14).dp)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .border(AppSurfaceTokens.OutlineWidth, AppSurfaceTokens.surface(), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                    }
                    AppText(
                        foldStatement,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(AppSpacingTokens.ExtraSmall))
                    AppIcon(
                        rememberAppChevronDownIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(AppSpacingTokens.Small + AppSpacingTokens.ExtraSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        }

        if (!isDetail) {
            AppHorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f),
                thickness = AppSpacingTokens.Micro * 0.35f
            )
        }
    }
}

/**
 *  富文本内容（支持表情、@提及、话题高亮）
 *  解析 API 返回的 rich_text_nodes 来正确渲染表情图片；
 *  若节点仅为纯文本短码，则用表情面板缓存补全图片。
 *
 *  @param onBlankTap 点击非 @/链接区域时回调（转发原文点击跳原动态）
 */
@Composable
fun RichTextContent(
    desc: DynamicDesc,
    onUserClick: (Long) -> Unit,
    onBlankTap: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var catalogEmoteMap by remember { mutableStateOf(DynamicEmoteCatalog.snapshot()) }
    LaunchedEffect(Unit) {
        catalogEmoteMap = DynamicEmoteCatalog.ensureLoaded()
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val richText = remember(desc, primaryColor, textColor, catalogEmoteMap) {
        buildDynamicRichText(
            desc = desc,
            primaryColor = primaryColor,
            textColor = textColor,
            extraEmoteUrlMap = catalogEmoteMap
        )
    }
    val annotatedText = richText.annotatedString

    // 仅对实际用到的表情 id 建 InlineContent，避免整包表情占内存
    val inlineContent = remember(richText.emojiUrlById) {
        richText.emojiUrlById.mapValues { (_, iconUrl) ->
            InlineTextContent(
                Placeholder(
                    width = 1.4.em,
                    height = 1.4.em,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(iconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    val copyText = remember(desc.rich_text_nodes, desc.text) {
        val richNodeText = resolveDynamicRichTextNodeDisplayText(desc.rich_text_nodes)
        richNodeText.ifBlank { desc.text }.trim()
    }
    var showCopySelectionDialog by remember(copyText) { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    AppText(
        text = annotatedText,
        inlineContent = inlineContent,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
        color = textColor,
        onTextLayout = { textLayoutResult = it },
        modifier = Modifier.pointerInput(copyText, annotatedText, onBlankTap) {
            detectTapGestures(
                onLongPress = {
                    if (copyText.isNotEmpty()) {
                        showCopySelectionDialog = true
                    }
                },
                onTap = { offset ->
                    val layoutResult = textLayoutResult ?: return@detectTapGestures
                    val position = layoutResult.getOffsetForPosition(offset)
                    val searchStart = maxOf(0, position - 1)
                    val searchEnd = minOf(annotatedText.length, position + 1)

                    annotatedText.getStringAnnotations(
                        tag = DYNAMIC_RICH_TEXT_USER_TAG,
                        start = searchStart,
                        end = searchEnd
                    ).firstOrNull()?.let { annotation ->
                        annotation.item.toLongOrNull()
                            ?.takeIf { it > 0L }
                            ?.let(onUserClick)
                        return@detectTapGestures
                    }

                    val urlAnnotation = annotatedText.getStringAnnotations(
                        tag = DYNAMIC_RICH_TEXT_URL_TAG,
                        start = searchStart,
                        end = searchEnd
                    ).firstOrNull()

                    if (urlAnnotation != null) {
                        scope.launch {
                            when (resolveDynamicRichTextOpenMode(urlAnnotation.item)) {
                                DynamicRichTextOpenMode.IN_APP -> {
                                    val inAppIntent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(urlAnnotation.item)
                                    ).setPackage(context.packageName)
                                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    val launchedInApp = runCatching {
                                        context.startActivity(inAppIntent)
                                    }.isSuccess
                                    if (!launchedInApp) {
                                        openDynamicRichTextLinkExternally(
                                            context,
                                            urlAnnotation.item,
                                            uriHandler
                                        )
                                    }
                                }

                                DynamicRichTextOpenMode.EXTERNAL -> {
                                    openDynamicRichTextLinkExternally(
                                        context,
                                        urlAnnotation.item,
                                        uriHandler
                                    )
                                }

                                null -> Unit
                            }
                        }
                        return@detectTapGestures
                    }

                    // 非 @ / 链接：交给外层（例如转发卡片打开原动态）
                    onBlankTap?.invoke()
                }
            )
        }
    )
    if (showCopySelectionDialog) {
        CopySelectionDialog(
            text = copyText,
            title = "选择动态内容",
            onDismiss = { showCopySelectionDialog = false }
        )
    }
}

private fun openDynamicRichTextLinkExternally(
    context: android.content.Context,
    url: String,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    val externalIntent = android.content.Intent(
        android.content.Intent.ACTION_VIEW,
        android.net.Uri.parse(url)
    ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

    val packageManager = context.packageManager
    val externalPackage = packageManager.queryIntentActivities(
        externalIntent,
        android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
    ).firstOrNull { it.activityInfo?.packageName != context.packageName }
        ?.activityInfo
        ?.packageName

    val launchedExternally = if (!externalPackage.isNullOrBlank()) {
        runCatching {
            context.startActivity(externalIntent.setPackage(externalPackage))
        }.isSuccess
    } else {
        false
    }

    if (!launchedExternally) {
        runCatching { uriHandler.openUri(url) }
    }
}

/**
 *  紧凑列表卡片 - 单行显示
 */
@Composable
fun DynamicCardCompact(
    item: DynamicItem,
    onVideoClick: (String) -> Unit,
    onUserClick: (Long) -> Unit
) {
    val author = item.modules.module_author
    val content = item.modules.module_dynamic
    val stat = item.modules.module_stat
    
    // 获取内容预览文本
    val previewText = content?.desc?.text?.take(50) 
        ?: content?.major?.archive?.title 
        ?: "动态"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // 如果有视频则跳转视频
                content?.major?.archive
                    ?.let(::resolveArchivePlayableBvid)
                    ?.let(onVideoClick)
                    ?: author?.let { onUserClick(it.mid) }
            }
            .padding(horizontal = AppSpacingTokens.Large, vertical = AppSpacingTokens.Medium),  //  优化间距
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        if (author != null) {
            Box(
                modifier = Modifier
                    .size(AppChromeSizeTokens.MinimumTouchTarget)
                    .clip(CircleShape)
                    .semantics { contentDescription = "查看${author.name}的个人主页" }
                    .clickable(enabled = author.mid > 0) { onUserClick(author.mid) },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(author.face.let { if (it.startsWith("http://")) it.replace("http://", "https://") else it })
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Medium)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
        }
        
        // 内容区
        Column(modifier = Modifier.weight(1f)) {
            // 用户名 + 时间
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText(
                    author?.name ?: "",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                AppText(
                    author?.let {
                        resolveDynamicAuthorTimeText(
                            pubTime = it.pub_time,
                            pubTs = it.pub_ts
                        )
                    }.orEmpty(),
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraSmall))
            
            // 内容预览
            AppText(
                previewText,
                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        
        // 封面缩略图（如果有视频）
        content?.major?.archive?.let { archive ->
            Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
            AsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(archive.cover.let { if (it.startsWith("http://")) it.replace("http://", "https://") else it })
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(width = AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.DoubleExtraLarge, height = AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Micro)
                    .clip(AppShapes.container(ContainerLevel.Chip)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
