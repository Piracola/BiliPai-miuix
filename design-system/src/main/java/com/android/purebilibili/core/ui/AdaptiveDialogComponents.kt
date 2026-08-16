package com.android.purebilibili.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class DialogActionLayoutPolicy(
    val expandToContainer: Boolean
)

/**
 * 历史 iOS 的全宽铺满操作区行为已随单向迁移删除（iOS → MIUIX 不再 expandToContainer）。
 */
fun resolveDialogActionLayoutPolicy(): DialogActionLayoutPolicy {
    return DialogActionLayoutPolicy(
        expandToContainer = false
    )
}

/**
 * Style-neutral Alert Dialog backed by the adaptive renderer.
 */
@Composable
internal fun AdaptiveAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    shape: Shape? = null,
    containerColor: Color? = null,
    tonalElevation: Dp? = null,
    properties: DialogProperties = DialogProperties()
) {
    val contentLayout = resolveAppCompactContentDialogLayoutPolicy()
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = resolveAppContentDialogProperties(
            base = properties,
            usePlatformDefaultWidth = contentLayout.usePlatformDefaultWidth,
        ),
    ) {
        Surface(
            modifier = modifier.appContentDialogWidth(policy = contentLayout),
            shape = shape ?: MaterialTheme.shapes.extraLarge,
            color = containerColor ?: AppSurfaceTokens.cardContainer(),
            tonalElevation = tonalElevation ?: 6.dp,
        ) {
            MiuixAlertDialogBody(
                icon = icon,
                title = title,
                text = text,
                confirmButton = confirmButton,
                dismissButton = dismissButton,
            )
        }
    }
}

@Composable
private fun MiuixAlertDialogBody(
    icon: @Composable (() -> Unit)?,
    title: @Composable (() -> Unit)?,
    text: @Composable (() -> Unit)?,
    confirmButton: @Composable (() -> Unit)?,
    dismissButton: @Composable (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
        }
        if (title != null) {
            Box(
                modifier = Modifier.padding(
                    top = if (icon != null) 12.dp else 8.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
                contentAlignment = Alignment.Center
            ) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    title()
                }
            }
        }
        if (text != null) {
            Box(
                modifier = Modifier.padding(
                    top = if (title != null) 8.dp else 12.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 12.dp
                ),
                contentAlignment = Alignment.Center
            ) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    text()
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (dismissButton != null) {
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    dismissButton()
                }
            }
            if (confirmButton != null) {
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    confirmButton()
                }
            }
        }
    }
}

/**
 * A helper for buttons inside the adaptive alert dialog if you need absolute control,
 * basically a wrapper that removes TextButton padding issues if standard TextButton is used.
 * But actually providing TextStyle above might be enough for simple Text() children.
 * If user passes TextButton, we might need to conform it.
 */
@Composable
internal fun AdaptiveDialogAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val layoutPolicy = resolveDialogActionLayoutPolicy()
    Box(
        modifier = modifier
            .then(
                if (layoutPolicy.expandToContainer) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.defaultMinSize(minWidth = 64.dp, minHeight = 40.dp)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
