# AI Source Map / AI 事实导航

最后核对：2026-07-31。本文只提供仓库路径与事实优先级，不替代源码检查。

## 推荐入口

| 需求 | 文件 |
| --- | --- |
| 项目总览 | [`../../README.md`](../../README.md) / [`../../README_EN.md`](../../README_EN.md) |
| 最新完整发布记录 | [`../../CHANGELOG.md`](../../CHANGELOG.md) |
| 当前开发优先级 | [`ROADMAP.md`](ROADMAP.md) |
| 架构 | [`ARCHITECTURE.md`](ARCHITECTURE.md) |
| 功能状态 | [`FEATURE_MATRIX.md`](FEATURE_MATRIX.md) |
| QA 与回归 | [`QA.md`](QA.md) |
| 发布流程 | [`RELEASE_WORKFLOW.md`](RELEASE_WORKFLOW.md) |
| JSON 与外部插件 | [`../PLUGIN_DEVELOPMENT.md`](../PLUGIN_DEVELOPMENT.md) |
| 源码级原生插件 | [`../NATIVE_PLUGIN_DEVELOPMENT.md`](../NATIVE_PLUGIN_DEVELOPMENT.md) |
| 插件 SDK | [`../../plugins/sdk/README.md`](../../plugins/sdk/README.md) |
| 结构约束 | [`../../STRUCTURE_GUIDELINES.adoc`](../../STRUCTURE_GUIDELINES.adoc) |

## 仓库结构

| 路径 | 职责 |
| --- | --- |
| `app/` | Android 主应用、功能界面、播放器、Navigation3 编排和测试 |
| `design-system/` | iOS、Material 3、Miuix 共用的主题、组件与视觉策略 |
| `settings-core/` | 可复用设置与偏好逻辑 |
| `network-core/` | 网络策略与底层网络支持 |
| `plugin-sdk/` | 外部插件可依赖的稳定接口 |
| `baselineprofile/` | 启动与帧性能基准配置 |
| `plugins/` | 插件示例、SDK 文档、工具与社区目录 |
| `scripts/` | 发布、性能和辅助脚本 |

主源码位于 `app/src/main/java/com/android/purebilibili/`，其中：

- `app/`：应用入口与启动装配。
- `core/`：跨业务公共能力。
- `data/`：数据模型与仓库实现。
- `domain/`：稳定业务规则与 UseCase。
- `feature/`：按业务场景组织的界面与交互。
- `navigation/`：路由兼容、入口策略与顶层导航装配；`navigation3/`：当前 NavKey、返回栈、Entry/Scene、预测返回与整卡会话实现。

## 事实优先级

出现冲突时按以下顺序判断：

1. 当前源码和构建配置。
2. `CHANGELOG.md` 与 GitHub Releases。
3. `docs/wiki/ROADMAP.md`（仅用于优先级和状态，不代表已发布）。
4. Wiki 与插件开发文档。
5. `README.md` / `README_EN.md`。

当前 `app/build.gradle.kts` 声明开发构建 `9.9.9.1 / versionCode 266`；`CHANGELOG.md` 最新完整发布记录为 `v9.9.8.9`。开发版本号领先发布记录时，不代表该版本已经公开发布。

当前构建基线为 AGP 9.3.1、Gradle 9.5、Kotlin 2.4、compileSdk 37；Navigation3 runtime/UI 使用官方同版 `1.2.0-alpha07`，Miuix `0.9.3` 继续用于主题与视觉组件，不再提供 NavDisplay 实现。

`AI.txt` 与 `llm.txt` 是兼容入口，主入口为 [`../../llms.txt`](../../llms.txt)。
