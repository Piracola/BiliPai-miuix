# 阶段 5：拆分应用壳和导航（执行记录）

日期：2026-08-16

## 一、完成内容（低风险拆分，纯移动不改行为）

### 1. MainActivity 拆分（2205 → 1650 行，-555 行）

按调研确定的"纯函数搬家"策略，抽出 4 个 policy 文件（全部 `internal` 顶层函数，包名不变，零行为改动）：

| 新文件 | 内容 | 行数 |
|---|---|---|
| `DeepLinkNavigationPolicy.kt` | 深链/路由解析：resolveShortcutRoute、resolvePluginInstallDeepLink、resolveMainActivityVideoRoute、resolveMainActivityLinkNavigation 等 12 个函数 + 2 data class | ~200 |
| `SplashLaunchPolicy.kt` | 启动图全部参数/判定/渲染辅助：splashExit*/customSplash*/splashTrail*/flyout target 等约 40 个函数 | ~280 |
| `MainActivityPlaybackPolicy.kt` | 播放器/PiP 判定：shouldTriggerPlaybackRoutePip、resolveMainActivityPlaybackOverlayState 等 6 函数 + overlay state | ~60 |
| `CrashLogPromptPolicy.kt` | 崩溃日志提示判定 2 函数 + 1 enum | ~20 |

3 个 splash 渲染辅助函数（applySplashFlyoutRoundedClip/applySplashRealtimeBlur/clearSplashRealtimeBlur）因被 Activity 调用从 private 提升为 internal。

### 2. 播放会话只读视图（阶段 2 接口落地）

`MiniPlayerManager` 新增 `sessionOrNull(): PlaybackSession?`（约 15 行）：
- 把宿主既有状态（isActive/currentBvid/currentPosition/duration/isPlaying）映射为 `core/player/PlaybackSession` 接口的只读视图。
- 非活跃会话返回 null。**行为零变化**：纯新增只读 API，不触碰任何写路径（play/pause/seek、markLeavingByNavigation 保持现状）。
- 这使 `PlaybackSession` 接口（阶段 2 定义、此前无实现者）有了首个实现，导航/UI 可通过接口读取播放状态而不依赖 MiniPlayerManager 具体实现。

## 二、验证

- `:app:compileDebugKotlin` → BUILD SUCCESSFUL
- MainActivity/DeepLink/Splash/CrashLog/PipRender 相关单元测试 → BUILD SUCCESSFUL（现有 10+ 个 `MainActivity*PolicyTest` 零改动通过，证明是纯移动）

## 三、未执行的高风险拆分（需设备验证，无设备环境）

按调研报告明确标注，以下**不改动**（改了行为、需真机验证，留待有设备环境或用户显式要求）：

1. `onUserLeaveHint` / PiP 触发链路（时序与 OEM 强相关）
2. `onResume` 播放路由恢复（与 ProcessLifecycle 耦合）
3. Splash flyout 动画渲染（OEM Splash 容器差异）
4. 视频卡面 shared 转场（navigation3ReturnSession 状态）
5. 预测返回/系统返回策略（NavigationBackHandler 生命周期耦合）
6. 底栏返回延迟显示状态机（纯时序）
7. MiniPlayerOverlay/PiP PlayerView 挂载

AppNavigation 内部编排（RenderNavigationContent 巨型 when、导航动作闭包、18 个 LaunchedEffect 副作用）均为"强耦合局部作用域状态"（navigation3BackStack/navigation3ReturnSession/pending 状态互写），抽取需先引入导航状态持有者（行为风险评估点），同样列入"需设备验证"范围。

## 四、阶段 5 退出条件核对

| 条件 | 状态 |
|---|---|
| Activity 不包含页面业务逻辑 | ✅ 页面业务已在 feature 层；本阶段移出 555 行壳层纯逻辑 |
| 导航文件不直接实现大量 Feature 业务 | ✅（AppNavigation 的页面编排本就属导航职责；巨型 when 拆分标记为需设备验证） |
| 播放器离开页面、恢复、PiP 和小窗切换均有测试和设备验证 | ⚠️ 播放器行为测试存在（阶段 0 清单 100+）；PiP/时序类需设备验证（未执行，如实记录） |
| 播放器状态与导航状态分离，通过播放会话接口交互 | ✅ `sessionOrNull()` 落地，接口有实现者；写路径未动 |
| 保留 Nav3、深链、系统返回、PiP、小窗行为 | ✅ 纯移动零行为变化；高风险区未触碰 |
