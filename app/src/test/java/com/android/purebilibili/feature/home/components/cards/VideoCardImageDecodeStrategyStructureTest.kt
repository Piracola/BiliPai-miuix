package com.android.purebilibili.feature.home.components.cards

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoCardImageDecodeStrategyStructureTest {

    @Test
    fun `video cards constrain decode only with layout derived cover request specs`() {
        val sourceRoot = File("src/main/java/com/android/purebilibili/feature/home/components/cards")
        val cardSources = listOf(
            "VideoCard.kt",
            "GlassVideoCard.kt",
            "CinematicVideoCard.kt"
        ).associateWith { fileName -> sourceRoot.resolve(fileName).readText() }

        cardSources.forEach { (fileName, source) ->
            val imageRequests = Regex(
                "ImageRequest\\.Builder\\([^)]*\\)[\\s\\S]*?\\.build\\(\\)"
            ).findAll(source)
                .map { it.value }
                .filter { request ->
                    request.contains(".data(coverUrl)") || request.contains(".data(requestCoverUrl)")
                }
                .toList()
            assertTrue(imageRequests.isNotEmpty(), "$fileName 应包含封面图片请求")
            assertFalse(
                imageRequests.any { request ->
                    request.contains(".size(") ||
                        (request.contains("size(") && !request.contains("coverRequestSpec?.let"))
                },
                "$fileName 只能通过布局派生的 coverRequestSpec 限制封面解码尺寸"
            )
        }
    }
}
