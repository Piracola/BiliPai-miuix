# 阶段 0：播放器行为清单

日期：2026-08-16。本清单供阶段 4（视频详情/播放器切片）、阶段 5（应用壳/导航）与"播放器最后迁移"策略使用。

## 1. 播放器代码地图

核心引擎层：`core/player/BasePlayerViewModel.kt`(342，VideoPlayback 与 BangumiPlayer 共用)、`HiResCompatibleRenderersFactory.kt`(115)、`PlaybackMediaCache.kt`(246)、`PlayerVolumeController.kt`(12)、`core/store/player/PlayerSettingsStore.kt`。

主视频播放（feature/video）：
- `viewmodel/VideoPlaybackViewModel.kt` — **8414 行**，UI 状态/播放器 attach/CDN/清晰度/弹幕发送/评论/投币三连/笔记/收藏全会话逻辑
- `usecase/VideoPlaybackUseCase.kt`(1423)、`state/VideoPlayerState.kt`(1553，ExoPlayer 构建+生命周期决策+dispose)、`player/MiniPlayerManager.kt`(2746，**全局单例**：小窗/PiP/后台/MediaSession/前台通知/导航离开标记)、`player/PlaybackService.kt`(152)、`player/PlaylistManager.kt`(721)、`playback/session/*`（SeekController 283、UserActionTracker 145、SessionStore 138）、`playback/coordinator/PlaybackCoordinator.kt`(106)、`playback/loader+resolver+policy`(~2000)、`VideoActivity.kt`(302，独立播放 Activity/PiP 入口)

播放器 UI 覆盖层：`ui/section/VideoPlayerSection.kt`(5100)、`ui/overlay/VideoPlayerOverlay.kt`(3335)、`ui/pager/PortraitVideoPager.kt`(3773)、`ui/overlay/FullscreenPlayerOverlay.kt`(1449)、`ui/overlay/MiniPlayerOverlay.kt`(919)、`ui/overlay/BottomControlBar.kt`(1621)、`ui/overlay/PortraitFullscreenOverlay.kt`(719)、`ui/gesture/PlayerGestureHandler.kt`(325)、overlay 其余 40+ 文件。弹幕：`danmaku/DanmakuManager.kt`(2266) + 19 个 policy/parser。

其他播放器：`feature/bangumi/BangumiPlayerViewModel.kt`(1098)+Screen、`feature/live/LivePlayerScreen.kt`(2358)+LivePlayerPipPolicy、`feature/download/OfflineVideoPlayerScreen.kt`(1160)、`feature/plugin/js/ExternalMediaPlayerScreen.kt`。

## 2. 行为清单（存在性 + 位置）

| 行为 | 存在 | 实现位置 |
|---|---|---|
| 播放/暂停/停止 | ✅ | `VideoPlaybackUseCase.kt:223-251`、`VideoPlayerState.kt:1051`、`PlaybackCoordinator.kt:23-71`（STOP/REPEAT/PLAY_NEXT/AUTO_CONTINUE） |
| seek/进度 | ✅ | `VideoPlaybackViewModel.kt:7705`、`PlaybackSeekController.kt` |
| 倍速 | ✅ | `VideoPlaybackViewModel.kt:1329-1453`、`SpeedSelectionMenuDialog`、`TwoFingerSpeedFeedbackOverlay` |
| 亮度/音量/进度手势 | ✅ | `ui/gesture/PlayerGestureHandler.kt:72-160`、全屏双击 `FullscreenPlayerOverlay.kt:570-615` |
| 弹幕显示/发送/面板 | ✅ | `DanmakuManager.kt`（attach/load）、`VideoPlayerSection.kt:1235,2740,3555`、`VideoPlaybackViewModel.kt:4257`、`DanmakuSettingsPanel.kt`(1843) |
| 清晰度/音质/设置面板 | ✅ | `QualityMenu.kt`、`AudioQualitySelectionMenu.kt`、`VideoSettingsPanel.kt`、`VideoPlayerOverlay.kt:1930-2071` |
| 横竖屏/全屏 | ✅ | `VideoActivity.kt:218-224`、`VideoDetailScreenStateHolder.kt:1997-2155`、`VideoPlayerState.kt:896`（竖屏全屏） |
| 后台播放 | ✅ | `PlaybackService.kt:43-152`、`MiniPlayerManager.kt:820-941`（拆视频轨重优化）、`BackgroundManager.kt` |
| 音频焦点 | ✅ | `VideoPlayerState.kt:1058-1061`、`MiniPlayerManager.kt:134-135,1701-1703` |
| PiP 进出 | ✅ | `VideoActivity.kt:227-286`、`VideoDetailPlatformEffectsHost.kt:80-115`、`MainActivity.kt:1311-1322`、`LivePlayerScreen.kt:390-401` |
| 小窗/迷你播放器 | ✅ | `MiniPlayerManager.kt:1870-1926`、`MiniPlayerOverlay.kt`、`MainActivity.kt:1394-1395` |
| 播放器-导航集成（离开/返回） | ✅ | `markLeavingByNavigation`（VideoDetailScreenStateHolder 多处）、`AppNavigation.kt:826-831`（haltForeignPlaybackForIncomingVideo）、`VideoPlayerState.kt:1169-1286`（生命周期恢复决策） |
| 覆盖层显隐 | ✅ | `VideoPlayerSection.kt:958`、全屏 4000ms 自动隐藏、小窗 3000ms、锁屏强制隐藏 |
| 连播/播放列表/下一集 | ✅ | `PlaybackCoordinator`+`PlaylistManager`+`NextPlaybackResolver` |

## 3. 播放器-导航-生命周期耦合点（阶段 5 重点）

1. `state/VideoPlayerState.kt` — ExoPlayer 创建/释放、LifecycleObserver 暂停恢复决策、与 MiniPlayerManager 的 player 所有权交接、MainActivity PendingIntent。
2. `player/MiniPlayerManager.kt` — 全局单例，跨 Activity/导航持有 ExoPlayer+MediaSession+通知；`isLeavingByNavigation/isNavigatingToVideo/isMiniMode/isSystemPipActive` 被 4 处读写。
3. `VideoActivity.kt` — Activity 级 PiP 与 Compose `isInPipMode` 同步。
4. `MainActivity.kt` — 小窗/PiP 渲染挂载 + 导航路由内播放器路由。
5. `navigation/AppNavigation.kt` — 播放器路由解析、进入前 haltForeignPlayback、离开时 markLeavingByNavigation。
6. `screen/VideoDetailScreenStateHolder.kt`(4527) — 返回处理、全屏方向、PiP 参数、连续播放器转场。
7. `ui/section/VideoPlayerSection.kt` — PlayerView 生命周期/surface rebind/前台恢复。
8. `core/lifecycle/BackgroundManager.kt` — 前后台事件转发。

## 4. 现有播放器测试（供"保留行为测试"参考）

- 单测（app/src/test）：policy 类齐全——`PlayerLifecyclePlaybackPolicyTest`、`VideoPlayerStateReusePolicyTest`、`VideoPlayerBufferPolicyTest`、`PlayerErrorRecoveryPolicyTest`、`VideoPlaybackUserSeekPolicyTest`、`PlaybackCompletionPolicyTest`、`PlaybackCdnFallbackPolicyTest`、`PlaybackStallRecoveryPolicyTest`、`MiniPlayerManagerLeakCleanupTest`、`BackgroundPlaybackPolicyTest`、`MediaQueueSyncPolicyTest`、弹幕 15 个、字幕 3 个、`BangumiPlayerOverlayPolicyTest`、`LivePlayerPipPolicyTest` 等。
- androidTest：`PlayerControlsUiRegressionTest`、`PortraitVideoPagerUiRegressionTest`、`DanmakuComposerUiRegressionTest` 等 5 个。
- **无覆盖**：`VideoPlaybackViewModel` 本体、`MiniPlayerManager` 播放控制主路径（需真 ExoPlayer）、`PlayerGestureHandler` 无直接单测。

## 5. 回归风险排序（阶段 4/5 最易回归行为）

1. **播放器所有权交接**：小窗/PiP/详情页三方争夺同一 ExoPlayer（`rememberVideoPlayerState` 复用 vs `onDispose` 判断释放，`VideoPlayerState.kt:1128-1153`）。风险：无声/双播/泄漏。
2. **生命周期暂停/恢复决策矩阵**（`VideoPlayerState.kt:1169-1286`，五元组：isMiniMode/isPip/isBackgroundAudio/isLeavingByNavigation/hasRecentUserLeaveHint），历史上修过多轮时序 bug。
3. **后台音频视频轨拆装**（`MiniPlayerManager.kt:820-941` 与内存分档联动）。
4. **导航离开/返回声音状态机**：markLeavingByNavigation 的 forceStop/deferPlaybackStop + onDispose 延迟释放 + haltForeignPlaybackForIncomingVideo 三处交织——"返回时无声/返回后仍响"。
5. **弹幕与播放进度同步**（attachPlayer 硬/软同步，seek/倍速/媒体切换三处改位置）。
6. **自动连播/播放结束动作**（跨多文件状态：PlaylistManager 外部列表 + completion 设置 + hasUserStartedPlayback）。
7. **覆盖层自动隐藏计时**（内嵌 200ms 轮询/全屏 4000ms/小窗 3000ms，与播放状态联动）。
8. **大文件改动风险**：上表 4 个超 4000 行文件内部状态同步（如 `isPortraitPlaybackSessionActive` 影响 STATE_ENDED 路径，`VideoPlaybackViewModel.kt:2161`）。

建议优先加固的测试点：(a) 播放器所有权交接（已有 `shouldReuseMiniPlayerAtEntry`/`shouldTreatPlayerAsOwnedByMiniPlayer` 纯函数，缺交接集成测试）；(b) 生命周期决策矩阵扩展场景；(c) 播放结束连播决策；(d) 手势补充单测；(e) 弹幕 seek 同步扩展。
