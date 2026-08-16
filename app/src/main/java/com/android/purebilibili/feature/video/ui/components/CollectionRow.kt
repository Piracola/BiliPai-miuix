// 文件路径: feature/video/ui/components/CollectionRow.kt
package com.android.purebilibili.feature.video.ui.components

import com.android.purebilibili.core.store.collection.CollectionSortMode
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.rememberAppShareIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.data.model.response.UgcSeason
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Folder
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.feature.video.ui.VideoDetailShapes

/**
 *  视频合集展示行
 * 显示合集名称、当前集数/总集数
 */
@Composable
fun CollectionRow(
    ugcSeason: UgcSeason,
    currentBvid: String,
    currentCid: Long = 0L,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shareIcon = rememberAppShareIcon()
    val collectionSubscriptionId = remember(ugcSeason) { resolveCollectionSubscriptionId(ugcSeason) }
    val allEpisodes = remember(ugcSeason.sections) { ugcSeason.sections.flatMap { it.episodes } }
    val currentAid = remember(allEpisodes, currentBvid, currentCid) {
        resolveCurrentUgcEpisodeAid(
            episodes = allEpisodes,
            currentBvid = currentBvid,
            currentCid = currentCid
        )
    }
    val sortMode by SettingsManager
        .getCollectionSortMode(context, collectionSubscriptionId)
        .collectAsStateWithLifecycle(initialValue = CollectionSortMode.ASCENDING
        )

    // 计算当前视频在合集中的位置
    val currentIndex = resolveCurrentUgcEpisodeIndex(
        episodes = allEpisodes,
        currentBvid = currentBvid,
        currentCid = currentCid
    )
    val currentPosition = if (currentIndex >= 0) currentIndex + 1 else 0
    val totalCount = allEpisodes.size.takeIf { it > 0 } ?: ugcSeason.ep_count
    
    AppSurface(
        modifier = modifier
            .fillMaxWidth(),
        shape = androidx.compose.ui.graphics.RectangleShape,
        color = Color.Transparent  // 透明背景，与周围统一
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //  合集图标
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(VideoDetailShapes.compactIcon())
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            //  合集信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppText(
                        text = "合集",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AppText(
                        text = ugcSeason.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentPosition > 0 && totalCount > 0) {
                        AppText(
                            text = "$currentPosition/$totalCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    AppText(
                        text = resolveCollectionSortLabel(sortMode),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            CollectionSubscriptionButton(
                collectionId = collectionSubscriptionId,
                currentBvid = currentBvid,
                currentAid = currentAid,
                fontSize = 12.sp
            )

            //  分享按钮
            AppIconButton(
                onClick = {
                    val shareUrl = "https://space.bilibili.com/${ugcSeason.mid}/lists/${ugcSeason.id}?type=season"
                    val shareText = "${ugcSeason.title}\n$shareUrl"
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "分享合集"))
                },
                modifier = Modifier.size(28.dp)
            ) {
                AppIcon(
                    shareIcon,
                    contentDescription = "分享合集",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            //  右侧箭头
            AppIcon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "查看合集",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
