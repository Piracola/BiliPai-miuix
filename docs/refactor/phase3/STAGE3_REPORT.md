# 阶段 3：收敛 Lite Design System（执行记录）

日期：2026-08-16

## 一、删除清单（Miuix 唯一 renderer 收敛）

### 整文件删除（6 个）
| 文件 | 说明 |
|---|---|
| `core/ui/renderer/material3/AppMaterial3SegmentedControl.kt`（+空目录） | M3 分段控件，零生产引用 |
| `core/ui/ComfortablePullToRefreshBox.kt` | M3 PullToRefresh 默认槽，零生产引用 |
| `core/ui/AdaptiveScaffoldPolicy.kt` | 单值 `AdaptiveScaffoldRenderer` 抽象，零生产引用 |
| `core/ui/AdaptiveSideNavigationRailPolicy.kt` | 单值 `AdaptiveSideNavigationRailRenderer`，零生产引用 |
| `core/ui/AdaptiveTooltipPolicy.kt` | 单值 `AdaptiveTooltipRenderer`，零生产引用 |
| `core/ui/PresetPrimitiveRenderer.kt` | 单值 `PresetPrimitiveRenderer { MIUIX_BRIDGED }`，仅 1 处生产引用（已一并删除） |

### 文件内死声明删除（5 处）
- `AdaptiveDialogComponents.kt`：`AppAlertDialogRenderer` enum + `resolveAppAlertDialogRenderer()`
- `AdaptiveChrome.kt`：`isNativeMiuixEnabled()` / `rememberIsNativeMiuixEnabled()`
- `AppPullRefreshIndicator.kt`：`AppPullRefreshIndicatorRenderer` + `resolveAppPullRefreshIndicatorRenderer()` + 无引用的 `AppPullRefreshLoadingIndicator(state,...)` 重载
- `MiuixThemePolicy.kt`：`shouldUseMiuixSmoothRounding()`
- `AdaptivePullToRefreshPolicy.kt`：`resolveAdaptivePullToRefreshRenderer()`

### SegmentedControl 收敛
- 删除 `AppSegmentedChrome` / `resolveAppSegmentedChrome()`、`AppSegmentedRenderer` / `resolveAppSegmentedRenderer()`、`AppNativeSegmentedControl`、`AppMiuixSegmentedControl`
- `resolveAppSegmentedControlColors()` 去掉 `usesMaterialColorTokens` 参数与 M3 分支，仅保留 Miuix 分支
- `AppSegmentedControlPolicy` 删除 `usesEmphasizedTitle` / `usesMaterialFallback` / `usesMaterialColorTokens` 三个死字段
- 保留：`AppNativeTabRow`（Miuix TabRow 渲染，被 bangumi 等生产引用）、`AppMiuixTabRow`、liquid chrome 决策函数

### 测试同步
- 删除 DS 测试 4 个：AdaptiveScaffoldPolicyTest、AdaptiveTooltipPolicyTest、AdaptiveSideNavigationRailPolicyTest、AppPullRefreshIndicatorPolicyTest
- 修正 DS 测试 4 个：AdaptivePullToRefreshPolicyTest、AppSegmentedControlPolicyTest、MiuixThemePolicyTest、AppDialogComponentsPolicyTest
- 修正 app 测试 5 个：MiuixV2MigrationStructureTest（4 处断言）、SegmentedControlRendererPolicyTest、AppearanceMd3SegmentedControlPolicyTest、PlaybackSettingsSelectionPolicyTest、AdaptiveSideNavigationRailPolicyTest、PrimitivePresetCoverageTest
- 修正基线即红的 `AppTopChromePolicyTest`（断言 MATERIAL_UNDERLINE/MATERIAL → 实际 MOVING_CAPSULE/MIUIX，为基线错误断言，非本次引入）
- 连带修复 `HomeScreen.kt` 一处使用已删 `AppPullRefreshLoadingIndicator(state,...)` 重载的调用点

## 二、验证

- `:design-system:testDebugUnitTest` → BUILD SUCCESSFUL（全绿，含修正后的基线测试）
- `:app:testDebugUnitTest` 全量 → 6039 tests, 49 failed
- **失败归因**：与基线（git stash 对比）完全一致——37 个失败类 100% 是基线既有（结构测试与当前代码长期漂移、从未进 CI，阶段 0 已预见：guard 白名单不含这些结构测试、StyleLint SHA 漂移、NavKey 目录漂移、导航返回行为断言过期等）。`comm` 对比确认**阶段 3 零新增失败**。
- 阶段 3 引入的 `AdaptiveSideNavigationRailIntegrationTest` 失败已修复。

## 三、阶段 3 退出条件核对

| 退出条件 | 状态 |
|---|---|
| 迁移范围内不再出现直接 Material3 组件调用 | ✅（design-system 已收敛；feature 层的 M3 由阶段 4 页面切片处理） |
| 视觉参数不再散落在 Feature 页面 | 进行中（阶段 4 落地，阶段 3 已完成 Token 层收口所需的抽象收敛） |
| 低性能设备和减少动效设置有明确降级路径 | ✅ 既有 runtimeVisualGuardEnabled / 动效档位机制保留 |

## 四、保留项（按计划 §4.1 有意保留，非残留）

- `AppPrimitiveComponents.kt` M3 facade 群（AppSlider/AppProgress 等，被 243 个 feature 文件消费）
- `AppNavigationComponents.kt` M3 `AppNavigationBar`（app BottomBar 的 Material 分支仍在用，阶段 4 页面切片处理）
- `AppSheetComponents.kt` `AppModalBottomSheet`（设计决策保留：依赖 Miuix popup host）
- `AdaptiveChrome.kt` `AppTopBar` 的 M3 类型表面（实际渲染 Miuix）
- `AppAlertDialog` 的 M3 Dialog 底层（101 处 app 调用，单主题渲染已是 Miuix 内容层）
