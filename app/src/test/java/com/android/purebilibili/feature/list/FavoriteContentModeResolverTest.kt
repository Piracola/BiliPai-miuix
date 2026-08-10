package com.android.purebilibili.feature.list

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FavoriteContentModeResolverTest {

    @Test
    fun nonFavoritePageUsesBaseMode() {
        assertEquals(
            FavoriteContentMode.BASE_LIST,
            resolveFavoriteContentMode(isFavoritePage = false, folderCount = 3)
        )
    }

    @Test
    fun singleFolderUsesFolderStateMode() {
        assertEquals(
            FavoriteContentMode.SINGLE_FOLDER,
            resolveFavoriteContentMode(isFavoritePage = true, folderCount = 1)
        )
    }

    @Test
    fun multipleFoldersUsePagerMode() {
        assertEquals(
            FavoriteContentMode.PAGER,
            resolveFavoriteContentMode(isFavoritePage = true, folderCount = 2)
        )
    }

    @Test
    fun favoriteHeaderUsesFolderSelectorWithoutOwnedSubscribedSegmentedRow() {
        val listSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt"
        )

        assertTrue(listSource.contains("FavoriteFolderSelector("))
        assertFalse(listSource.contains("selectedValue = favoriteBrowseSection"))
        assertFalse(listSource.contains("FavoriteFolderSummary("))
        assertFalse(listSource.contains("AppSegmentOption(FavoriteBrowseSection.OWNED"))
    }

    @Test
    fun sharedSegmentedControlForwardsLiquidInteractionOptions() {
        val segmentedSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/AppSegmentedControl.kt"
        )
        val bottomBarSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarLiquidSegmentedControl.kt"
        )

        assertTrue(
            segmentedSource.contains("forceLiquidIndicator: Boolean = false"),
            "Shared segmented control should expose an explicit liquid-indicator override"
        )
        assertTrue(
            segmentedSource.contains("tapPressRefractionEnabled: Boolean = true"),
            "Shared segmented control should expose tap refraction control to callers"
        )
        assertTrue(
            segmentedSource.contains("forceLiquidChrome = forceLiquidIndicator"),
            "Shared iOS segmented control should forward the override into the bottom-bar liquid implementation"
        )
        assertTrue(
            segmentedSource.contains("tapPressRefractionEnabled = tapPressRefractionEnabled"),
            "Shared iOS segmented control should forward tap refraction control into the bottom-bar liquid implementation"
        )
        assertTrue(
            segmentedSource.contains("dragSelectionEnabled = dragSelectionEnabled"),
            "Shared segmented control should forward drag-selection policy to its liquid implementation"
        )
        assertTrue(
            bottomBarSource.contains("forceLiquidChrome: Boolean = false"),
            "BottomBarLiquidSegmentedControl should allow parents with settled settings to bypass the async default fallback"
        )
        assertTrue(
            bottomBarSource.contains("forceLiquidChrome || homeSettings.androidNativeLiquidGlassEnabled"),
            "BottomBarLiquidSegmentedControl should treat forced liquid chrome the same as the global Android native glass setting"
        )
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
