package com.android.purebilibili.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveTooltipIntegrationTest {

    @Test
    fun appearanceDescriptionCardAndAdaptiveTooltipWireOfficialPath() {
        val tooltipSource = load(
            "design-system/src/main/java/com/android/purebilibili/core/ui/AdaptiveTooltip.kt"
        )
        val appearanceSource = load(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt"
        )

        assertTrue(tooltipSource.contains("MiuixTooltipBox("))
        assertTrue(tooltipSource.contains("rememberPresetPrimitiveRenderer()"))
        // D3：界面预设说明卡已降级为组内小字说明，不再使用高强调 tooltip 卡。
        assertFalse(appearanceSource.contains("AdaptivePlainTooltipBox("))
        assertTrue(appearanceSource.contains("uiPresetDescription.summary"))
    }

    private fun load(path: String): String = listOf(
        File(path),
        File("../$path"),
        File(path.removePrefix("app/")),
    ).first { it.exists() }.readText()
}
