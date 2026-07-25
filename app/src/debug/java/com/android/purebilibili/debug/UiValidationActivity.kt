package com.android.purebilibili.debug

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.PureBiliBiliTheme
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.ui.AdaptiveScaffold
import com.android.purebilibili.core.ui.AdaptiveTopAppBar
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.components.IOSClickableItem
import com.android.purebilibili.core.ui.components.IOSGroup
import com.android.purebilibili.core.ui.components.IOSSectionTitle
import com.android.purebilibili.core.ui.components.IOSSwitchItem
import com.android.purebilibili.feature.settings.AppThemeMode

class UiValidationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialDarkTheme = intent.getStringExtra(EXTRA_THEME) == THEME_DARK
        setContent {
            UiValidationRoot(
                initialDarkTheme = initialDarkTheme,
                onSystemBarStyleChange = ::updateSystemBars
            )
        }
    }

    private fun updateSystemBars(darkTheme: Boolean) {
        val transparent = android.graphics.Color.TRANSPARENT
        val style = if (darkTheme) {
            SystemBarStyle.dark(transparent)
        } else {
            SystemBarStyle.light(transparent, transparent)
        }
        enableEdgeToEdge(
            statusBarStyle = style,
            navigationBarStyle = style
        )
    }

    private companion object {
        const val EXTRA_THEME = "ui_theme"
        const val THEME_DARK = "dark"
    }
}

@Composable
private fun UiValidationRoot(
    initialDarkTheme: Boolean,
    onSystemBarStyleChange: (Boolean) -> Unit = {}
) {
    var darkTheme by rememberSaveable { mutableStateOf(initialDarkTheme) }
    LaunchedEffect(darkTheme) {
        onSystemBarStyleChange(darkTheme)
    }
    PureBiliBiliTheme(
        uiPreset = UiPreset.MD3,
        androidNativeVariant = AndroidNativeVariant.MIUIX,
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        darkTheme = darkTheme,
        dynamicColor = false
    ) {
        UiValidationScreen(
            darkTheme = darkTheme,
            onDarkThemeChange = { darkTheme = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UiValidationScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    AdaptiveScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AdaptiveTopAppBar(
                title = "UI 验证台",
                subtitle = "Debug only",
                actions = {
                    FilledIconButton(
                        onClick = { onDarkThemeChange(!darkTheme) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (darkTheme) "切换到浅色" else "切换到深色"
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppSurfaceTokens.background()),
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "${configuration.screenWidthDp} x ${configuration.screenHeightDp} dp",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppSurfaceTokens.onSurfaceVariantSummary()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ThemeModeControl(
                        darkTheme = darkTheme,
                        onDarkThemeChange = onDarkThemeChange
                    )
                }
            }

            item {
                ValidationSection(title = "语义颜色") {
                    val roles = listOf(
                        ValidationColor("Primary", AppSurfaceTokens.primary()),
                        ValidationColor("Background", AppSurfaceTokens.background()),
                        ValidationColor("Surface", AppSurfaceTokens.surface()),
                        ValidationColor("Container", AppSurfaceTokens.surfaceContainer()),
                        ValidationColor("Container High", AppSurfaceTokens.surfaceContainerHigh())
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        roles.forEach { role ->
                            ColorRoleRow(role)
                        }
                    }
                }
            }

            item {
                ValidationSection(title = "排版") {
                    Text("页面标题 / Title large", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("正文内容 / Body large", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "辅助信息 / Body small，用于检查较长文字是否截断或挤压布局。",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppSurfaceTokens.onSurfaceVariantSummary()
                    )
                }
            }

            item {
                ValidationSection(title = "操作") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Outlined.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("主要操作")
                        }
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Outlined.PlayCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("次要操作")
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .fillMaxWidth()
                ) {
                    IOSSectionTitle("列表与设置")
                    IOSGroup {
                        IOSClickableItem(
                            icon = Icons.Outlined.Settings,
                            title = "外观设置",
                            subtitle = "检查标题、摘要、图标和尾部箭头",
                            onClick = {}
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 64.dp),
                            color = AppSurfaceTokens.divider()
                        )
                        IOSSwitchItem(
                            icon = Icons.Outlined.Notifications,
                            title = "通知开关",
                            subtitle = "检查开关状态与最小触摸区域",
                            checked = true,
                            onCheckedChange = {}
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeControl(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        listOf(false to "浅色", true to "深色").forEachIndexed { index, (dark, label) ->
            SegmentedButton(
                modifier = Modifier.testTag(if (dark) "theme_dark" else "theme_light"),
                selected = darkTheme == dark,
                onClick = { onDarkThemeChange(dark) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                icon = {
                    Icon(
                        imageVector = if (dark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun ValidationSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AppSurfaceTokens.onSurface()
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

private data class ValidationColor(
    val name: String,
    val color: Color
)

@Composable
private fun ColorRoleRow(role: ValidationColor) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(role.color, MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = role.name,
            style = MaterialTheme.typography.bodyLarge,
            color = AppSurfaceTokens.onSurface()
        )
    }
}

@Preview(name = "Phone Light", widthDp = 393, heightDp = 852, showSystemUi = true)
@Composable
private fun UiValidationPhoneLightPreview() {
    UiValidationRoot(initialDarkTheme = false)
}

@Preview(
    name = "Phone Dark",
    widthDp = 393,
    heightDp = 852,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun UiValidationPhoneDarkPreview() {
    UiValidationRoot(initialDarkTheme = true)
}

@Preview(name = "Tablet Light", widthDp = 1280, heightDp = 800)
@Composable
private fun UiValidationTabletLightPreview() {
    UiValidationRoot(initialDarkTheme = false)
}

@Preview(
    name = "Tablet Dark",
    widthDp = 1280,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun UiValidationTabletDarkPreview() {
    UiValidationRoot(initialDarkTheme = true)
}
