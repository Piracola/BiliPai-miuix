# 阶段 1：架构护栏（执行记录）

日期：2026-08-16
分支：`codex/bili-lite-phase0`（阶段 1 在同一分支推进）
提交：见 git log（`test(architecture)` 提交）

## 做了什么

### 1. 新增 4 个架构护栏测试（`app/src/test/java/com/android/purebilibili/architecture/`）

| 测试 | 拦截规则 | 机制 |
|---|---|---|
| `UiLayerMustNotTouchCoreDataStructureTest` | UI（feature）不得直接访问 Core 全局对象 | 单词边界正则扫描 feature 全部 731 文件；白名单外命中即失败 |
| `CoreLayerMustNotDependOnComposeStructureTest` | Core 业务层（network/store/player/database/cache/cooldown/coroutines/lifecycle/refresh）与 data 层不得 import `androidx.compose.*` | 零容忍；`core/ui`、`core/theme`、`core/util`、`core/plugin` 豁免（UI 基础设施） |
| `FeatureLayerIsolationStructureTest` | Feature 间不得直接访问对方内部实现 | 文件级棘轮：每个源 feature 的跨引用文件数冻结、只减不增 + SHA-256 快照（防"同数量换文件"） |
| `ArchitectureAllowlistRatchetTest` | 白名单只减不增 | 数量棘轮（上限 116）+ SHA-256 内容快照 |

设计遵循仓库既有模式（`FrameBudgetLintTest` / `StyleLintAllowlistRatchetTest`）：纯源码文本扫描、候选根回退、`scannerActuallyReadsSources` 自检防"路径失效全绿"。

### 2. 白名单（`ArchitectureAllowlist.kt`，116 个文件）

接入时实测 feature 文件引用 Core 全局对象 116 个（`SettingsManager` 为绝对大头；含 `by SettingsManager` 委托与全限定名两种引用形式）。全部按文件级豁免冻结，作为阶段 2 旧调用点例外清单的**文件级快照**；每迁移干净一个文件就调小上限、更新 SHA。

### 3. CI 接入（`.github/workflows/Build.yml`）

`quality-guards` job 的 `--tests` 白名单新增 4 个测试类全限定名。PR 默认路径直接跑，违规即失败；白名单增改会触发棘轮失败并暴露在 PR diff。

### 4. 禁止符号清单

NetworkModule、TokenManager、AccountSessionStore、DatabaseModule、AppDatabase、SettingsManager、PlayerSettingsStore、NetworkProxyStore + data/repository 20 个单例（VideoRepository 等）。

## 当前状态（阶段 1 退出条件核对）

| 退出条件 | 状态 |
|---|---|
| 旧代码可以有明确例外清单 | ✅ 116 文件白名单（文件级，带 SHA 快照） |
| 新代码不得继续扩大旧耦合 | ✅ 白名单外新引用即 CI 失败 |
| 所有后续迁移都能通过静态检查判断是否违反边界 | ✅ 四条规则可独立判定 |

## 已知限制

- 规则 A/C 是**文件级**判定（一个文件放行全部符号），比调用点级宽松。阶段 2 的接缝例外清单若需要调用点级精度，可在此基础上升级（当前足够防止新增扩散）。
- 规则 C 快照为接入时刻的全量 82 文件；后续阶段 4 迁移会让它逐次变化，届时按设计同步 SHA。
- `core/util`（4 文件）、`core/plugin`（5 文件）仍 import Compose（UI 工具/插件 API），列入豁免；`ModifierExt.kt`、`PermissionHelper.kt` 等属 UI 基础设施而非业务 Core。

## 验证

`./gradlew :app:testDebugUnitTest --tests 'com.android.purebilibili.architecture.*'` → 10 tests，BUILD SUCCESSFUL。
