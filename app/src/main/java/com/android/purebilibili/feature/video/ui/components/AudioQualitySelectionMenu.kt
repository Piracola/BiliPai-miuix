package com.android.purebilibili.feature.video.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.purebilibili.feature.video.playback.audio.AudioQualityOption
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.*
import io.github.alexzhirkevich.cupertino.icons.outlined.*

private val HiResGold = Color(0xFFFFD36A)
private val DolbyBlue = Color(0xFF8DCDFF)

@Composable
fun HiResBadge(
    modifier: Modifier = Modifier
) {
    AudioFormatBadge(
        text = "Hi-Res",
        accentColor = HiResGold,
        backgroundColor = Color(0xFF332A14),
        modifier = modifier
    )
}

@Composable
fun DolbyBadge(
    modifier: Modifier = Modifier
) {
    AudioFormatBadge(
        text = "DOLBY",
        accentColor = DolbyBlue,
        backgroundColor = Color(0xFF142A3A),
        modifier = modifier
    )
}

@Composable
private fun AudioFormatBadge(
    text: String,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = backgroundColor.copy(alpha = 0.9f),
        contentColor = accentColor,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(0.75.dp, accentColor.copy(alpha = 0.9f))
    ) {
        Text(
            text = text,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 9.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun AudioQualitySelectionMenu(
    options: List<AudioQualityOption>,
    requestedAudioQuality: Int,
    onAudioQualitySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 280.dp)
                .heightIn(max = 400.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            color = Color(0xFF2B2B2B),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "音质选择",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                options.forEach { option ->
                    val isSelected = option.preferenceId == requestedAudioQuality
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable(enabled = !isSelected) {
                                onAudioQualitySelected(option.preferenceId)
                            }
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = option.label,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White.copy(alpha = 0.9f)
                            },
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (option.isHiRes) {
                            Spacer(modifier = Modifier.width(8.dp))
                            HiResBadge()
                        }
                        if (option.isDolby) {
                            Spacer(modifier = Modifier.width(8.dp))
                            DolbyBadge()
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (isSelected) {
                            Icon(
                                imageVector = CupertinoIcons.Default.Checkmark,
                                contentDescription = "当前音质",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioQualitySelectionMenuDialog(
    options: List<AudioQualityOption>,
    requestedAudioQuality: Int,
    onAudioQualitySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AudioQualitySelectionMenu(
            options = options,
            requestedAudioQuality = requestedAudioQuality,
            onAudioQualitySelected = onAudioQualitySelected,
            onDismiss = onDismiss
        )
    }
}
