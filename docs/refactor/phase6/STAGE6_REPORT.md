# 阶段 6：Gradle 模块化（执行记录）

日期：2026-08-16

## 一、结论

**物理 Gradle 模块化推迟，按计划 §3.2 的降级路径执行「先包级边界，后 Gradle 模块化」**。

阶段 6 完成的不是"创建 4 个新 Gradle 模块并移动文件"（那是需要设备验证 + 数天工作量的独立任务，且当前 split-package 与反向依赖会先导致大面积编译失败），而是：
1. **实测就绪度评估**（候选模块能否抽取的硬数据）
2. **固化目标模块包边界**（架构测试，防边界退化）
3. **冻结反向依赖清单**（抽模块前必须先消除，只减不增）
4. **产出模块化前置条件清单**（文档）

## 二、就绪度实测（2026-08-16）

| 候选模块 | 源包 | 文件数 | feature 反向依赖 | 就绪度 |
|---|---|---|---|---|
| `:core-model` | `data/model` | 43 | **0** | ✅ 可直接抽取（仅 kotlinx-serialization） |
| `:core-player` | `core/player` | 5 | **0** | ✅ 可直接抽取（media3） |
| `:core-data` | `data/repository` + `core/store` + `core/network` + `core/database` | 60+ | repository 3 / store 2 / network·database 0 | ⚠️ 先消除 5 个反向依赖 |
| `:feature-contracts` | 尚未建立（UiState/Event/Effect 契约分散在各 feature） | — | — | ⚠️ 需先按阶段 4 切片逐步建立契约 |

反向依赖明细（冻结于 `ModuleBoundaryReadinessStructureTest`）：
- `data/repository`：DanmakuRepository、ArticleRepository、VideoRepository（import feature 层）
- `core/store`：SettingsManager、SettingsReader（import feature.settings 枚举）

## 三、新增架构测试

`ModuleBoundaryReadinessStructureTest`（app/src/test/.../architecture/，已接入 CI guard）：
- `coreModelCandidate_hasNoFeatureDependency` / `corePlayerCandidate_hasNoFeatureDependency` / `coreNetworkDatabaseLifecycle_haveNoFeatureDependency`：零容忍，一旦出现 feature 反向依赖立即失败。
- `repositoryFeatureDependencies_doNotGrow` / `storeFeatureDependencies_doNotGrow`：冻结快照（文件级 SHA 语义用集合相等），只减不增。
- `scannerActuallyReadsSources`：自检。

## 四、模块化前置条件清单（后续物理拆分前必须完成）

1. **split-package 处理（最高优先）**：`design-system`（namespace=designsystem 但源码在 `core.ui`/`core.theme`）、`settings-core`（`core.store`）、`network-core`（`core.network.policy`）与 app 同包名跨模块共存。物理拆分前必须统一包名（如 `core.ui` → 各模块独立根包）或合并回 app。
2. **消除 5 个反向依赖**：DanmakuRepository/ArticleRepository/VideoRepository 的 feature 引用、SettingsManager/SettingsReader 的 feature.settings 枚举引用（把枚举下沉到 core 或改为 core 包内定义）。
3. **design-system 的 M3 facade 收敛**（阶段 3 保留项）：AppPrimitiveComponents 等 243 个 feature 文件的消费点，拆模块前需明确 design-system 的依赖面。
4. **feature-contracts 建立**：按阶段 4 切片模式为 Tier A 页面逐一定义 UiState/Event/Effect 契约。
5. **设备验证**：模块化会改变编译产物与类加载路径，需真机冒烟（启动/播放/PiP）。
6. `settings-core`（1 文件）、`network-core`（3 文件）边界薄弱：维持现状，不参与本轮拆分（计划 §3.2 明确）。

## 五、验证

- `ModuleBoundaryReadinessStructureTest` → BUILD SUCCESSFUL（6 tests）
- CI guard 白名单新增该测试类
