# 阶段 0：残留代码 / 资源 / 测试覆盖 / CI 审计

日期：2026-08-16。方法：资源逐一反查 `R.<type>.<name>` + `@<type>/<name>`；源码关键词扫描；只读。

## 1. 孤儿/可疑资源（app 模块，其他模块无 res）

### 1a. 确认未引用 drawable（10 个）
`ic_bottom_nav_listen_video(_selected).xml`（listen-video 模式已删）、`ic_home_nav_dynamic_filled.xml`、`ic_home_nav_live_filled.xml`、`ic_home_nav_game_filled.xml`（`HomeNavigationMiuixStructureTest` 已 assert 源码无引用但未删文件）、`ic_launcher_background.xml`、`ic_launcher_background_3d.xml`、`ic_telegram_logo.xml`、`ic_telegram_squircle.xml`、`ic_telegram_squircle_dark.xml`（含 drawable-night 同族）。

### 1b. 孤儿 mipmap（22 个根名，约 123 个密度文件）
`ic_launcher_3d_round`、`ic_launcher_bilipai_monet_round`、`ic_launcher_bilipai_pink_round`、`ic_launcher_bilipai_round`、`ic_launcher_bilipai_white_round`、`ic_launcher_foreground`（非 `_flat`）、`ic_launcher_blue_snow_maid_*` 家族（announcement/dark/front/light 及 _round 变体）。**勿删**：`ic_launcher_blue_snow_maid(_round/foreground/monochrome)`、`ic_launcher_3d`、`ic_launcher_bilipai(_monet)`、`ic_launcher_3d_foreground`、`ic_launcher_foreground_flat`。

### 1c. 孤儿 raw Lottie JSON（23 个，0 引用）
`addone_icon, attach_gallery, bubble, cake, camera, channel_create, daynight_theme, email_setup_heart, filters, fire_on, folder_in, folder_share, gift, gigagroup_convert, giveaway_results, heart_like, hint_swipe_reply, ic_download, ic_unmute, iphone_30, premium_speed, sheet_music, utyan_new`。Lottie 依赖已从 build 移除（grep 无 lottie），是死资产。仅 `cdn_region_catalog.json` 在用（CdnRegionPlugin）。

### 1d. 孤儿 color
`values/colors.xml`：purple_200/500/700、teal_200/700、black、white、background（模板残留色板）；`splash_bilipai_monet_background/foreground`（4 份 colors.xml，无引用）。**勿删**：`splash_background`（themes.xml 在用）、`splash_icon_blue_snow_maid`。

### 1e. 在用确认
`layout/view_player_texture.xml`（LivePlayerScreen:771）、`xml/shortcuts.xml`（4 个 ic_shortcut_* 在用）、`drawable-night/` 其余文件在用。

## 2. 代码残留

1. **LottieComponents.kt**（`core/ui/LottieComponents.kt`）仍存在并被 BangumiDetailScreen/ArticleDetailScreen 等调用（`CutePersonLoadingIndicator`/`AdaptiveLoadingIndicator`）；Lottie 依赖已移除、23 个 raw JSON 变孤儿。若内部已改为 Miuix 进度条则是残留命名，需核查。测试 `AdaptiveLoadingIndicatorIntegrationTest` 断言该文件不含 `lottie` 字符串、`IosLoadingIndicator.kt` 不存在。
2. **liquid-glass 恒 false 分支链（中优先，审计发现的唯一大面积"声称删除仍留活代码"区域）**：
   - `BottomBar.kt:897` `shouldRenderBottomBarLiquidGlassEffects(...) = false`（硬编码恒 false），`:1301` `resolveBottomBarLiquidGlassLensProgress`，:1856/1881/1929/2179/2546/2634/2967 整条 LiquidGlass 分支链。
   - `HomeGlassVisualPolicy.kt:172` `resolveHomeRefreshTipAppearance(liquidGlassEnabled=…)` 的 GLASS 分支不可达。
   - `core/ui/blur/RecoverableVisualEffects.kt:51/69` `shouldAllowHomeChromeLiquidGlass`/`shouldAllowDirectHazeLiquidGlassFallback` 仍被 HomeHeader/BottomBar import。
   - 测试仍覆盖：`HomeGlassVisualPolicyTest`、`BottomBar*PolicyTest`、`RecoverableVisualEffectsPolicyTest`、`BangumiLiquidGlassStructureTest`、`ReusableLiquidGlassBackdropStructureTest`、`BottomBarMatchedLiquidChromeStructureTest`、`VideoDetailScreenPolicyTest:245`（LiquidGlassToggle 断言）。
3. **story**：仅注释残留（PortraitVideoLoadPolicy.kt:44/140、PortraitPagerSwitchPolicy.kt:211 的 KDoc），功能已删干净。
4. **干净项确认**：anime4k、listenVideo、black-vinyl、easterEgg、entranceAnimation、RendererPreset、ICON_SETTINGS/TIPS_SETTINGS 全 0 命中；Cupertino 命中均为 Miuix 下拉刷新样式枚举与许可证文案，非残留。

## 3. 测试覆盖地图

规模：单元测试 **939 个类**（app 872 / design-system 50 / settings-core 1 / network-core 1）+ androidTest 15（app）。app 28 个 feature 目录全部有对应测试目录。

- home 98、video 242、settings 59、dynamic 39、list 30、live 19、download 18、search 17、bangumi 16、profile 14、space 13、plugin 10+、cast 8、message 7、login 5、following 4、onboarding 4、article/bangumi/partition/personal/screenshot/web/privacy/livesite/watchlater 较少。
- core（store ~20、network、theme、ui、blur、`*LintTest` ~9、transition ~10）、data/model ~26、data/repository ~30、navigation/navigation3 ~25。
- androidTest：dynamic 3、video 5、login 1、settings 1、home 1、tablet 1 + **2 个空壳 `ExampleInstrumentedTest`（`com.Android` 与 `com.example` 双包名残留，含大写包名不规范）**。
- **无直接单测的主屏幕/编排层**：HomeScreen 主编排、VideoDetailScreen、SettingsScreen/SettingsSections、LivePlayerScreen、LoginScreen（均只有 Policy 级测试拆分）。

## 4. 死依赖

- toml 无未使用的声明。注意：`implementation(libs.miuix.shader)`（app/build.gradle.kts:383）全代码库无 `top.yukonga.miuix.kmp.shader` import，疑似冗余（低风险，勿直接删，需确认由 miuix-blur 传递提供）。
- app/build.gradle.kts:360-540 大量字符串内联依赖未走 catalog（activity-compose、appcompat、biometric、retrofit、okhttp、richeditor-compose、cupertino 等 15+ 处）——规范性债，非死依赖。

## 5. CI 静态检查现状（阶段 1 接入点）

`.github/workflows/` 3 个文件，**无独立"结构测试/违规 import 检查" job**：
- **Build.yml**：guard job 跑 `:app:testDebugUnitTest --tests` 白名单 **10 个测试类**：`AppUpdateCheckerTest`、`AppVersionPolicyTest`、`ApkArtifactNamingPolicyTest`、`PreferenceKeyUniquenessTest`、6 个 lint/结构测试（HardcodedColor/Spacing/Typography/MigratedFeatureStyle/StyleLintAllowlistRatchet/FrameBudgetLint）。即仓库事实上的"结构守卫"= 这 10 个白名单测试。
- **ComposeReports.yml**：release 编译 + Compose reports（无断言守卫）。
- **PerfGuard.yml**：帧预算与稳定性棘轮（FrameBudgetLintTest 系）。

问题：guard 白名单不含任何 Home/Settings/视频结构测试（`HomeNavigationMiuixStructureTest` 等大量结构测试不在 CI 运行）；`AppUpdateCheckerTest`（纯网络更新检查）在守卫清单内职责存疑。阶段 1 的"违规 import 检查接入 PR 默认路径"应改造 guard job 或新增 job，并建立版本化例外清单（见 `ARCHITECTURE_AUDIT.md` §4）。

## 6. 建议清理最小清单（按风险排序）

**低风险（纯资源删除，不影响编译）**：10 个孤儿 drawable；23 个孤儿 Lottie raw JSON（先核查 LottieComponents.kt 无 R.raw 引用）；22 个孤儿 mipmap 根名（~123 文件）；孤儿 color；drawable-night/ic_telegram_squircle.xml。
**中风险（需先确认）**：核查 LottieComponents.kt 现状并清理其命名与结构测试绑定；清理 BottomBar.kt liquid-glass 恒 false 分支链 + RecoverableVisualEffects 两个函数 + 相关 6 个测试；确认 miuix.shader 声明可删。
**低风险收尾**：删除两个 ExampleInstrumentedTest 空壳；对齐 Build.yml guard 白名单与现行结构测试。

> 阶段 0 明确**不执行**上述清理（退出条件：不开始大规模代码移动）；清单作为后续阶段的输入。
