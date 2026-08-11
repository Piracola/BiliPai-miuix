# 阶段 0 基线审计报告（主题与前端架构精简）

> 关联计划：[前端架构与主题精简优化计划](FRONTEND_ARCHITECTURE_THEME_SIMPLIFICATION_PLAN.md)
> 状态：历史阶段 0 快照。数值和源码路径只描述 2026-08-04 的审计起点，不是当前实现事实。
> 清单生成命令：[theme_simplification_inventory.ps1](../../scripts/theme_simplification_inventory.ps1)
> 生成产物：`docs/theme_simplification_inventory/*.csv`

## 1. 基线记录

| 项 | 值 |
| --- | --- |
| 实施起点 commit | `a9f6a55006920288b355fd39b6580ba1e41a06aa`（2026-08-04 17:35:52 +0800） |
| 检索日期 | 2026-08-04 |
| Miuix 版本 | 0.9.3（ui / preference / blur / shader / squircle / icons） |
| Cupertino 依赖 | `cupertino:0.1.0-alpha04`、`cupertino-adaptive:0.1.0-alpha04`、`cupertino-icons-extended:0.1.0-alpha04` |
| Material Icons | `androidx.compose.material:material-icons-extended`（app 直接依赖，design-system `api` 暴露） |
| 计划声明的事实基线 | `.trae/specs/audit-frontend-architecture-theme-simplification/review-report.md` **不存在**（`.trae/specs/` 为空），本报告替代为新的清单基线 |

## 2. 清单汇总（生产源码 app/src/main + design-system/src/main，共 1123 个 Kotlin 文件）

| 类别 | 文件数 | 出现次数 | 说明 |
| --- | --- | --- | --- |
| Cupertino 全部 import | 89 | 287 | `io.github.alexzhirkevich.cupertino.*` |
| └ Cupertino Icons | 86 | 280 | 其中 app 83 文件 / 258 次，design-system 3 文件 / 22 次 |
| └ Cupertino 控件（非 icons） | 5 | 7 | `CupertinoSlider`（DefaultPlaybackSpeedPreferenceControl）、`CupertinoActivityIndicator`（HomeScreen）、`CupertinoSwitch`（AdFilterPlugin）及 design-system 2 处 |
| cupertino-adaptive | **0** | **0** | 无任何 import，依赖可先删 |
| Material Icons | 73 | 451 | app 71 文件 / 393 次，design-system 2 文件 / 58 次（AppIcons.kt 54 + AdaptivePreferenceComponents.kt 4） |
| Miuix Icons 直接 import | 5 | 27 | `top.yukonga.miuix.kmp.icon.*` 泄漏到 feature |
| iOS 符号（iOS* 前缀） | 51 | 445 | app 39 文件 / 367 次，design-system 12 文件 / 78 次；主体是 Color.kt 语义色板常量 |
| 主题枚举引用 | 43 | 640 | `UiPreset` / `AndroidNativeVariant` / `UiStyle` / `AppThemeSelection` / `PresetPrimitiveRenderer` |
| 旧主题键 | 1 | 16 | `ui_preset` / `android_native_variant_v1`，全部在 SettingsManager |

## 3. 关键结构事实（影响各阶段设计）

1. **四套平行枚举**：`UiPreset`(IOS/MD3) + `AndroidNativeVariant`(MATERIAL3/MIUIX) 持久化于旧键；`UiStyle`、`AppThemeSelection`、`PresetPrimitiveRenderer` 均为派生模型。
2. **默认值仍为 IOS/MATERIAL3**：[UiPreset.kt](../../design-system/src/main/java/com/android/purebilibili/core/theme/UiPreset.kt) 中 `LocalUiPreset = { IOS }`、`fromValue` 兜底 `IOS`；[Theme.kt](../../app/src/main/java/com/android/purebilibili/core/theme/Theme.kt) 中 `PureBiliBiliTheme(uiPreset = IOS)`；`SettingsViewModel.SettingsUiState(themeSelection = IOS)`。
3. **AppIcons.kt 已是语义入口但按主题分发**：每个 `rememberApp*Icon()` 都按 `(LocalUiPreset, LocalAndroidNativeVariant)` 返回 Cupertino 或 Material 图标（[AppIcons.kt](../../design-system/src/main/java/com/android/purebilibili/core/ui/AppIcons.kt)），共 54 个 Material + 17 个 Cupertino import。阶段 5 的核心改动点是去掉分发、改为单一 Miuix 风格映射。
4. **旧键读写位置**：[SettingsManager.kt](../../app/src/main/java/com/android/purebilibili/core/store/SettingsManager.kt) 定义 L1107-1108，读取 L1778-1780（AppThemeSettings）与 L1967-1969（getUiStyle），写入 L1952-1977（setUiPreset/setAndroidNativeVariant/setUiStyle），分享定义 L6308-6309。
5. **iOS 色板 = 应用语义色**：[Color.kt](../../design-system/src/main/java/com/android/purebilibili/core/theme/Color.kt) 的 23 个 `iOS*` 常量（iOSPink 点赞、iOSYellow 投币、iOSOrange 收藏、iOSTeal 评论、iOSPurple 三连、iOSSystemGray* 层级灰）被 feature 大量使用，属于"通用能力历史命名"，应改中性名而非删除。
6. **iOS 专属残留**：`IosLoadingIndicator.kt`、`IosContinuousCornerShape.kt`、Theme.kt 的 iOS 配色生成路径（createIosColorScheme/rememberIosColorScheme/alignIosColorSchemeWithDynamicAccent）。`IOSSectionTitle` 等旧包装组件已不存在（审计脚本模式零命中），说明此前已部分清理。
7. **已有迁移护栏**：`MiuixV2MigrationStructureTest`、`BottomBarMiuixStructureTest`、`SettingsMiuixSimplificationStructureTest`、`DynamicNeutralUiStructureTest`、`HomeNavigationMiuixStructureTest` 等结构测试已存在，阶段 4/5 每批的"零直接 import"可沿用该模式扩展。

## 4. 命中项分类

### A. iOS 专属（删除，归属阶段 3/6）
- `design-system/.../IosLoadingIndicator.kt`、`IosContinuousCornerShape.kt`
- Theme.kt 的 `createIosColorScheme` / `rememberIosColorScheme` / `alignIosColorSchemeWithDynamicAccent`（iOS 预设色板）
- `UiPreset.IOS` 分支、`PresetPrimitiveRenderer.IOS`、`AppThemeSelection.IOS`、`UiStyle.IOS`
- 设置 UI 的 iOS 选项（AppearanceUiPresetSegmentPolicy / DescriptionPolicy）、SettingsViewModel 的 IOS 默认值
- 测试与文档中对 iOS 三主题的断言

### B. 通用能力历史命名（改中性名，归属阶段 3）
- `Color.kt` 的 `iOS*` 语义色板（点赞/投币/收藏/评论/三连/层级灰）→ 中性名；影响 app 39 文件 367 处引用
- `resolveMd3ThemeSeedColor` 的 `iOSSystemBlue` 兜底 → 中性兜底色
- Theme.kt 中 iOS 相关的颜色注释与常量命名

### C. 图标来源（收敛到 AppIcons 语义入口，归属阶段 5）
- Cupertino Icons：86 文件 280 次
- Material Icons：73 文件 451 次
- Miuix Icons 直接 import（非底栏）：HomeHeader.kt(4)、TopBar.kt(3)、GestureLevelOverlayPolicy.kt(3) → 收敛到 AppIcons
- AppIcons.kt 本身的 Cupertino/Material import 与按主题分发逻辑

### D. 测试/文档（归属阶段 1/7）
- 断言三主题状态的策略测试：AppearanceUiPresetSegmentPolicyTest、AppearanceUiPresetDescriptionPolicyTest、SettingsLanguageStateTest（断言默认 IOS）、PrimitivePresetCoverageTest、Adaptive*PolicyTest 等
- 文档：`03_THEMES.md`（三风格）、`ARCHITECTURE.md`、`ROADMAP.md`、ui-design README 与页面档案、`QA.md`

### E. 可直接删除（归属阶段 2/6）
- `cupertino-adaptive` 依赖（0 import，可独立变更先行删除）
- 旧主题键 `ui_preset` / `android_native_variant_v1`（迁移完成后删除，16 处均在 SettingsManager）

## 5. 图标批次规划（阶段 5 建议批次 → 实际文件映射）

| 批次 | 语义域 | 覆盖文件（feature 分布） | 验证方式 |
| --- | --- | --- | --- |
| 5.1 | 返回/搜索/更多/关闭/确认/警告/设置 | AppIcons.kt 现有 `resolveApp*Icon` 全部改为单一 Miuix 映射（一次性收敛 design-system 侧） | design-system 单测 + `:design-system:compileDebugKotlin` |
| 5.2 | 媒体：播放/暂停/进度/音量/字幕/投屏 | feature/video（Cupertino 37 文件 / Material 27 文件）、feature/live（Material 11 文件）、feature/bangumi（Cupertino 5 文件）、feature/audio（Cupertino+Material 2 文件） | `:app:compileDebugKotlin` + 目标测试 |
| 5.3 | 功能：设置/账号/隐私/下载/消息 | feature/settings（Cupertino 15 文件 / Material 5 文件）、feature/download（Cupertino 2 + Material 3）、feature/message（Material 2）、feature/profile（Material 3）、feature/search（Material 2） | 同上 |
| 5.4 | 长尾 Cupertino 图标 | feature/partition（13 次）、feature/plugin（7 文件）、feature/home 卡片（5 文件）、feature/login/onboarding/category/list/space/watchlater/cast/dynamic | 同上 |
| 5.5 | Material Icons 直接调用清零 | 其余 Material 直接调用文件（含 TopBar 18 次、PartitionScreen 41 次、SpaceScreen 16 次等重度文件） | 同上 + 结构测试 |
| 5.6 | 首页导航自有 outlined/filled 资产 | BottomBar.kt、HomeNavigationIconPolicy.kt（见 §6 例外） | 设备手工检查 |

注：`GestureLevelOverlayPolicy.kt` 是三重命中（Cupertino 7 + Material 12 + Miuix 3），批次 5.2 优先处理。

## 6. 首页底栏临时例外清单

迁移期间允许保留当前成对图标引用，**仅限以下两个文件**，不扩散：

| 文件 | 命中 | 保留内容 | 退出条件 |
| --- | --- | --- | --- |
| `feature/home/components/BottomBar.kt` | Cupertino 3 + Miuix 3 | `BottomNavItem.selectedIcon/unselectedIcon` 成对图标与交叉淡入淡出 | 项目自有 outlined/filled 资产完成后切换为 `AppIcons.Navigation` |
| `feature/home/components/HomeNavigationIconPolicy.kt` | Miuix 14 + `vectorResource` | `resolveMiuixPreferredHomeNavigationIcon` 的 Regular/Medium 图标与本地矢量（动态/Story/直播/游戏/听视频） | 同上 |

其余 Miuix 直接 import 文件（HomeHeader、TopBar、GestureLevelOverlayPolicy）**不属于**例外，按计划第 6.4 节收敛。

## 7. 阶段 0 退出条件核对

| 退出条件 | 达成情况 |
| --- | --- |
| 清单可由命令重复生成 | 达成：`powershell -File scripts/theme_simplification_inventory.ps1 [-DumpLines]` |
| 每项有目标替代、归属批次和验证方式 | 达成：§4 分类 + §5 批次表 + §8 替代总表 |
| 首页底栏临时允许列表被单独记录 | 达成：§6 |

## 8. 类别 → 目标替代 / 批次 / 验证方式总表

| 类别 | 目标替代 | 归属阶段/批次 | 验证方式 |
| --- | --- | --- | --- |
| Cupertino 控件 | Miuix 组件（MIUIX 渲染器）/ Material3 组件（MATERIAL3 渲染器）；无等价时进中性 facade | 阶段 4（5 个批次） | 每批 `:app:compileDebugKotlin` + 目标测试 |
| Cupertino Icons | `AppIcons` 单一 Miuix 风格映射 | 阶段 5（5.1/5.2/5.3/5.4） | 结构测试 + compile |
| Material Icons | `AppIcons` 单一映射（MD3 与 MIUIX 共用） | 阶段 5（5.5） | 结构测试 + compile |
| Miuix Icons 直接 import | 收敛到 `AppIcons` 实现层（底栏例外除外） | 阶段 5（5.1） | 结构测试 |
| iOS 专属视觉 | 删除或中性重写 | 阶段 3 / 6 | design-system 策略测试 + 双主题回归 |
| iOS 语义色板 | 中性命名（如 Brand* / NeutralGray*） | 阶段 3 | `:app:compileDebugKotlin` |
| 主题枚举 | `UiStyle`/`AppThemeSelection` 收敛两值，默认 MIUIX | 阶段 1 | `UiPreset`/`Theme` 目标单测 |
| 旧主题键 | 新稳定键 + 一次性迁移，单事务删旧键 | 阶段 2 | DataStore 迁移表测试 |
| cupertino-adaptive 依赖 | 直接删除 | 独立变更 / 阶段 6 | 依赖报告零命中 |
| 测试/文档 | 双主题断言 + 双主题事实基线 | 阶段 1 / 7 | 全套单测 + lint |

## 9. 风险与建议

1. **阶段 4/5 是主要工作量**（120+ 文件），建议按 §5 批次逐 feature 提交，每批跑 `:app:compileDebugKotlin` 与结构测试，避免混批。
2. **默认值改 MIUIX 必须与迁移测试同步落地**，否则历史 iOS 用户首帧仍可能闪 iOS 主题。
3. `review-report.md` 缺失，后续实施以本报告为清单基线；如审计结论需要回溯，可在实施前补齐该文件。
4. 阶段 1 的迁移表测试可直接对照计划第 5.2 节表格编写，无需依赖本报告的清单细节。
