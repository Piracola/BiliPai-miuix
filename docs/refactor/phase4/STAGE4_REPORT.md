# 阶段 4：垂直切片迁移页面（执行记录）

日期：2026-08-16

## 一、完成切片

### 切片 1：`feature/web`（Tier C 叶子页，纯 UI 替换）
- `WebViewScreen.kt`：`material3.*` 通配导入 → 精确 `TopAppBarDefaults`；`MaterialTheme.colorScheme.surface/onSurface` → `AppSurfaceTokens.surface()/onSurface()`。功能零改动（URL 拦截/跳转逻辑保留）。
- 既有 `WebViewNavigationPolicyTest` 5 例全绿（功能对照基线）。

### 切片 2：`feature/partition`（Tier B 示范，完整 Coordinator 切片）
按计划 §4 固定流程执行：
1. **记录原功能**：分区侧栏 + 视频列表 + 下拉刷新 + 滚动加载 + bangumi 特殊分区跳转。
2. **定义 UiState/事件**：`PartitionFeedUiState`（selectedPartition/videos/isLoading/isRefreshing/error）。
3. **数据与副作用移入 Coordinator**：`PartitionFeedViewModel.kt`（新建）持有设置读取（`topTabLabelMode`、`homeFeedCardStyle` 以 StateFlow 暴露）与 `VideoRepository` 数据获取；UI 不再直接访问 SettingsManager/Repository。
4. **Lite Token 替换**：`MaterialTheme.colorScheme.primary/onSurfaceVariant/onSurface` → `AppSurfaceTokens.primary()/onSurfaceVariantSummary()/onSurface()`。
5. **迁移纯逻辑测试**：`PartitionScreenStructureTest` 14 例同步（断言改指向 VM 文件，Screen 断言改为 assertFalse 防回退）。
6. **验证**：分区 14 例 + 架构护栏全绿。

## 二、护栏规则完善（与切片联动）

- 规则 A 增加 **`*ViewModel.kt` 豁免**：ViewModel 属 Coordinator 层（依赖 Core 合法，符合计划 §3.1 分层）。这使 20 个 ViewModel 文件从白名单移除。
- 白名单 **116 → 95**（partition UI 文件 + 20 个 ViewModel 文件）；棘轮上限同步调小、SHA 快照更新。护栏的"文件级豁免"语义在 KDoc 中补充说明。

## 三、功能对照清单（阶段 4 要求）

| 页面 | 原功能 | 迁移后 | 对照 |
|---|---|---|---|
| WebViewScreen | URL 拦截/深链跳转/内嵌浏览器 | 同（仅 M3 类型导入与 Token 替换） | ✅ WebViewNavigationPolicyTest 5 例 |
| PartitionScreen | 分区列表/侧栏/刷新/加载更多/bangumi 跳转 | 同（设置读取与数据获取移入 VM） | ✅ PartitionScreenStructureTest 14 例 |

## 四、阶段 4 退出条件（示范切片口径）

| 条件 | 状态 |
|---|---|
| 页面功能不减少 | ✅ 行为对照测试全绿 |
| 页面不直接访问 Core 全局对象 | ✅ partition/web 均不在白名单、护栏零违规 |
| UI 风格和 Token 使用统一 | ✅ M3 颜色全部替换为 AppSurfaceTokens |
| 该页面上游同步冲突面减少 | ✅ UI 层只依赖 Token/组件，无业务耦合 |

## 五、范围说明（诚实记录）

- 本阶段完成 **2 个代表性切片**（1 个 Tier C 示范 + 1 个 Tier B 完整流程），确立切片迁移的模式与护栏联动方式。
- **未完成**：其余 90+ 个页面（settings 全组、video、home 等 Tier A 大页面）的逐个迁移。全量切片需要多轮迭代（每个大页面数千行、涉及播放器/导航耦合），按计划 §4 的逐切片节奏继续。
- 阶段 4 决策点 C（设置页/首页/列表页迁移后仍频繁触碰业务层 → 修正 Contract）在示范切片中未触发：partition 切片后 UI 零业务耦合。

## 六、验证

- `:app:testDebugUnitTest`（partition.* + architecture.*）→ BUILD SUCCESSFUL
- `:app:compileDebugKotlin` → BUILD SUCCESSFUL
- 架构护栏全绿：白名单 95、棘轮 SHA 快照一致。
