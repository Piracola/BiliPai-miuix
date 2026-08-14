package com.android.purebilibili.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.ui.AppModalBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import com.android.purebilibili.core.ui.components.AppFilterChip
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppTextButton
import com.android.purebilibili.data.repository.SearchDuration
import com.android.purebilibili.data.repository.SearchOrder
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * BiliPai-style video filter chrome:
 * horizontal order chips + filter icon that opens a bottom sheet
 * with publish time / duration / zone selectors.
 */
@Composable
fun SearchVideoFilterBar(
    currentOrder: SearchOrder,
    currentDurations: Set<SearchDuration>,
    currentVideoTid: Int,
    currentPubTimeType: SearchVideoPubTimeType,
    currentPubBegin: Long?,
    currentPubEnd: Long?,
    onOrderChange: (SearchOrder) -> Unit,
    onDurationSelect: (SearchDuration) -> Unit,
    onVideoTidChange: (Int) -> Unit,
    onPubTimeTypeChange: (SearchVideoPubTimeType) -> Unit,
    onCustomPubTimeRange: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val filterActive = hasActiveSearchVideoFilters(
        durations = currentDurations,
        videoTid = currentVideoTid,
        pubTimeType = currentPubTimeType
    )
    val orderOptions = remember { resolveSearchVideoOrderOptions() }
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            orderOptions.forEach { order ->
                val selected = order == currentOrder
                AppText(
                    text = resolveSearchOrderChipLabel(order),
                    modifier = Modifier
                        .clickable { onOrderChange(order) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    color = if (selected) primary else outline,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
        VerticalDivider(
            modifier = Modifier
                .height(18.dp)
                .padding(horizontal = 2.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
        // Always circular Material ripple. Miuix IconButton indication is square;
        // clip + unbounded radius keeps the wave circular under every preset.
        val filterInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = filterInteraction,
                    indication = ripple(
                        bounded = false,
                        radius = 20.dp
                    ),
                    role = Role.Button,
                    onClick = { showFilterSheet = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "筛选",
                tint = if (filterActive) primary else outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showFilterSheet) {
        SearchVideoFilterSheetHost(
            currentDurations = currentDurations,
            currentVideoTid = currentVideoTid,
            currentPubTimeType = currentPubTimeType,
            currentPubBegin = currentPubBegin,
            currentPubEnd = currentPubEnd,
            onDismiss = { showFilterSheet = false },
            onDurationSelect = {
                onDurationSelect(it)
                showFilterSheet = false
            },
            onVideoTidChange = {
                onVideoTidChange(it)
                showFilterSheet = false
            },
            onPubTimeTypeChange = {
                onPubTimeTypeChange(it)
                showFilterSheet = false
            },
            onCustomPubTimeRange = { begin, end ->
                onCustomPubTimeRange(begin, end)
                showFilterSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchVideoFilterSheetHost(
    currentDurations: Set<SearchDuration>,
    currentVideoTid: Int,
    currentPubTimeType: SearchVideoPubTimeType,
    currentPubBegin: Long?,
    currentPubEnd: Long?,
    onDismiss: () -> Unit,
    onDurationSelect: (SearchDuration) -> Unit,
    onVideoTidChange: (Int) -> Unit,
    onPubTimeTypeChange: (SearchVideoPubTimeType) -> Unit,
    onCustomPubTimeRange: (Long, Long) -> Unit
) {
    // Host contract from stage 3: the single Miuix theme resolves to the Miuix
    // OverlayBottomSheet host. Never copy the host decision here.
    val sheetContent: @Composable () -> Unit = {
        SearchVideoFilterSheetContent(
            currentDurations = currentDurations,
            currentVideoTid = currentVideoTid,
            currentPubTimeType = currentPubTimeType,
            currentPubBegin = currentPubBegin,
            currentPubEnd = currentPubEnd,
            onDurationSelect = onDurationSelect,
            onVideoTidChange = onVideoTidChange,
            onPubTimeTypeChange = onPubTimeTypeChange,
            onCustomPubTimeRange = onCustomPubTimeRange
        )
    }
    OverlayBottomSheet(
        show = true,
        title = "筛选",
        onDismissRequest = onDismiss,
        content = sheetContent
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SearchVideoFilterSheetContent(
    currentDurations: Set<SearchDuration>,
    currentVideoTid: Int,
    currentPubTimeType: SearchVideoPubTimeType,
    currentPubBegin: Long?,
    currentPubEnd: Long?,
    onDurationSelect: (SearchDuration) -> Unit,
    onVideoTidChange: (Int) -> Unit,
    onPubTimeTypeChange: (SearchVideoPubTimeType) -> Unit,
    onCustomPubTimeRange: (Long, Long) -> Unit
) {
    val selectedDuration = resolveSelectedSearchDuration(currentDurations)
    val zoneOptions = remember { resolveSearchVideoZoneOptions() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA) }
    var showBeginPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val defaultBegin = currentPubBegin
        ?: TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    val defaultEnd = currentPubEnd
        ?: TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 32.dp)
    ) {
        SearchFilterSectionTitle(text = "发布时间")
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                SearchVideoPubTimeType.ALL,
                SearchVideoPubTimeType.DAY,
                SearchVideoPubTimeType.WEEK,
                SearchVideoPubTimeType.HALF_YEAR
            ).forEach { type ->
                SearchFilterSelectableChip(
                    label = type.label,
                    selected = currentPubTimeType == type,
                    onClick = { onPubTimeTypeChange(type) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchFilterSelectableChip(
                label = dateFormat.format(Date(TimeUnit.SECONDS.toMillis(defaultBegin))),
                selected = currentPubTimeType == SearchVideoPubTimeType.CUSTOM,
                onClick = { showBeginPicker = true },
                modifier = Modifier.weight(1f),
                center = true
            )
            AppText(
                text = "至",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SearchFilterSelectableChip(
                label = dateFormat.format(Date(TimeUnit.SECONDS.toMillis(defaultEnd))),
                selected = currentPubTimeType == SearchVideoPubTimeType.CUSTOM,
                onClick = { showEndPicker = true },
                modifier = Modifier.weight(1f),
                center = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        SearchFilterSectionTitle(text = "内容时长")
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            resolveSearchVideoDurationOptions().forEach { duration ->
                SearchFilterSelectableChip(
                    label = resolveSearchDurationChipLabel(duration),
                    selected = selectedDuration == duration,
                    onClick = { onDurationSelect(duration) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        SearchFilterSectionTitle(text = "内容分区")
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            zoneOptions.forEach { zone ->
                SearchFilterSelectableChip(
                    label = zone.label,
                    selected = currentVideoTid == zone.tid,
                    onClick = { onVideoTidChange(zone.tid) }
                )
            }
        }
    }

    if (showBeginPicker) {
        SearchDatePickerDialog(
            initialEpochMillis = TimeUnit.SECONDS.toMillis(defaultBegin),
            onDismiss = { showBeginPicker = false },
            onConfirm = { millis ->
                val begin = TimeUnit.MILLISECONDS.toSeconds(millis)
                val end = currentPubEnd ?: defaultEnd
                onCustomPubTimeRange(begin, end)
                showBeginPicker = false
            }
        )
    }
    if (showEndPicker) {
        SearchDatePickerDialog(
            initialEpochMillis = TimeUnit.SECONDS.toMillis(defaultEnd),
            onDismiss = { showEndPicker = false },
            onConfirm = { millis ->
                val end = TimeUnit.MILLISECONDS.toSeconds(millis)
                val begin = currentPubBegin ?: defaultBegin
                onCustomPubTimeRange(begin, end)
                showEndPicker = false
            }
        )
    }
}

@Composable
private fun SearchFilterSectionTitle(
    text: String
) {
    AppText(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp)
    )
}

@Composable
private fun SearchFilterSelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    center: Boolean = false
) {
    AppFilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = {
            AppText(
                text = label,
                fontSize = 13.sp,
                maxLines = 1,
                textAlign = if (center) TextAlign.Center else TextAlign.Start,
                modifier = if (center) Modifier.fillMaxWidth() else Modifier
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.Transparent,
            selectedBorderColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchDatePickerDialog(
    initialEpochMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialEpochMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppTextButton(
                onClick = {
                    val selected = state.selectedDateMillis ?: return@AppTextButton
                    onConfirm(selected)
                }
            ) {
                AppText("确定")
            }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) {
                AppText("取消")
            }
        }
    ) {
        DatePicker(state = state)
    }
}
