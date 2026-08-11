# BiliPai 双主题系统重构执行计划（简版）

> 状态：历史方案草案，已被[前端架构与主题精简优化计划](FRONTEND_ARCHITECTURE_THEME_SIMPLIFICATION_PLAN.md)取代；不得将本文的旧基线或步骤视为当前事实。

> 目标：让 MIUIX 与 Material 3 共用一套业务 UI 代码，主题差异集中在 design-system 内部。
>
> 执行对象：DeepSeek v4 Flash 或其他小尺寸代码模型。
>
> 原始基线：2026-08-06，记录保留用于追溯。

## 1. 最终目标

运行时只保留：

- MIUIX
- MATERIAL3

业务 feature 不得直接依赖：

- UiPreset
- AndroidNativeVariant
- UiStyle.IOS
- LocalUiPreset
- LocalAndroidNativeVariant
- PresetPrimitiveRenderer
- Material 3/Miuix 高层控件

业务页面只调用中性 App* 组件，例如：

~~~text
AppText
AppIcon
AppButton
AppSurface
AppCard
AppSwitch
AppSlider
AppTextField
AppSearchField
AppSegmentedControl
AppScaffold
AppTopBar
AppNavigationBar
AppNavigationRail
AppAlertDialog
AppModalBottomSheet
AppLoadingIndicator
AppPullRefresh
AppPreference
~~~

多数 App* 组件已存在于 design-system（core/ui/components/*、AppSheetComponents.kt 等），本计划对它们是检查与补强，不新建同义组件。

MIUIX 和 Material 3 可以使用不同的底层 Compose 组件，但必须保持相同的业务回调、状态含义、无障碍语义和最小 48dp 触摸区域。

## 2. 当前主要问题

主题数据已经有 theme_selection_v1，但运行时仍然绕回旧模型：

~~~text
theme_selection_v1
 -> UiStyle
 -> legacyWritePlan()
 -> UiPreset + AndroidNativeVariant
 -> PureBiliBiliTheme
~~~

当前架构审计已知问题：

~~~text
STYLE_FEATURE=5
LOCAL_FEATURE=4
RENDERER_FEATURE=1
VENDOR_COMPONENT_FEATURE=13
UNREGISTERED=17
STALE=1
REGISTRY_EVIDENCE_VIOLATIONS=4
STYLE_PRODUCTION=12（审计基线上限 7，需一并归零）
~~~

目标：

~~~text
STYLE_FEATURE=0
LOCAL_FEATURE=0
RENDERER_FEATURE=0
VENDOR_COMPONENT_FEATURE=0
UNREGISTERED=0
STALE=0
REGISTRY_EVIDENCE_VIOLATIONS=0
STYLE_PRODUCTION=0
~~~

保留旧枚举只用于历史数据迁移和旧设置文件兼容，不要让它们继续进入运行时主题树。

## 3. 目标架构

~~~text
DataStore
  -> LegacyThemeMigration（只处理旧键）
  -> AppThemeSettings(uiStyle = MIUIX/MATERIAL3)
  -> AppDesignSystemTheme
      -> LocalAppUiStyle
      -> Semantic Tokens
      -> MiuixTheme + MaterialTheme
      -> Internal Miuix/Material3 Renderer
  -> Feature UI
      -> App* Facade
~~~

建议新增或统一：

~~~kotlin
enum class AppUiStyle {
    MIUIX,
    MATERIAL3,
}
~~~

AppThemeSettings 直接保存 AppUiStyle，不再保存 UiPreset + AndroidNativeVariant。

主题宿主保留同时嵌套 MiuixTheme 和 MaterialTheme 的结构，因为项目已经使用 Material 颜色桥接到 Miuix。

## 4. 执行阶段

### 阶段 0：统一文档和审计基线

修改范围：

- docs/wiki/FRONTEND_ARCHITECTURE_THEME_SIMPLIFICATION_PLAN.md
- docs/wiki/ui-design/03_THEMES.md
- docs/UI_COMPONENT_REGISTRY.csv
- docs/UI_ARCHITECTURE_BASELINE.json

任务：

1. 把文档中的“三风格”统一改为“两主题”。
2. 说明 iOS 只属于历史迁移兼容。
3. 同步当前 17 个未登记文件、1 个过期文件和 4 个证据错误。
4. 不降低审计阈值。

验证：

~~~powershell
rtk powershell -NoProfile -ExecutionPolicy Bypass -File scripts/ui_architecture_audit.ps1
rtk powershell -NoProfile -ExecutionPolicy Bypass -File scripts/ui_architecture_audit_self_test.ps1
~~~

### 阶段 1A：新增 AppUiStyle 并统一运行时主题类型

重点文件：

- design-system/src/main/java/com/android/purebilibili/core/theme/UiPreset.kt
- design-system/src/main/java/com/android/purebilibili/core/ui/AppThemeSelection.kt
- app/src/main/java/com/android/purebilibili/core/store/theme/ThemeSelectionStore.kt
- app/src/main/java/com/android/purebilibili/feature/settings/SettingsViewModel.kt
- app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt
- 外观设置相关策略与测试（AppearanceUiPresetDescriptionPolicy、AppearanceUiPresetSegmentPolicy 等）

任务：

1. 新增 AppUiStyle，两值：MIUIX、MATERIAL3。做法是就地收敛：把现有 UiStyle 改名为 AppUiStyle 并删除 IOS，不要保留三份并行枚举。
2. 删除 AppThemeSelection，全部调用方统一使用 AppUiStyle。
3. 设置页同步收敛为两个选项（MIUIX / Material 3），旧 iOS 选项随枚举删除而消失；同步更新相关策略和测试（只更新断言，不删除测试）。
4. 持久化字符串保持 "MIUIX" / "MATERIAL3" 不变，已写入的 theme_selection_v1 数据零迁移。
5. 保留旧键迁移逻辑：UiPreset / AndroidNativeVariant 保留为遗留 int 映射类型，fromLegacyValues 改为返回 AppUiStyle 的迁移函数。
6. 所有缺失、非法、旧 iOS 值默认迁移为 MIUIX；旧 MD3 + MIUIX 迁移为 MIUIX，旧 MD3 + MATERIAL3 迁移为 MATERIAL3。

验证：

~~~powershell
rtk .\gradlew.bat :design-system:testDebugUnitTest --tests '*UiPreset*' --tests '*UiStyle*' --no-daemon --no-configuration-cache --console=plain
rtk .\gradlew.bat :app:testDebugUnitTest --tests '*ThemeSelectionMigrationPolicyTest' --tests '*AppThemeSettingsMappingPolicyTest' --no-daemon --no-configuration-cache --console=plain
~~~

### 阶段 1B：AppThemeSettings 直接保存 AppUiStyle

重点文件：

- app/src/main/java/com/android/purebilibili/core/store/SettingsManager.kt
- 相关主题迁移测试

任务：

1. AppThemeSettings 删除 uiPreset + androidNativeVariant 字段，替换为 uiStyle: AppUiStyle。
2. SettingsManager 对外只暴露 AppUiStyle；读写路径内部完成旧键读取与一次性迁移（沿用 ThemeSelectionStore 现有机制，不重建）。
3. 删除运行时对 legacyWritePlan() 的依赖，legacyWritePlan 仅保留在迁移边界。
4. 保留 getAppThemeSettings → ensureThemeSelectionMigrated → emit 的首帧顺序。

验证：同阶段 1A 的第二条命令。

### 阶段 2A：修改主题宿主（含兼容桥接）

重点文件：

- app/src/main/java/com/android/purebilibili/core/theme/Theme.kt
- app/src/main/java/com/android/purebilibili/MainActivity.kt
- app/src/main/java/com/android/purebilibili/core/store/SettingsManager.kt
- app/src/main/java/com/android/purebilibili/core/store/OnboardingSettingsStore.kt
- app/src/main/java/com/android/purebilibili/core/store/HomeSettingsUiPresetPolicy.kt
- app/src/main/java/com/android/purebilibili/core/util/ModifierExt.kt
- 主题桥接测试

任务：

1. PureBiliBiliTheme 改为接收 AppUiStyle。
2. 新增 LocalAppUiStyle；同时继续提供 LocalUiPreset / LocalAndroidNativeVariant 兼容值（由 AppUiStyle 映射生成）——这是阶段 2B 迁移期间的桥接，禁止在 2B 完成前删除旧 Local。
3. 保留 MiuixTheme + MaterialTheme 同时提供。
4. 保持动态颜色、AMOLED、字体、主题色覆盖行为。
5. 删除正常运行时的 UiPreset.IOS 分支。
6. ModifierExt.kt 的 LocalUiPreset / MD3 分支迁移到 AppUiStyle（MD3 → MATERIAL3，按压缩放与阻尼参数保持原值）。
7. OnboardingSettingsStore、HomeSettingsUiPresetPolicy 改用 AppUiStyle（这三个文件属于 STYLE_PRODUCTION=12 的审计缺口，必须在本阶段处理，否则最终回归无法通过）。
8. 保留 getAppThemeSettings → ensureThemeSelectionMigrated → emit 的首帧顺序，避免主题闪切。
9. 不改变导航、滚动、搜索、播放和 PIP 状态。

验证：

~~~powershell
rtk .\gradlew.bat :design-system:testDebugUnitTest --tests '*Theme*' --tests '*AppSurfaceTokensTest' --no-daemon --no-configuration-cache --console=plain
rtk .\gradlew.bat :app:compileDebugKotlin --no-daemon --no-configuration-cache --console=plain
~~~

### 阶段 2B：design-system 内部旧主题模型分批迁移

背景：design-system 的 27 个生产文件仍直接使用 UiPreset / AndroidNativeVariant / LocalUiPreset / LocalAndroidNativeVariant（约 391 处文本命中）。审计只扫描 app 模块看不到这部分，但 §7 完成条件要求"旧主题 pair 只存在于迁移边界"，仅靠 2A 无法达成，必须显式分批清理。

重点文件（每批 3～6 个，按依赖顺序）：

- 第 1 批（Token/Policy）：AppShapes.kt、AppSurfaceTokens.kt、AppMotionTokens.kt、AppChromeSizeTokens.kt、AppSemanticVisualPolicy.kt
- 第 2 批（Adaptive 控件）：AdaptiveChrome.kt、AdaptiveDialogComponents.kt、AdaptiveScaffoldPolicy.kt、AdaptiveSideNavigationRailPolicy.kt、AdaptiveTooltipPolicy.kt
- 第 3 批（列表/偏好）：AdaptivePreferenceComponents.kt、AdaptiveListItemPolicy.kt、AdaptiveLoadingIndicatorPolicy.kt、AdaptivePullToRefreshPolicy.kt、AppAdaptiveSwitchPolicy.kt
- 第 4 批（App 组件）：AppSheetComponents.kt、AppSegmentedControl.kt、AppSegmentedControlPolicy.kt、AppPullRefreshIndicator.kt、AppPrimaryButton.kt、BottomBarContentPaddingPolicy.kt
- 第 5 批（其余 + Renderer）：ContentCardSurfacePolicy.kt、AppSquircleModifiers.kt、PresetPrimitiveRenderer.kt、AndroidNativeVariantThemePolicy.kt、IosContinuousCornerShape.kt
- UiPreset.kt 本身作为迁移边界文件保留（只含遗留枚举与迁移函数，不放回运行时主题树）

任务：

1. 每批把旧 Local 读取改为 LocalAppUiStyle / AppUiStyle 参数。
2. 语义映射固定：MIUIX → 原 MIUIX 行为；MATERIAL3 → 原 MD3 + MATERIAL3 行为；原 iOS 专属行为不产生新运行时分支（已由迁移表落到 MIUIX）。
3. 每批在已有测试模式附近补纯 Kotlin policy 测试。
4. 全部批次完成后，确认 design-system 不再读取旧 Local，再执行阶段 6 的兼容 Local 删除。

验证：每批 `:design-system:testDebugUnitTest` + `:app:compileDebugKotlin`。

### 阶段 3：检查并增强公共组件边界

现状：facade 已存在（AppPrimitiveComponents.kt、AppSheetComponents.kt、AppNavigationComponents.kt、AppPreferenceComponents.kt、AppSegmentedControl.kt 等），本阶段**不新建同义组件**，只做检查与补强：

1. 核对每个 App* 组件已改为读取 LocalAppUiStyle，不再读旧 Local（依赖 2B 进度，AppSheetComponents 等仍在旧 Local 清单内）。
2. 保证两个 Renderer 的回调和状态语义一致。
3. AppModalBottomSheet 的弹层宿主契约独立成政策函数，业务页不允许自行判断，也不允许机械替换：

~~~kotlin
// 示例：resolveAppBottomSheetHostPolicy
fun resolveBottomSheetHost(style: AppUiStyle): BottomSheetHost = when (style) {
    AppUiStyle.MIUIX -> BottomSheetHost.MIUIX_OVERLAY     // OverlayBottomSheet
    AppUiStyle.MATERIAL3 -> BottomSheetHost.MATERIAL3     // Material3 ModalBottomSheet
}
~~~

注意：OverlayBottomSheet 与 ModalBottomSheet 不仅外观不同，弹层宿主也不同（Miuix 弹层依赖 overlay host），直接替换会导致 MIUIX 下点击无效或弹层不显示。

组件核对优先级（原有，仅核对与补强，不新增能力）：

1. Surface/Card/Text/Icon。
2. Button、Switch、Checkbox、Slider。
3. TextField、SearchField、SegmentedControl。
4. Dialog、BottomSheet、Snackbar。
5. Loading、Progress、PullRefresh。
6. Scaffold、TopBar、NavigationBar、NavigationRail。

每个组件至少有一个纯 Kotlin policy 测试，并确保两个 Renderer 的回调和状态语义一致。

### 阶段 4：迁移业务 feature

每批只修改 3～8 个生产文件。

#### 批次 4A：搜索

先迁移纯逻辑文件 SearchVideoFilterPolicy.kt，再迁移 SearchVideoFilterSheet.kt；Sheet 迁移依赖阶段 2B/3 的宿主契约（FilterChip、Checkbox、DropdownMenu、ModalBottomSheet 的 facade 均已存在，检查补强即可，不新建）。

优先迁移：

- app/src/main/java/com/android/purebilibili/feature/search/SearchLandingUi.kt
- app/src/main/java/com/android/purebilibili/feature/search/SearchScreen.kt
- app/src/main/java/com/android/purebilibili/feature/search/SearchTrendingScreen.kt
- app/src/main/java/com/android/purebilibili/feature/search/SearchVideoFilterSheet.kt
- app/src/main/java/com/android/purebilibili/feature/search/TopicDetailScreen.kt
- app/src/main/java/com/android/purebilibili/feature/search/SearchVideoFilterPolicy.kt

删除主题 Local 和 Renderer 依赖，替换直接使用的 Surface、Text、Icon、Button、FilterChip、Checkbox、DropdownMenu、ModalBottomSheet。SearchVideoFilterSheet 保留现有宿主政策（MIUIX → OverlayBottomSheet / MATERIAL3 → ModalBottomSheet），只把判断源换成 AppUiStyle。保留搜索状态、焦点、键盘、分页和返回行为。

#### 批次 4B：动态、设置弹窗、视频设置

优先迁移：

- app/src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCard.kt
- app/src/main/java/com/android/purebilibili/feature/settings/update/AppUpdateDialogHost.kt
- app/src/main/java/com/android/purebilibili/feature/video/ui/components/Anime4KSettingsUi.kt
- app/src/main/java/com/android/purebilibili/feature/video/ui/components/AudioQualitySelectionMenu.kt
- app/src/main/java/com/android/purebilibili/feature/video/ui/components/DanmakuSettingsPanel.kt
- app/src/main/java/com/android/purebilibili/feature/video/ui/components/QualityMenu.kt
- app/src/main/java/com/android/purebilibili/feature/video/ui/components/SponsorSkipUI.kt
- app/src/main/java/com/android/purebilibili/feature/video/ui/overlay/BottomControlBar.kt

禁止修改播放器输出、HDR、SurfaceView、PIP、弹幕渲染和核心手势状态机。

#### 批次 4C：Home、Live、Profile、Settings

先迁移公共控件，再处理页面级特例。不要一次性重写 BottomBar.kt、HomeScreen.kt 或 AppearanceSettingsScreen.kt。

### 阶段 5：图标和设置分享

1. feature 只使用 AppIcons 语义入口。
2. Miuix Icons、Material Icons、Cupertino Icons 只允许存在于图标实现层或登记的临时例外。
3. 新设置分享格式使用 uiStyle=MIUIX/MATERIAL3。
4. 旧 uiPresetValue 和 androidNativeVariantValue 仅用于旧文件导入兼容。
5. 新导入数据统一经过 AppUiStyle 解析。

### 阶段 6：清理过渡代码

在前面阶段全部通过后：

1. 删除 PresetPrimitiveRenderer.IOS 及依赖它的 Ios* 组件壳与 iOS 专属 Token（如 IosContinuousCornerShape、IosLoadingIndicator）。
2. 删除兼容 Local（LocalUiPreset / LocalAndroidNativeVariant）——仅当 design-system 内部确认不再读取后执行（阶段 2B 全部完成）。
3. 删除 feature 中的旧主题 Local 和 Renderer。
4. 修复 UI_COMPONENT_REGISTRY。
5. 确认 app 没有 Miuix 高层控件直接调用后，再评估移除 app 的直接 Miuix 依赖。
6. 不要为了清理 Material Icons 而使用语义错误的替代图标。

### 阶段 7：最终回归

~~~powershell
rtk powershell -NoProfile -ExecutionPolicy Bypass -File scripts/ui_architecture_audit.ps1
rtk powershell -NoProfile -ExecutionPolicy Bypass -File scripts/ui_architecture_audit_self_test.ps1
rtk .\gradlew.bat :design-system:testDebugUnitTest --no-daemon --no-configuration-cache --console=plain
rtk .\gradlew.bat :app:testDebugUnitTest --no-daemon --no-configuration-cache --console=plain
rtk .\gradlew.bat :app:compileDebugKotlin --no-daemon --no-configuration-cache --console=plain
rtk .\gradlew.bat :app:lintDebug --no-daemon --no-configuration-cache --console=plain
~~~

人工验收由维护者执行，不读取或生成截图：

- MIUIX / Material 3。
- 浅色 / 深色 / AMOLED。
- 手机和 840dp 以上平板。
- 首页、搜索、设置、视频播放、下载 Sheet、评论、个人空间、直播。
- 切换主题后业务状态不丢失。
- 1.3 倍字体不遮挡操作。
- TalkBack 能读出按钮、开关、选中、进度和错误恢复动作。

## 5. 给 DeepSeek v4 Flash 的任务模板

每次只发送一个小任务，不要一次交付整个计划。

~~~text
你正在修改 BiliPai-miuix。

任务：<一个明确的小任务>

目标：<一句话说明>

允许修改文件：
- <最多 3～8 个文件>

禁止修改：
- 网络、播放器输出、插件协议、Gradle wrapper
- 与本任务无关的 feature
- 测试删除、skip、ignore、baseline、审计阈值

先读取：
- AGENTS.md
- 本计划对应章节
- 目标文件附近的测试

执行：
1. 先搜索现有等价 API。
2. 先更新或补充纯 Kotlin 测试。
3. 使用 apply_patch 小步修改。
4. 运行本任务的验证命令。
5. 超出范围的问题只报告，不顺手修复。

最后输出：
- 修改文件
- 行为变化
- 测试命令和结果
- 未完成事项
~~~

模型必须停止并报告的情况：

- 需要修改未列出的模块。
- 发现导航、播放或业务状态需要改变。
- 需要增加依赖。
- 需要删除迁移兼容代码。
- 需要读取截图或做视觉判断。
- 需要改变审计阈值或增加永久例外。

## 6. 每批次完成条件

~~~text
[ ] 只修改了任务允许的文件
[ ] 没有新增 feature 主题 Local 依赖
[ ] 没有新增 Material3/Miuix 高层控件直接调用
[ ] 没有引入 iOS 运行时分支
[ ] 没有改变业务状态、回调、导航和播放器输出
[ ] 有对应纯 Kotlin 测试
[ ] 相关 Gradle 测试通过
[ ] compileDebugKotlin 通过或记录环境失败
[ ] 审计没有新增问题
[ ] STYLE_PRODUCTION 不超过基线 7，且本批次没有新增旧模型引用
[ ] UI_COMPONENT_REGISTRY 同步
[ ] git diff --check 通过
[ ] 变更可以单独回滚
~~~

## 7. 最终完成条件

1. 运行时主题类型只有 MIUIX 和 MATERIAL3。
2. 旧主题 pair 只存在于迁移和旧文件兼容边界；STYLE_PRODUCTION = 0（若迁移边界必须保留引用，需逐文件列出清单，不允许模糊表述）。
3. feature 不直接读取主题 Local、Renderer 或厂商高层控件。
4. VENDOR_COMPONENT_FEATURE=0。
5. STYLE_FEATURE=0。
6. LOCAL_FEATURE=0。
7. RENDERER_FEATURE=0。
8. 组件登记表没有未登记或过期文件。
9. design-system 不反向依赖 app、feature、store 或业务数据。
10. 单测、编译、lint、架构审计全部通过。
11. MIUIX/Material 3、明暗主题、手机/平板文字验收通过。
12. 主题切换不重置业务状态，不改变按钮含义，不破坏返回和播放器行为。

## 8. 推荐第一批

不要从整个 Theme.kt 或 BottomBar.kt 开始：

1. 阶段 0：统一文档和审计登记表。
2. 阶段 1A：新增 AppUiStyle，补齐迁移测试。
3. 阶段 1B：让 AppThemeSettings 直接保存 AppUiStyle。
4. 阶段 2A：更新 PureBiliBiliTheme、MainActivity 和兼容桥接（含 ModifierExt / OnboardingSettingsStore / HomeSettingsUiPresetPolicy）。
5. 阶段 2B 第 1 批：Token/Policy 5 个文件。
6. 阶段 3 首批：AppSheetComponents.kt 的宿主契约政策。
7. 阶段 4A：先迁 SearchVideoFilterPolicy.kt，再迁 SearchVideoFilterSheet.kt。
8. 运行主题测试、编译和架构审计。

