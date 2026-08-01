package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRootCategoryContentStructureTest {

    @Test
    fun rootCategoryContent_usesStateAndActionHolders() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(source.contains("internal data class SettingsRootCategoryActions("))
        assertTrue(source.contains("internal data class SettingsRootCategoryState("))
        assertTrue(
            source.contains(
                """
                internal fun SettingsRootCategoryContent(
                    category: SettingsRootCategory,
                    actions: SettingsRootCategoryActions,
                    state: SettingsRootCategoryState
                )
                """.trimIndent()
            )
        )
    }

    @Test
    fun detailEntrySection_submitsDetailFocusBeforeOpeningEntry() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val sectionBlock = source
            .substringAfter("internal fun SettingsDetailEntrySection(")
            .substringBefore("internal fun SettingsRootCategoryContent(")

        assertTrue(sectionBlock.contains("resolveSettingsSceneDetailFocus(entry.target)?.let"))
        assertTrue(sectionBlock.contains("SettingsSearchFocusController.submit(detailFocus.target, detailFocus.focusId)"))
        assertTrue(sectionBlock.contains("entry.onClick()"))
        assertTrue(sectionBlock.contains("subtitle = entry.value"))
    }

    @Test
    fun feedApiSection_usesNativeSwitchAndSliderItems() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val feedApiBlock = source
            .substringAfter("fun FeedApiSection(")
            .substringBefore("@Composable\nprivate fun FeedDynamicTabVisibilityItem(")

        assertTrue(feedApiBlock.contains("SettingSwitchItem("))
        assertTrue(feedApiBlock.contains("SettingSliderItem("))
        assertTrue(feedApiBlock.contains("SettingsAdaptiveDivider()"))
        assertTrue(feedApiBlock.contains("动态顶栏下滑折叠"))
        assertTrue(feedApiBlock.contains("dynamicTopBarCollapseOnScroll"))
        assertFalse(feedApiBlock.contains("private fun FeedSwitchItem("))
    }

    @Test
    fun mobileSettingsRoot_usesCategoryListWithSearchEntryAndAboutSection() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(source.contains("SettingsHomeSearchEntry("))
        assertTrue(source.contains("SettingsRootCategoryListSection("))
        assertTrue(source.contains("SettingsAboutHomeSection("))
        assertTrue(source.contains("MobileSettingsNavLayout("))
    }

    @Test
    fun rootCategoryContent_staggersDetailGroupsWithEntranceSections() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(source.contains("internal fun SettingsRootCategoryEntranceSection("))
        assertTrue(source.contains("Box(modifier = Modifier.entrance())"))
        val contentBlock = source
            .substringAfter("internal fun SettingsRootCategoryContent(")
            .substringBefore("@Composable\nfun SupportToolsSection(")
        assertTrue(contentBlock.contains("SettingsRootCategoryEntranceSection {"))
        assertTrue(
            contentBlock.indexOf("SettingsRootCategoryEntranceSection {") <
                contentBlock.indexOf("SettingsDetailGroup(title = \"动效\")")
        )
    }

    @Test
    fun rootCategoryContent_usesStableDetailGroupsWithoutSceneShortcutRows() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val contentBlock = source
            .substringAfter("internal fun SettingsRootCategoryContent(")
            .substringBefore("@Composable\nfun SupportToolsSection(")

        assertTrue(contentBlock.contains("Column {\n        when (category)"))
        assertTrue(contentBlock.contains("SettingsDetailGroup("))
        assertTrue(contentBlock.contains("SettingsDetailEntrySection("))
        assertFalse(contentBlock.contains("SettingsSceneShortcutSection("))
        assertTrue(contentBlock.contains("SettingsRootCategory.CONTENT_PLAYBACK -> {"))
        assertTrue(contentBlock.contains("SettingsRootCategory.PRIVACY_STORAGE -> {"))
        assertTrue(contentBlock.contains("SettingsRootCategory.SYSTEM_ABOUT -> {"))
    }

    @Test
    fun mobileSettingsRootUsesNav3DestinationInsteadOfLocalDrillDown() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(source.contains("SettingsNavDestination"))
        assertTrue(source.contains("SettingsRootCategoryContent("))
        assertTrue(source.contains("onCategoryClick"))
        assertFalse(source.contains("SettingsRootDrillDownNavigator("))
        assertFalse(source.contains("activeRootCategoryName"))
    }

    @Test
    fun mobileSettingsRootUsesCategoryRowsWithoutAccordionExpansion() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val sectionBlock = source
            .substringAfter("internal fun SettingsRootCategoryListSection(")
            .substringBefore("@Composable\nprivate fun SettingsRootCategoryRow(")

        assertTrue(sectionBlock.contains("SettingsRootCategoryRow("))
        assertTrue(sectionBlock.contains("onCategoryClick(category)"))
        assertFalse(sectionBlock.contains("AnimatedVisibility("), "Nagram-style root should navigate, not accordion-expand")
        assertFalse(sectionBlock.contains("SettingsRootCategoryContent("), "root rows should not inline detail content")
    }

    @Test
    fun mobileSettingsRootUsesNav3ScreensInsteadOfInlineDrillDown() {
        val screenSource = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")
        val categoryScreenExists = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsCategoryScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsCategoryScreen.kt")
        ).any { it.exists() }
        val searchScreenExists = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsSearchScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsSearchScreen.kt")
        ).any { it.exists() }

        assertTrue(categoryScreenExists)
        assertTrue(searchScreenExists)
        assertTrue(screenSource.contains("SettingsNavDestination"))
        assertFalse(screenSource.contains("SettingsRootDrillDownNavigator("))
    }

    @Test
    fun tabletSettingsShell_usesNavDrivenCategoryRail() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsTabletShell.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsTabletShell.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(source.contains("SettingsHomeSearchEntry(onClick = onSearchOpen)"))
        assertTrue(source.contains("NavigationDrawerItem("))
        assertTrue(source.contains("text = category.subtitle"))
        assertTrue(source.contains("rightPane()"))
    }

    @Test
    fun appNavigation_wrapsSettingsSubtreeRoutesWithTabletShell() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt"),
            File("src/main/java/com/android/purebilibili/navigation/AppNavigation.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(source.contains("fun SettingsTabletEntry(content: @Composable () -> Unit)"))
        assertTrue(source.contains("SettingsTabletNavEntryShell("))
        assertTrue(source.contains("forceSinglePaneContent = true"))
        listOf(
            "BiliPaiNavEntryContentRole.SETTINGS ->",
            "BiliPaiNavEntryContentRole.PLAYBACK_SETTINGS ->",
            "BiliPaiNavEntryContentRole.PLUGINS_SETTINGS ->",
            "BiliPaiNavEntryContentRole.TIPS_SETTINGS ->",
        ).forEach { marker ->
            val block = source.substringAfter(marker)
            assertTrue(
                block.contains("SettingsTabletEntry {"),
                "Expected SettingsTabletEntry wrapper after $marker"
            )
        }
    }

    @Test
    fun tipsSettingsScreen_usesSharedPageScaffold() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/TipsSettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/TipsSettingsScreen.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(source.contains("SettingsPageScaffold("))
        assertFalse(source.contains("SettingsLargeTitleHeader("))
        assertFalse(source.contains("AppScaffold("))
    }

    @Test
    fun aboutSupport_keepsReleaseChannelBelowAboutDetailsWithoutDuplicateAuthorCard() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val aboutBlock = source
            .substringAfter("SettingsRootCategory.SYSTEM_ABOUT -> {")
            .substringBefore("SupportToolsSection(")

        assertTrue(aboutBlock.indexOf("AboutSection(") < aboutBlock.indexOf("ReleaseChannelPinnedCard("))
        assertFalse(aboutBlock.contains("FollowAuthorSection("))
    }

    @Test
    fun aboutSectionShowsProjectOverviewAndStaticContributorsBeforeRows() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val aboutSectionBlock = source
            .substringAfter("fun AboutSection(")
            .substringBefore("@Composable\nprivate fun AboutProjectOverviewCard(")

        assertTrue(aboutSectionBlock.contains("AboutProjectOverviewCard(versionName = versionName)"))
        assertTrue(aboutSectionBlock.indexOf("AboutProjectOverviewCard(") < aboutSectionBlock.indexOf("SettingsCardGroup {"))
        assertTrue(source.contains("internal val AboutContributors = listOf("))
        assertTrue(source.contains("AboutContributor(\"jay3-yy\", \"jay3-yy\", R.drawable.avatar_jay3_yy)"))
        assertTrue(source.contains("name = \"Piracola\""))
        assertTrue(source.contains("githubLogin = \"Piracola\""))
        assertTrue(source.contains("AboutContributor(\"Chenx Dust\""))
        assertTrue(source.contains("AboutContributor(\"usontong\""))
        assertTrue(source.contains("AboutContributor(\"Leko\", \"lekoOwO\", R.drawable.avatar_lekoowo)"))
        assertTrue(source.contains("AboutContributor(\"qyo123oyq\", \"qyo123oyq\", R.drawable.avatar_qyo123oyq)"))
        assertTrue(source.contains("name = \"maxzrb\""))
        assertTrue(source.contains("githubLogin = \"maxzrb\""))
        assertTrue(source.contains("name = \"xiaoniao427\""))
        assertTrue(source.contains("name = \"zensu357\""))
        assertTrue(source.contains("name = \"Kurarion\""))
    }

    @Test
    fun aboutContributors_useGithubProfilesAndClickableAvatars() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val contributorBlock = source
            .substringAfter("internal data class AboutContributor(")
            .substringBefore("// 默认使用本地头像以避免进入关于页时请求 GitHub")
        val overviewBlock = source
            .substringAfter("private fun AboutProjectOverviewCard(")
            .substringBefore("@Composable\nprivate fun AboutContributorItem(")
        val itemBlock = source
            .substringAfter("private fun AboutContributorItem(")

        assertTrue(contributorBlock.contains("val githubLogin: String"))
        assertTrue(contributorBlock.contains("val avatarResId: Int?"))
        assertTrue(contributorBlock.contains("\"https://github.com/${'$'}githubLogin\""))
        assertTrue(contributorBlock.contains("val avatarUrl: String?"))
        assertTrue(source.contains("https://avatars.githubusercontent.com/u/114979598?v=4"))
        assertTrue(overviewBlock.contains("text = \"贡献者\""))
        assertFalse(overviewBlock.contains("其他贡献者"))
        assertFalse(overviewBlock.contains("顺手"))
        assertTrue(overviewBlock.contains("remember { AboutSlogans.random() }"))
        assertTrue(source.contains("删繁留简见初心"))
        assertTrue(source.contains("广告退场方显净"))
        assertTrue(source.contains("弱水三千凡君取"))
        assertFalse(source.contains("主题:"))
        assertTrue(overviewBlock.contains("FlowRow("))
        assertFalse(overviewBlock.contains("horizontalScroll("))
        assertTrue(itemBlock.contains("LocalUriHandler.current"))
        assertTrue(itemBlock.contains("uriHandler.openUri(contributor.profileUrl)"))
        assertTrue(itemBlock.contains("Box("))
        assertTrue(itemBlock.contains("AsyncImage("))
        assertTrue(itemBlock.contains("model = avatarUrl"))
        assertTrue(itemBlock.contains("painterResource(id = avatarResId)"))
        assertTrue(itemBlock.contains("contentScale = ContentScale.Crop"))
    }

    @Test
    fun settingsSubpages_useNagramStyleSectionNames() {
        val settingsSections = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")
        val appearance = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")
        val playback = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/PlaybackSettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/PlaybackSettingsScreen.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")
        val animation = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(settingsSections.contains("SettingsDetailGroup(title = \"显示与交互\")"))
        assertTrue(settingsSections.contains("SettingsDetailGroup(title = \"画质与播放\")"))
        assertTrue(settingsSections.contains("SettingsDetailGroup(title = \"隐私与安全\")"))
        assertTrue(appearance.contains("AppPreferenceSectionTitle(\"显示模式\")"))
        assertTrue(appearance.contains("AppPreferenceSectionTitle(\"字体与密度\")"))
        assertTrue(appearance.contains("AppPreferenceSectionTitle(\"开屏与图标\")"))
        assertTrue(playback.contains("AppPreferenceSectionTitle(\"小窗与后台\")"))
        assertTrue(playback.contains("AppPreferenceSectionTitle(\"诊断\")"))
        assertTrue(
            playback.indexOf("AppPreferenceSectionTitle(\"网络与画质\")") <
                playback.indexOf("AppPreferenceSectionTitle(\"互动与评论\")")
        )
        assertTrue(animation.contains("AppPreferenceSectionTitle(\"动效\")"))
        assertFalse(animation.contains("AppPreferenceSectionTitle(\"玻璃效果\")"))
        assertFalse(animation.contains("AppPreferenceSectionTitle(\"底栏入口\")"))
    }

    @Test
    fun aboutProjectOverview_usesRasterLauncherAssetForComposePainter() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val overviewBlock = source
            .substringAfter("private fun AboutProjectOverviewCard(")
            .substringBefore("@Composable\nprivate fun ContributorAvatarRow(")

        assertFalse(overviewBlock.contains("R.mipmap.ic_launcher_bilipai)"))
        assertTrue(overviewBlock.contains("R.mipmap.ic_launcher_bilipai_foreground"))
    }

    @Test
    fun aboutSection_doesNotRenderDuplicateReleaseChannelDisclaimerEntry() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val aboutSectionBlock = source
            .substringAfter("fun AboutSection(")
            .substringBefore("@Composable\nfun CheckUpdateSection(")

        assertFalse(aboutSectionBlock.contains("title = \"发布渠道声明\""))
        assertFalse(aboutSectionBlock.contains("SettingsSearchTarget.DISCLAIMER"))
    }

    @Test
    fun tabletSettingsCategoryDrawer_showsSubtitle() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsTabletShell.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsTabletShell.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(source.contains("text = category.subtitle"))
    }

    @Test
    fun categoryEntries_useShortPlaybackNavigationCopy() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        assertTrue(source.contains("title = \"首页样式与壁纸\""))
        assertTrue(source.contains("value = \"在播放设置内 · 全屏方向与手势控制\""))
        assertTrue(source.contains("value = \"在播放设置内 · 诊断日志与统计\""))
    }

    @Test
    fun releaseChannelPinnedCard_keepsActionsInOneLine() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText().replace("\r\n", "\n")

        val pinnedCardBlock = source
            .substringAfter("fun ReleaseChannelPinnedCard(")
            .substringBefore("@Composable\nfun SettingsSubpageEntrySection(")

        assertTrue(pinnedCardBlock.contains("modifier = Modifier.fillMaxWidth()"))
        assertTrue(pinnedCardBlock.contains("modifier = Modifier.weight(1f)"))
        assertTrue(pinnedCardBlock.contains("softWrap = false"))
        assertTrue(pinnedCardBlock.contains("maxLines = 1"))
    }
}
