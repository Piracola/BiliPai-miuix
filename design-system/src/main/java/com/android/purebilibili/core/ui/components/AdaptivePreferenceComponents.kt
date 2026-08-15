package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import com.android.purebilibili.core.theme.LocalCornerRadiusScale
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens
import com.android.purebilibili.core.ui.resolveCompactCapsuleChromeSpec
import com.android.purebilibili.core.theme.iOSCornerRadius
import com.android.purebilibili.core.ui.LocalAppListItemStyle
import com.android.purebilibili.core.ui.LocalAppThemeConfig
import com.android.purebilibili.core.ui.LocalGlobalWallpaperBackdropVisible
import com.android.purebilibili.core.ui.adaptiveSquircleBackground
import com.android.purebilibili.core.ui.AppListItemStyle
import com.android.purebilibili.core.ui.rememberResolvedAppListItemStyle
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import com.android.purebilibili.core.ui.AppSurfaceTokens
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import top.yukonga.miuix.kmp.preference.SliderPreference as MiuixSliderPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference as MiuixSwitchPreference
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults as MiuixTextFieldDefaults
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.theme.MiuixTheme

private object NoOpHapticFeedback : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
}

/**
 * Trailing preference value layout (version / channel / short detail on the right).
 *
 * Previously hard-capped at 120.dp + single-line ellipsis, which clipped common Chinese
 * labels like「开源约定与官方渠道」and version strings on phone-width settings rows.
 * Shared by every AdaptivePreferenceContent renderer (MD3 / Miuix / fallback).
 */
internal const val APP_PREFERENCE_VALUE_MAX_WIDTH_DP = 200
internal const val APP_PREFERENCE_VALUE_MAX_LINES = 2

internal fun Modifier.appPreferenceValueTextModifier(): Modifier =
    this.widthIn(max = APP_PREFERENCE_VALUE_MAX_WIDTH_DP.dp)

// ═══════════════════════════════════════════════════
//  Common iOS List Components (Reused across Settings, Profile, etc.)
// ═══════════════════════════════════════════════════

data class AdaptiveListComponentVisualSpec(
    val sectionStartPaddingDp: Int,
    val groupCornerRadiusDp: Int,
    val groupTonalElevationDp: Int,
    val iconCornerRadiusDp: Int,
    val iconContainerSizeDp: Int,
    val iconGlyphSizeDp: Int,
    val iconBackgroundAlpha: Float,
    val gridCornerRadiusDp: Int,
    val searchBarCornerRadiusDp: Int,
    val searchBarHeightDp: Int,
    val dividerThicknessDp: Float,
    val dividerStartIndentDp: Int
)

enum class AppPreferenceIconTreatment {
    TONAL,
    FILLED,
}

val LocalAppPreferenceIconTreatment = staticCompositionLocalOf {
    AppPreferenceIconTreatment.TONAL
}

val LocalAppPreferenceGroupPresentation = staticCompositionLocalOf {
    AppPreferenceGroupPresentation.CARD
}

data class AdaptiveListRowVisualSpec(
    val insideHorizontalPaddingDp: Int,
    val insideVerticalPaddingDp: Int,
    val trailingIconSizeDp: Int,
    val trailingSpacingDp: Int,
    val minTouchTargetHeightDp: Int
)

/**
 * Semantic list capabilities consumed by feature screens without exposing the active UI style.
 */
data class AdaptiveListVisualCapabilities(
    val componentSpec: AdaptiveListComponentVisualSpec,
    val rowSpec: AdaptiveListRowVisualSpec,
    val showExplicitActionChevron: Boolean,
)

internal fun resolveAdaptiveListComponentVisualSpec(): AdaptiveListComponentVisualSpec {
    val chromeTokens = resolveAndroidNativeChromeTokens()
    val compactChrome = resolveCompactCapsuleChromeSpec()
    return AdaptiveListComponentVisualSpec(
        sectionStartPaddingDp = chromeTokens.denseHorizontalSpacingDp,
        groupCornerRadiusDp = chromeTokens.containerCornerRadiusDp,
        groupTonalElevationDp = chromeTokens.tonalSurfaceElevationDp,
        iconCornerRadiusDp = 10,
        iconContainerSizeDp = 38,
        iconGlyphSizeDp = 20,
        iconBackgroundAlpha = chromeTokens.selectedContainerAlpha,
        gridCornerRadiusDp = chromeTokens.containerCornerRadiusDp,
        searchBarCornerRadiusDp = compactChrome.primaryCornerRadiusDp,
        searchBarHeightDp = compactChrome.primaryHeightDp,
        dividerThicknessDp = 0f,
        dividerStartIndentDp = chromeTokens.denseHorizontalSpacingDp
    )
}

internal fun resolveAdaptiveListRowVisualSpec(): AdaptiveListRowVisualSpec {
    val chromeTokens = resolveAndroidNativeChromeTokens()
    return AdaptiveListRowVisualSpec(
        insideHorizontalPaddingDp = 16,
        insideVerticalPaddingDp = 14,
        trailingIconSizeDp = 14,
        trailingSpacingDp = 6,
        minTouchTargetHeightDp = chromeTokens.rowMinTouchTargetDp
    )
}

internal fun resolveAdaptiveListVisualCapabilities(): AdaptiveListVisualCapabilities = AdaptiveListVisualCapabilities(
    componentSpec = resolveAdaptiveListComponentVisualSpec(),
    rowSpec = resolveAdaptiveListRowVisualSpec(),
    showExplicitActionChevron = !shouldUseNativeMiuixSearchBar(),
)

@Composable
fun rememberAdaptiveListVisualCapabilities(): AdaptiveListVisualCapabilities {
    return remember {
        resolveAdaptiveListVisualCapabilities()
    }
}

internal fun resolveAdaptiveGroupContainerColor(
    colorScheme: ColorScheme,
    globalWallpaperVisible: Boolean = false
): Color {
    val resolvedColor = colorScheme.surfaceContainer
    return resolveGlobalWallpaperListContainerColor(
        containerColor = resolvedColor,
        colorScheme = colorScheme,
        globalWallpaperVisible = globalWallpaperVisible,
        targetAlpha = 0.62f
    )
}

internal fun resolveAdaptiveSearchBarContainerColor(
    colorScheme: ColorScheme,
    globalWallpaperVisible: Boolean = false
): Color {
    val resolvedColor = colorScheme.surfaceContainer
    return resolveGlobalWallpaperListContainerColor(
        containerColor = resolvedColor,
        colorScheme = colorScheme,
        globalWallpaperVisible = globalWallpaperVisible,
        targetAlpha = 0.48f
    )
}

internal fun shouldUseNativeMiuixSearchBar(): Boolean = true

internal fun resolveGlobalWallpaperListContainerColor(
    containerColor: Color,
    colorScheme: ColorScheme,
    globalWallpaperVisible: Boolean,
    targetAlpha: Float
): Color {
    if (!globalWallpaperVisible || containerColor.alpha == 0f) return containerColor
    if (!isDefaultListContainerColor(containerColor, colorScheme)) return containerColor
    val adjustedAlpha = if (colorScheme.background.luminance() > 0.5f) {
        targetAlpha
    } else {
        (targetAlpha + 0.12f).coerceAtMost(0.78f)
    }
    return containerColor.copy(alpha = containerColor.alpha.coerceAtMost(adjustedAlpha))
}

private fun isDefaultListContainerColor(
    color: Color,
    colorScheme: ColorScheme
): Boolean {
    val opaqueColor = color.copy(alpha = 1f)
    return opaqueColor == colorScheme.background.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surface.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surfaceVariant.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surfaceContainer.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surfaceContainerLow.copy(alpha = 1f) ||
        opaqueColor == colorScheme.surfaceContainerHigh.copy(alpha = 1f)
}

internal fun resolveAdaptiveSemanticIconTint(
    iconTint: Color,
): Color = iconTint

@Suppress("UNUSED_PARAMETER")
internal fun resolveAdaptivePreferenceIconContainerColor(
    iconTint: Color,
    semanticTint: Color,
    treatment: AppPreferenceIconTreatment,
): Color = semanticTint

internal fun resolveAdaptivePreferenceIconContentColor(
    containerColor: Color,
    colorScheme: ColorScheme,
): Color {
    if (containerColor == Color.Unspecified) return Color.Unspecified
    val opaqueContainer = containerColor.copy(alpha = 1f)
    return when (opaqueContainer) {
        colorScheme.primary.copy(alpha = 1f) -> colorScheme.onPrimary
        colorScheme.secondary.copy(alpha = 1f) -> colorScheme.onSecondary
        colorScheme.tertiary.copy(alpha = 1f) -> colorScheme.onTertiary
        colorScheme.error.copy(alpha = 1f) -> colorScheme.onError
        colorScheme.primaryContainer.copy(alpha = 1f) -> colorScheme.onPrimaryContainer
        colorScheme.secondaryContainer.copy(alpha = 1f) -> colorScheme.onSecondaryContainer
        colorScheme.tertiaryContainer.copy(alpha = 1f) -> colorScheme.onTertiaryContainer
        colorScheme.errorContainer.copy(alpha = 1f) -> colorScheme.onErrorContainer
        else -> if (opaqueContainer.luminance() >= 0.72f) Color.Black else Color.White
    }
}

internal fun resolveAdaptivePreferenceIconGlyphColor(
    treatment: AppPreferenceIconTreatment,
    containerContentColor: Color,
    semanticIconColor: Color,
): Color = if (treatment == AppPreferenceIconTreatment.FILLED) {
    containerContentColor
} else {
    semanticIconColor
}

internal fun resolveAdaptivePreferenceIconBackgroundAlpha(
    treatment: AppPreferenceIconTreatment,
    tonalAlpha: Float,
): Float = if (treatment == AppPreferenceIconTreatment.FILLED) 1f else tonalAlpha

@Composable
fun rememberAdaptiveSemanticIconTint(
    iconTint: Color,
): Color {
    return remember(iconTint) {
        resolveAdaptiveSemanticIconTint(iconTint = iconTint)
    }
}

/**
 * 与 [AdaptivePreferenceContent] 图标最终呈现一致的颜色：保留传入的多彩/语义色。
 * 用于无容器图标（如 WindowSpinnerPreference 的 startAction、发布渠道卡片）。
 */
@Composable
fun rememberAdaptivePreferenceIconTint(
    iconTint: Color,
): Color {
    return remember(iconTint) {
        iconTint
    }
}

@Composable
fun rememberAdaptivePreferenceIconContentColor(
    containerColor: Color,
): Color {
    val colorScheme = MaterialTheme.colorScheme
    return remember(containerColor, colorScheme) {
        resolveAdaptivePreferenceIconContentColor(containerColor, colorScheme)
    }
}

@Composable
fun rememberAdaptivePreferenceIconContainerColor(
    iconTint: Color,
): Color {
    val treatment = LocalAppPreferenceIconTreatment.current
    val semanticTint = rememberAdaptiveSemanticIconTint(iconTint)
    return remember(iconTint, semanticTint, treatment) {
        resolveAdaptivePreferenceIconContainerColor(
            iconTint = iconTint,
            semanticTint = semanticTint,
            treatment = treatment,
        )
    }
}

@Composable
fun AppAdaptiveSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    MiuixSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier
    )
}

@Composable
fun AdaptivePreferenceSectionTitleRenderer(title: String) {
    val visualSpec = remember {
        resolveAdaptiveListComponentVisualSpec()
    }
    SmallTitle(
        text = title,
        textColor = AppSurfaceTokens.onSurfaceVariantSummary(),
        insideMargin = PaddingValues(
            start = visualSpec.sectionStartPaddingDp.dp,
            top = 24.dp,
            bottom = 8.dp
        )
    )
}

@Composable
fun AdaptivePreferenceGroupRenderer(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    shape: androidx.compose.ui.graphics.Shape? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    presentation: AppPreferenceGroupPresentation = AppPreferenceGroupPresentation.CARD,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedPresentation = if (
        LocalAppPreferenceGroupPresentation.current == AppPreferenceGroupPresentation.FLAT
    ) {
        AppPreferenceGroupPresentation.FLAT
    } else {
        presentation
    }
    if (resolvedPresentation == AppPreferenceGroupPresentation.FLAT) {
        Column(modifier = modifier, content = content)
        return
    }

    val visualSpec = remember {
        resolveAdaptiveListComponentVisualSpec()
    }
    val colorScheme = MaterialTheme.colorScheme
    val defaultShape = RoundedCornerShape(visualSpec.groupCornerRadiusDp.dp)
    val appliedShape = shape ?: defaultShape
    val resolvedContainerColor = resolveAdaptiveGroupContainerColor(
        colorScheme = colorScheme,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    )

    MiuixCard(
        modifier = modifier.padding(horizontal = 14.dp),
        cornerRadius = visualSpec.groupCornerRadiusDp.dp,
        insideMargin = PaddingValues(0.dp),
        colors = MiuixCardDefaults.defaultColors(color = resolvedContainerColor)
    ) {
        content()
    }
}

@Composable
internal fun AdaptiveSwitchPreferenceContent(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val visualSpec = remember {
        resolveAdaptiveListComponentVisualSpec()
    }
    val rowSpec = remember {
        resolveAdaptiveListRowVisualSpec()
    }
    val iconTreatment = LocalAppPreferenceIconTreatment.current
    val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(iconTint)
    val filledIconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
    val iconContentColor = resolveAdaptivePreferenceIconGlyphColor(
        treatment = iconTreatment,
        containerContentColor = filledIconContentColor,
        semanticIconColor = effectiveIconTint,
    )
    val iconBackgroundAlpha = resolveAdaptivePreferenceIconBackgroundAlpha(
        iconTreatment,
        visualSpec.iconBackgroundAlpha,
    )
    val listItemStyle = rememberResolvedAppListItemStyle()
    if (listItemStyle == AppListItemStyle.NATIVE) {
        val platformHaptic = LocalHapticFeedback.current
        val effectiveHaptic = if (LocalAppThemeConfig.current.hapticFeedbackEnabled) {
            platformHaptic
        } else {
            NoOpHapticFeedback
        }
        CompositionLocalProvider(LocalHapticFeedback provides effectiveHaptic) {
            MiuixSwitchPreference(
                checked = checked,
                onCheckedChange = onCheckedChange,
                title = title,
                titleColor = BasicComponentDefaults.titleColor(color = textColor),
                summary = subtitle,
                summaryColor = BasicComponentDefaults.summaryColor(color = subtitleColor),
                enabled = enabled,
                insideMargin = PaddingValues(
                    horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                    vertical = rowSpec.insideVerticalPaddingDp.dp
                ),
                startAction = {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }
                }
            )
        }
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = rowSpec.minTouchTargetHeightDp.dp)
            .alpha(if (enabled) 1f else 0.6f)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(
                horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                vertical = rowSpec.insideVerticalPaddingDp.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(visualSpec.iconContainerSizeDp.dp)
                    .adaptiveSquircleBackground(
                        color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                        cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconContentColor,
                    modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = textColor)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = subtitleColor)
            }
        }
        Spacer(modifier = Modifier.width(rowSpec.trailingSpacingDp.dp))
        AppAdaptiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun AdaptiveSliderPreferenceRenderer(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueLabel: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    val visualSpec = remember {
        resolveAdaptiveListComponentVisualSpec()
    }
    val rowSpec = remember {
        resolveAdaptiveListRowVisualSpec()
    }
    val iconTreatment = LocalAppPreferenceIconTreatment.current
    val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(iconTint)
    val filledIconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
    val iconContentColor = resolveAdaptivePreferenceIconGlyphColor(
        treatment = iconTreatment,
        containerContentColor = filledIconContentColor,
        semanticIconColor = effectiveIconTint,
    )
    val iconBackgroundAlpha = resolveAdaptivePreferenceIconBackgroundAlpha(
        iconTreatment,
        visualSpec.iconBackgroundAlpha,
    )
    val iconCornerRadius = visualSpec.iconCornerRadiusDp.dp
    MiuixSliderPreference(
        value = value,
        onValueChange = onValueChange,
        title = title,
        titleColor = BasicComponentDefaults.titleColor(color = textColor),
        summary = subtitle,
        summaryColor = BasicComponentDefaults.summaryColor(color = subtitleColor),
        valueText = valueLabel,
        valueRange = valueRange,
        steps = steps,
        insideMargin = PaddingValues(
            horizontal = rowSpec.insideHorizontalPaddingDp.dp,
            vertical = rowSpec.insideVerticalPaddingDp.dp
        ),
        startAction = {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(visualSpec.iconContainerSizeDp.dp)
                        .adaptiveSquircleBackground(
                            color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                            cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconContentColor,
                        modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                    )
                }
            }
        }
    )
}

@Composable
internal fun AdaptivePreferenceContent(
    icon: ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    copyValue: String? = null,
    onClick: (() -> Unit)? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    chevronTint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
    centered: Boolean = false,
    enableCopy: Boolean = false,
    onCopyRequest: ((text: String, label: String?) -> Unit)? = null,
    showChevron: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val visualSpec = remember {
        resolveAdaptiveListComponentVisualSpec()
    }
    val rowSpec = remember {
        resolveAdaptiveListRowVisualSpec()
    }
    val iconTreatment = LocalAppPreferenceIconTreatment.current
    val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(iconTint)
    val filledIconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
    val iconContentColor = resolveAdaptivePreferenceIconGlyphColor(
        treatment = iconTreatment,
        containerContentColor = filledIconContentColor,
        semanticIconColor = effectiveIconTint,
    )
    val iconBackgroundAlpha = resolveAdaptivePreferenceIconBackgroundAlpha(
        iconTreatment,
        visualSpec.iconBackgroundAlpha,
    )
    val iconCornerRadius = visualSpec.iconCornerRadiusDp.dp
    val clickableRenderer = resolveAppClickableItemRenderer(
        onClick = onClick,
        showChevron = showChevron,
        centered = centered
    )
    val listItemStyle = rememberResolvedAppListItemStyle()
    val nativeListItem = listItemStyle == AppListItemStyle.NATIVE
    if (nativeListItem && clickableRenderer == AppClickableItemRenderer.MIUIX_ARROW) {
        BasicComponent(
            onClick = onClick,
            insideMargin = PaddingValues(
                horizontal = rowSpec.insideHorizontalPaddingDp.dp,
                vertical = rowSpec.insideVerticalPaddingDp.dp
            ),
            startAction = {
                when {
                    icon != null -> {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }

                    iconPainter != null -> {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = if (effectiveIconTint == Color.Unspecified) {
                                        Color.Transparent
                                    } else {
                                        effectiveIconTint.copy(alpha = iconBackgroundAlpha)
                                    },
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }
                }
            },
            endActions = {
                trailingContent?.invoke()
                if (!value.isNullOrBlank()) {
                    if (trailingContent != null) Spacer(Modifier.width(8.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = valueColor,
                        maxLines = APP_PREFERENCE_VALUE_MAX_LINES,
                        softWrap = true,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        modifier = Modifier
                            .appPreferenceValueTextModifier()
                            .onLongPressAction(
                                enabled = enableCopy && onCopyRequest != null,
                                onLongPress = { onCopyRequest?.invoke(copyValue ?: value, title) },
                            )
                    )
                }
                if (showChevron && onClick != null) {
                    if (trailingContent != null || !value.isNullOrBlank()) Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = chevronTint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        ) {
            Text(
                text = title,
                color = textColor,
                fontSize = MiuixTheme.textStyles.headline1.fontSize,
                fontWeight = FontWeight.Medium,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = subtitleColor,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                )
            }
        }
        return
    }
    BasicComponent(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = rowSpec.minTouchTargetHeightDp.dp),
        title = title,
        summary = subtitle,
        onClick = onClick,
        insideMargin = PaddingValues(
            horizontal = rowSpec.insideHorizontalPaddingDp.dp,
            vertical = rowSpec.insideVerticalPaddingDp.dp
        ),
            startAction = {
                when {
                    icon != null -> {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }

                    iconPainter != null -> {
                        Box(
                            modifier = Modifier
                                .size(visualSpec.iconContainerSizeDp.dp)
                                .adaptiveSquircleBackground(
                                    color = if (effectiveIconTint == Color.Unspecified) {
                                        Color.Transparent
                                    } else {
                                        effectiveIconTint.copy(alpha = iconBackgroundAlpha)
                                    },
                                    cornerRadius = visualSpec.iconCornerRadiusDp.dp,
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = null,
                                tint = iconContentColor,
                                modifier = Modifier.size(visualSpec.iconGlyphSizeDp.dp)
                            )
                        }
                    }
                }
            },
            endActions = {
                trailingContent?.invoke()
                if (!value.isNullOrBlank()) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppSurfaceTokens.onSurfaceVariantSummary(),
                        maxLines = APP_PREFERENCE_VALUE_MAX_LINES,
                        softWrap = true,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        modifier = Modifier
                            .appPreferenceValueTextModifier()
                            .onLongPressAction(
                                enabled = enableCopy && onCopyRequest != null,
                                onLongPress = { onCopyRequest?.invoke(copyValue ?: value, title) },
                            )
                    )
                    Spacer(modifier = Modifier.width(rowSpec.trailingSpacingDp.dp))
                }
                if (onClick != null && showChevron) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = AppSurfaceTokens.onSurfaceVariantActions(),
                        modifier = Modifier.size(rowSpec.trailingIconSizeDp.dp)
                    )
                }
            }
        )
}

@Composable
fun AdaptivePreferenceDividerRenderer(
    modifier: Modifier = Modifier,
    startIndent: androidx.compose.ui.unit.Dp = 66.dp
) {
    // 迁移后所有样式统一使用 0.dp 分隔线，渲染器保持空操作。
    return
}


@Composable
fun AdaptivePreferenceGridItemRenderer(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    val visualSpec = remember {
        resolveAdaptiveListComponentVisualSpec()
    }
    val iconTreatment = LocalAppPreferenceIconTreatment.current
    val effectiveIconTint = rememberAdaptivePreferenceIconContainerColor(iconTint)
    val filledIconContentColor = rememberAdaptivePreferenceIconContentColor(effectiveIconTint)
    val iconContentColor = resolveAdaptivePreferenceIconGlyphColor(
        treatment = iconTreatment,
        containerContentColor = filledIconContentColor,
        semanticIconColor = effectiveIconTint,
    )
    val iconBackgroundAlpha = resolveAdaptivePreferenceIconBackgroundAlpha(
        iconTreatment,
        visualSpec.iconBackgroundAlpha,
    )
    val cornerRadiusScale = LocalCornerRadiusScale.current
    val itemCornerRadius = visualSpec.gridCornerRadiusDp.dp
    val resolvedContainerColor = resolveGlobalWallpaperListContainerColor(
        containerColor = containerColor,
        colorScheme = MaterialTheme.colorScheme,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current,
        targetAlpha = 0.62f
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(itemCornerRadius))
            .background(resolvedContainerColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .adaptiveSquircleBackground(
                    color = effectiveIconTint.copy(alpha = iconBackgroundAlpha),
                    cornerRadius = iOSCornerRadius.Small * cornerRadiusScale,
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconContentColor,
                modifier = Modifier.size(26.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AdaptiveSearchFieldRenderer(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    heightOverride: Dp? = null,
    forceExpandedInput: Boolean = false,
    topBarChrome: Boolean = false,
    onSearch: () -> Unit = {},
    onClear: () -> Unit = { onQueryChange("") },
    showClearAction: Boolean = true,
    autoFocusEnabled: Boolean = forceExpandedInput,
    focusRequester: FocusRequester? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val visualSpec = remember {
        resolveAdaptiveListComponentVisualSpec()
    }
    val searchBarCornerRadius = visualSpec.searchBarCornerRadiusDp.dp
    val resolvedContainerColor = resolveAdaptiveSearchBarContainerColor(
        colorScheme = colorScheme,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    )
    val resolvedHeight = heightOverride ?: visualSpec.searchBarHeightDp.dp

    if (forceExpandedInput) {
        val fallbackFocusRequester = remember { FocusRequester() }
        val resolvedFocusRequester = focusRequester ?: fallbackFocusRequester
        LaunchedEffect(resolvedFocusRequester, autoFocusEnabled) {
            if (autoFocusEnabled) {
                delay(80)
                runCatching { resolvedFocusRequester.requestFocus() }
            }
        }
        val focusModifier = Modifier.focusRequester(resolvedFocusRequester)
        MiuixAdaptiveSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = modifier.then(focusModifier),
            placeholder = placeholder,
            containerColor = resolvedContainerColor,
            height = resolvedHeight,
            forceExpandedInput = true,
            onSearch = onSearch,
            interactionSource = interactionSource,
        )
        return
    }

    MiuixAdaptiveSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        modifier = modifier,
        placeholder = placeholder,
        containerColor = resolvedContainerColor,
        height = resolvedHeight,
        forceExpandedInput = forceExpandedInput,
        onSearch = { onSearch() },
        interactionSource = interactionSource,
    )
}

@Composable
fun AppSearchEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
) {
    val colorScheme = MaterialTheme.colorScheme
    val visualSpec = rememberAdaptiveListVisualCapabilities().componentSpec
    val resolvedContainerColor = resolveAdaptiveSearchBarContainerColor(
        colorScheme = colorScheme,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current,
    )
    val cornerRadius = visualSpec.searchBarCornerRadiusDp.dp
    val searchIcon = Icons.Default.Search

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = visualSpec.searchBarHeightDp.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(resolvedContainerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = searchIcon,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun AdaptiveTextFieldRenderer(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MiuixTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label ?: placeholder.orEmpty(),
            useLabelAsPlaceholder = label == null,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            colors = MiuixTextFieldDefaults.textFieldColors(
                borderColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MiuixTheme.colorScheme.primary
                },
            ),
        )
        supportingText?.invoke()
    }
}

@Composable
private fun MiuixAdaptiveSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    @Suppress("UNUSED_PARAMETER") containerColor: Color,
    height: androidx.compose.ui.unit.Dp,
    forceExpandedInput: Boolean = false,
    onSearch: () -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
) {
    if (forceExpandedInput) {
        InputField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { onSearch() },
            expanded = true,
            onExpandedChange = {},
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            label = placeholder,
            interactionSource = interactionSource,
        )
        return
    }
    var expanded by rememberSaveable(query.isNotBlank()) {
        mutableStateOf(query.isNotBlank())
    }
    InputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { onSearch() },
        expanded = expanded || query.isNotBlank(),
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        label = placeholder,
        interactionSource = interactionSource,
    )
}
