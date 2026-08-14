package com.android.purebilibili.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTooltipIntegrationTest {

    @Test
    fun appearanceDescriptionCardAndAdaptiveTooltipWireOfficialPath() {
        val tooltipSource = load(
            "design-system/src/main/java/com/android/purebilibili/core/ui/AdaptiveTooltip.kt"
        )

        assertTrue(tooltipSource.contains("MiuixTooltipBox("))
    }

    private fun load(path: String): String = listOf(
        File(path),
        File("../$path"),
        File(path.removePrefix("app/")),
    ).first { it.exists() }.readText()
}
