# 重构阶段 0 交付：冻结基线与全量审计

日期：2026-08-16
适用分支：`codex/bili-lite-phase0`（基于 `codex/bili-lite` 切出）
回滚基线：标签 `baseline/bili-lite-pre-refactor`

本目录是 `docs/FULL_REFACTOR_PLAN.md` 阶段 0 的版本化交付物。所有清单均为只读审计产出，阶段 0 未移动任何代码。

## 文件索引

| 文件 | 内容 |
|---|---|
| `BASELINE.md` | Git 基线、构建基线、同步点记录 |
| `FEATURE_INVENTORY.md` | 功能/页面/路由清单、Feature 三级分级冻结、每功能 M3/Miuix 使用 |
| `SETTINGS_INVENTORY.md` | 设置项清单与 schema 冻结规则 |
| `PLAYER_INVENTORY.md` | 播放器行为清单、耦合点、回归风险排序 |
| `ARCHITECTURE_AUDIT.md` | 模块依赖图、split-package 风险、全局对象访问审计 |
| `LEFTOVER_AUDIT.md` | 已删功能残留、孤儿资源、测试覆盖地图、CI 现状 |
| `UPSTREAM_CHURN.md` | upstream/main churn 分布与同步承诺校准 |

## 阶段 0 退出条件核对

| 退出条件 | 状态 |
|---|---|
| 当前代码状态可回滚 | ✅ 标签 `baseline/bili-lite-pre-refactor` 指向 `codex/bili-lite` 的 `97e13acd8` |
| 所有主要页面和核心功能有清单，Feature 分级已冻结 | ✅ 见 `FEATURE_INVENTORY.md`，分级与计划书 §3.3 逐字一致 |
| 已知失败项和环境问题被区分为"产品问题"或"构建环境问题" | ✅ 编译基线通过，无已知产品失败项；见 `BASELINE.md` §3 |
| 没有开始大规模代码移动 | ✅ 阶段 0 仅产出清单文档，零代码改动 |

## 决策点 A 建议

计划书 §9 决策点 A：完成阶段 0 后，若功能清单、基线或构建状态无法确认，先修复基线，不进入重构。

建议：**可以进入阶段 1**。依据：

- 构建基线干净（`:app:compileDebugKotlin` BUILD SUCCESSFUL）。
- 功能/路由/设置/播放器四份清单完整，无悬空路由。
- 上游同步点已记录（`upstream/main = 379c968b9`，合并基点 `9f0e6b1eb`）。

但注意以下三项会在阶段 1/2 立即影响工作，建议先在阶段 1 的架构护栏中定义：

1. **UI → 全局对象硬违规约 370 处**（主要是 SettingsManager 约 300+ 处）：阶段 1 的"违规 import 检查"必须先把这批既有调用点列入版本化例外清单，否则 PR 默认路径直接全红。见 `ARCHITECTURE_AUDIT.md` §4 的分批豁免建议。
2. **churn 校准结论与计划 §6 有出入**：上游近 4 个月（2026-05~08）月均约 580 提交、占全窗口 84.6%，`navigation/AppNavigation.kt` 是全仓第二热文件，`SettingsManager.kt` 是第六热文件。Tier 1/2/3 划分需按 `UPSTREAM_CHURN.md` §5 修订（Core 同步窗口 2-4 周、路由列入受管理而非可同步、SettingsManager 从稳定 Core 剥离）。
3. **settings schema 冻结需立即生效**：`SETTINGS_INVENTORY.md` §4 列出 8 条冻结规则（key 只增不改、锁定字符串编码格式、legacy 双写影子缓存同步等），阶段 2 的接缝测试按此验收。

## 计划书 §3.2 模块归宿的阶段 0 结论

- `settings-core`（1 文件）、`network-core`（3 文件）边界薄弱，维持现状；**不**在阶段 0 合并。
- `app` 内 `data/repository` 20 个 object 单例、`core/store/SettingsManager.kt`（6067 行）等全局对象群，阶段 6 前只保持包级边界和架构测试。
- **新增风险**：三个库模块的源码包名与 app `core` 包重叠（split-package），Gradle 层隔离但包名层未隔离，阶段 6 最先处理。见 `ARCHITECTURE_AUDIT.md` §3。
