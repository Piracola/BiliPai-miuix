# 阶段 0 基线记录

日期：2026-08-16

## 1. Git 基线

| 项 | 值 |
|---|---|
| 执行前本地分支 | `codex/bili-lite` @ `97e13acd8`（`refactor(ui): 底栏同字形、顶部栏悬空…`） |
| 回滚标签 | `baseline/bili-lite-pre-refactor` → `97e13acd8`（工作树干净时打标） |
| 重构分支 | `codex/bili-lite-phase0`（自 `codex/bili-lite` 切出，已推送 origin） |
| `upstream/main`（fetch 后） | `379c968b9503d861d7290d28a6bf4a20c56b37da`（`0.2.3-beta.6` 之后） |
| 合并基点（本地 Lite 分支与 upstream/main） | `9f0e6b1eb`（= fetch 前的 upstream/main，`0.2.3-beta.4`） |
| Lite 独有提交数 | 19（`git rev-list --count HEAD ^upstream/main`） |
| 上游新提交数（基点之后） | 37（`git rev-list --count upstream/main ^HEAD`） |

fetch 增量：`9f0e6b1eb..379c968b9` 为 `main` 更新；另新增分支 `upstream/codex/danmaku-engine-v2`、标签 `0.2.3-beta.5/6`。

> 注意：`origin/codex/bili-lite` 与本地 `codex/bili-lite` 已分叉（ahead 595 / behind 141）。按计划 §6.1，该远端分支不作为权威基线，建议后续重命名归档为 `archive/bili-lite-pre-refactor`（涉及远端写操作，需用户确认后执行）。

## 2. 构建基线

命令（复用缓存 Gradle 9.5.0，`GRADLE_OPTS` 加 SSL 放行）：

```
:app:compileDebugKotlin  →  BUILD SUCCESSFUL in 1m 8s（50 actionable tasks, 50 up-to-date）
```

- 未执行打包/安装/全量测试（遵守 AGENTS.md 验证阶梯）。
- 单元测试基线未在本阶段运行；CI guard job 的 10 个白名单测试是仓库事实上的"通过基线"，见 `LEFTOVER_AUDIT.md` §5。

## 3. 已知失败项与环境问题区分

| 现象 | 分类 | 备注 |
|---|---|---|
| 无（编译通过，无已知产品失败项） | — | — |
| CI guard job 含 `AppUpdateCheckerTest`（纯网络更新检查） | CI 设计问题（非产品回归） | 属"守卫清单职责存疑"，见 `LEFTOVER_AUDIT.md` §5 |
| 本机 Gradle wrapper 曾存在下载失败史（AGENTS.md 记载） | 构建环境问题 | 本次直接用缓存 `~/.gradle/wrapper/dists/gradle-9.5.0-bin/…/gradle` 绕过 |

## 4. 代码规模基线（快照，供阶段 6 对比）

- `app/src/main/java`：1001 个 .kt 文件，316478 行（feature 731 文件/251003 行，core 148/32743，data 82/22918，navigation+navigation3 31/6688，app 6/696，domain 2/225，MainActivity 1/2205）。
- 全 app 最大 10 文件：`VideoPlaybackViewModel.kt` 8414、`SettingsManager.kt` 6067、`VideoPlayerSection.kt` 5100、`ProfileScreen.kt` 4611、`VideoDetailScreenStateHolder.kt` 4527、`SpaceScreen.kt` 4404、`BottomBar.kt` 3945、`PortraitVideoPager.kt` 3773、`SearchScreen.kt` 3725、`VideoPlayerOverlay.kt` 3335。另有 `AppNavigation.kt` 2992、`ApiClient.kt` 2928、`MiniPlayerManager.kt` 2746、`MainActivity.kt` 2205。
- 测试：单元测试 939 个类（app 872 + design-system 50 + settings-core 1 + network-core 1），androidTest 15 个（app）。
