# Miuix 对齐记录

最后更新：2026-07-31

## 背景

本仓库通过 Maven Central 引入 `top.yukonga.miuix.kmp`（当前钉扎 **0.9.3**），并在
`AndroidNativeVariant.MIUIX` 下经由 `PresetPrimitiveRenderer.MIUIX_BRIDGED` 分发到官方组件。

Navigation3 需单独看待：Miuix 0.9.3 的 `miuix-navigation3-ui` 编译于 Navigation3 runtime
1.1.4，而当前应用使用官方 runtime/UI 1.2.0-alpha07。为避免 SceneState 与预测返回版本错配，
当前只保留 Miuix 主题/组件/blur/shader/squircle/icons，不使用 Miuix NavDisplay 实现。

上游发布说明：<https://github.com/compose-miuix-ui/miuix/releases/tag/v0.9.3>

## 本地结论（相对上游能力）

- `Miuix` 不是“只换颜色”的封装：有独立主题、颜色槽位、文字样式与 squircle / smooth rounding。
- 壳层组件包括 `TopAppBar`、`NavigationBar` / `FloatingNavigationBar`、`NavigationRail`、`TabRow`、
  `BasicComponent` / Preference、以及 0.9.3 新增的 `Badge` / `Tooltip`。
- 正文文字分层更接近 `17 / 16 / 14 / 13 / 11sp`，不是 Material 3 token 的直接镜像。

## 对 BiliPai 当前实现的判断

P0–P5 深度适配主路径已落地；后续属于可选加深，而非阻塞性缺口：

- 颜色通过 `Material ColorScheme -> Miuix Colors` 桥接 + `AppSurfaceTokens` 消费。
- 壳层 / Preference / 内容卡 / 播放器设置与迷你播放器壳 / Tooltip 均已挂 `MIUIX_BRIDGED`。
- 可选：首页视频卡更深 squircle、更多长按 Tooltip 面。

## 已落地

- Miuix 变体独立 typography / shapes / corner scale / smooth rounding。
- `AppSurfaceTokens` 语义色；feature 层禁止直读 `MiuixTheme.colorScheme`（结构测试守门）。
- 设置 Scaffold、分段 `TabRow`、搜索 `InputField`、列表 `BasicComponent` / `SwitchPreference` / `SliderPreference` / `ArrowPreference`。
- 首页 `AdaptivePullToRefreshBox` → 官方 `PullToRefresh`。
- 底栏官方 `NavigationBar` + `Badge`；0.9.3 起 `TextOnly` 映射为 `IconWithSelectedLabel`。
- 平板 `FrostedSideBar` / `AdaptiveSideNavigationRail` → 官方 `NavigationRail`（Expanded 可展开）。
- 播放器 `VideoSettingsPanel`：可点击项走 `ArrowPreference`，开关行走 `SwitchPreference`。
- 迷你播放器壳：`MiniPlayerOverlayShellPolicy`（更圆角、更扁 elevation、`AppSurfaceTokens.primary` 强调色）。
- 设置外观说明卡：`AdaptivePlainTooltipBox` → 官方 `TooltipBox`（长按/悬停）。
- 工具链：Kotlin `2.4.0` + KSP `2.3.10` + miuix `0.9.3`（含 `miuix-shader`）。
- `TextOnly`：MD3 设置保留；Miuix 路径映射为 `IconWithSelectedLabel`（非死分支，属 0.9.3 兼容）。

## 后续对齐顺序（深度适配）

1. ~~P1 壳层~~（底栏 Badge、平板 Rail）
2. ~~P2 Preference 主路径~~（Switch / Slider / Arrow）
3. ~~P3 内容面~~（`ContentCardSurfacePolicy` → 消息 / 搜索 / 动态 GlassCard）
4. ~~P4 播放器~~（设置 Preference + 迷你播放器壳）
5. ~~P5 Tooltip / 文档收尾~~

可选加深：首页视频卡 squircle、更多长按 Tooltip 接入点。

## 非目标

- 一次性去掉 MaterialTheme 单主题树
- 大规模重做 iOS / MD3
- 通过新增无关依赖解决风格问题
