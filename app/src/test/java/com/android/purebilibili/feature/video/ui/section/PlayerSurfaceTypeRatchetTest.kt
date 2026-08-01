package com.android.purebilibili.feature.video.ui.section

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class PlayerSurfaceTypeRatchetTest {

    @Test
    fun playerViewUsesSurfaceViewSoHdrMetadataCanReachTheDisplay() {
        val layout = listOf(
            "src/main/res/layout/view_player_texture.xml",
            "app/src/main/res/layout/view_player_texture.xml"
        )
            .map(::File)
            .firstOrNull(File::exists)
            ?: error("找不到 PlayerView 布局")

        assertTrue(layout.readText().contains("app:surface_type=\"surface_view\""))
    }
}
