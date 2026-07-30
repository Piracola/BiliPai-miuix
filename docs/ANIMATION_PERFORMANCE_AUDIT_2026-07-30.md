# 动画与转场性能全面审查

日期：2026-07-30  
目标设备：Redmi K60，骁龙 8+ Gen 1  
审查方式：源码静态审查、现有基准记录复核、调用链分析  
当前状态：审查报告，尚未修改业务源码

## 1. 结论摘要

目前的主要问题不是 Redmi K60 性能不足，也不是某一个模糊半径或动画时长单独过大，而是同一帧内叠加了过多不同阶段的工作：

```text
Compose 状态更新/重组
    + 测量与布局
    + 大尺寸图层录制或共享图层合成
    + 全屏模糊/背景采样
    + TextureView 视频纹理合成
    + Surface/显示模式同步
    = 超过单帧预算，出现掉帧
```

K60 在 120Hz 下每帧只有约 `8.33ms`，60Hz 下约 `16.67ms`。项目进入视频详情页时会主动请求不低于 90Hz 的最高刷新率，反而在最复杂的转场期间缩短了每帧预算。持续使用 TextureView、实时模糊、视频解码和高刷还会提高 GPU 带宽与平均功耗，设备升温后发生热降频，于是形成“多次往返后越来越卡、越来越热”的现象。

当前最应该优先处理的不是继续微调 easing 或把所有模糊直接关闭，而是：

1. 转场开始前确定统一性能档，转场过程中不再切刷新率、Surface 或重型特效。
2. 默认改为“封面参与共享变形，正常播放使用 SurfaceView”，不让共享转场开关把整个播放生命周期锁定为 TextureView。
3. 返回时保留播放会话和位置，但由封面或冻结帧接管动画；性能档下暂停解码并解绑视频输出。
4. 普通预测返回立即提交页面返回，让模糊清理与位移并行，取消当前最长约 750ms 的串行链。
5. 所有 Haze、Kyant、Miuix 和原生 RenderEffect 路径统一消费同一个视觉预算，并在转场前根据温度、省电模式和近期帧耗时主动降级。

## 2. 证据等级

- **已确认**：源码调用链能够直接证明行为存在。
- **高概率**：源码能证明高成本路径存在，但具体 CPU/GPU 占比需要 K60 Perfetto 数据确认。
- **条件性**：只有用户打开特定功能或进入特定页面时发生。
- **不能仅靠源码确认**：恢复页布局峰值、厂商 Surface 调度和热降频时间点，必须真机测量。

## 3. P0：应第一批处理

### P0-1 视频详情无开关请求最高刷新率

**证据：已确认**

- `app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailPlatformEffectsHost.kt:26-69`
- `app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailPlatformPolicy.kt:860-885`
- `app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt:1282-1285`

详情页激活后会从当前分辨率的显示模式中，选择刷新率不低于 `90Hz` 的最高模式；退出详情时又恢复原模式。它没有结合视频帧率、转场阶段、温度、省电模式或用户性能偏好。

**影响：**

- 120Hz 将帧预算压缩到 8.33ms，而视频大多只有 24/30/60fps，并不能从持续 120Hz 合成中获得对等收益。
- 进入和退出时的显示模式切换与共享转场同时发生，可能额外引入 SurfaceFlinger 和显示同步工作。
- 高刷持续提高合成频率和功耗，是发热与后续热降频的重要放大器。

**建议：** 转场期间保持进入前刷新率；页面稳定后再按视频帧率选择 60/90/120Hz。省电、温度升高或性能档降级时固定 60Hz；不要在返回动画开始时切显示模式。

### P0-2 共享转场使播放器整个生命周期使用 TextureView

**证据：已确认**

- `VideoDetailScreenStateHolder.kt:2454,2669,3182` 均传入 `useTextureSurfaceForNavigation = transitionEnabled`
- `VideoPlayerSectionPolicy.kt:898-908` 只要导航变换开启就选择 TextureView
- `VideoPlayerSection.kt:2783-2796` 据此加载 TextureView 布局
- `app/src/main/res/layout/view_player_texture.xml:2-6` 明确配置 `surface_type="texture_view"`

共享转场默认开启，因此原本只持续 280-480ms 的动画需求，变成了整个视频播放期间的持续 GPU 纹理合成成本。

**影响：** TextureView 会把每个视频帧放进应用 GPU 合成树，方便缩放、裁剪和共享变形，但比 SurfaceView 的独立硬件合成路径更耗 GPU 带宽。它能够同时解释“转场掉帧”和“播放一段时间后发热”。

**建议：** 默认使用 SurfaceView 播放，卡片封面或驻留冻结帧参与 shared morph（共享形变）。只有视频翻转等确实需要纹理变换的功能才临时使用 TextureView；共享转场开关不应决定整个播放器生命周期的输出类型。

### P0-3 返回动画故意保留实时播放器和解码

**证据：已确认**

- `app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt:1412-1422`
- `app/src/main/java/com/android/purebilibili/feature/video/player/MiniPlayerManager.kt:1545-1573`
- `VideoDetailTransitionPolicy.kt:266-281`
- `VideoDetailScreenStateHolder.kt:1432-1448`

当卡片转场开启且存在来源路由时，返回会设置 `deferPlaybackStop=true`，直到共享壳落位后才停播。这个设计避免了“黑壳突然卸掉”，视觉动机合理，但让返回同一批帧同时承担：视频解码、TextureView 输出、弹幕/Overlay、sharedBounds 缩放、首页恢复与底栏恢复。

**建议：** 保留播放器会话、进度和缓冲状态，不等于必须保留实时视频输出。BALANCED/CRITICAL 档返回时由封面或冻结首帧接管 morph，并暂停或解绑视频输出；只有 FULL 档允许 live-surface return，且它应是明确选择而不是默认。

### P0-4 普通预测返回存在最长约 750ms 的串行链

**证据：已确认**

- `app/src/main/java/com/android/purebilibili/core/ui/transition/PredictiveBackBackgroundPolicy.kt:103-118,127-137,166-200`
- `app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt:424-454`
- `app/src/main/java/com/android/purebilibili/navigation3/predictiveback/BiliPaiDefaultPredictiveBackAnimation.kt:31-40`

普通预测返回会给目标页做约 28/22px 的全屏动态模糊，提交返回前等待最长约 200ms 的清模糊任务 `join()`，随后再执行固定 550ms、`LinearEasing` 的全页滑出动画。

**影响：** 用户松手后页面仍要串行等待，GPU 模糊和双页面绘制持续时间过长；线性 550ms 既显慢，也扩大热负载窗口。

**建议：** 手势提交后立即调用返回；模糊清理、当前页退出和目标页稳定化并行；总收尾控制在约 220-280ms。RenderEffect 按量化半径缓存，避免在 `graphicsLayer` 中反复创建效果对象。

说明：视频卡共享返回明确排除了这套普通预测返回模糊，两者不能合并成“视频返回叠了两层模糊”。

### P0-5 视觉降级守卫触发太晚，而且关键路径绕过预算

**证据：已确认**

- `app/src/main/java/com/android/purebilibili/core/ui/performance/RuntimeVisualGuardTracker.kt:14-21,159-200`
- `design-system/src/main/java/com/android/purebilibili/core/ui/adaptive/RuntimeVisualGuardPolicy.kt:3-6,35-60`
- `design-system/src/main/java/com/android/purebilibili/core/ui/adaptive/RuntimeVisualGuardSignalPolicy.kt:10-19`
- `app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt:832,1813,1938`
- `app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt:3148-3152,3180-3183`

当前守卫至少收集 12 帧，并要求连续两个窗口掉帧率达到 7.5% 才降级，降级后保持 60 秒。跟踪信号只覆盖首页和视频详情的少数交互。首页、视频卡和底栏多处又硬编码关闭平滑优先或把 `forceLowBlurBudget` 固定为 false；直接调用 Kyant/Miuix backdrop 的路径也不会自动消费这份预算。

全仓暂未发现基于 `PowerManager` thermal status、温度或省电模式的转场前置策略。

**建议：** 在动画开始前一次性选择 `FULL / BALANCED / CRITICAL` 档位，并锁定到动画结束，避免中途切树：

- FULL：冷机、非省电、近期帧时间健康，可允许有限实时模糊或 live 视频形变。
- BALANCED：默认档，封面 morph、SurfaceView、降低或冻结动态模糊、暂停非必要 Overlay。
- CRITICAL：温度高、省电、连续掉帧或 GPU 超预算，纯位移/透明度、无动态模糊、60Hz。

守卫应是事故后的兜底，不能代替转场前的主动预算选择。

## 4. P1：高收益的第二批问题

### P1-1 高级弹幕固定约 60fps 驱动 Compose，并每帧扫描全表

**证据：已确认**

- `app/src/main/java/com/android/purebilibili/feature/video/ui/overlay/AdvancedDanmakuOverlay.kt:45-67` 每 16ms 更新播放位置
- 同文件 `:78-98` 每次对全部高级弹幕 `filter` 并组合活动项
- 同文件 `:103-184` 每项重新计算文字、位置、缩放和多个 Modifier
- `VideoPlayerSection.kt:3493-3506` 在普通弹幕可见时挂载高级层

返回 live morph 期间没有统一的 transition/performance-tier 门控，因此它可能与视频纹理和共享转场共同运行。

**建议：** 优先改成单一 Canvas draw pass 或专用渲染线程；至少建立按开始/结束时间索引，不再每帧全表扫描。更新跟随视频帧或 Choreographer，并在所有转场期间冻结/隐藏弹幕、字幕、互动卡和控制层。

### P1-2 标准弹幕与命令弹幕也在实时返回期间保留

**证据：已确认**

- 标准 ByteDance `DanmakuView`：`VideoPlayerSection.kt:3458-3488`
- 命令弹幕：`CommandDanmakuOverlay.kt:66-91`，每 80ms 更新时间并扫描项目
- 挂载处：`VideoPlayerSection.kt:3509-3529`

标准弹幕不是 Compose 每帧重组，并且已经有被动配置，这是正确优化；但返回峰值阶段仍会增加 View/Surface 合成和工作集。

**建议：** shared enter/return 开始时统一只保留封面或单帧视频，结束后再恢复所有播放器 Overlay。

### P1-3 Anime4K 没有转场、热状态和省电旁路

**证据：已确认，功能开启时触发**

- `Anime4KOutputPolicy.kt:24-44` 仅检查插件、GLES、HDR、PiP、音频和生命周期
- `Anime4KGLSurfaceView.kt:16-29` 每个输入视频帧请求渲染
- `Anime4KPipelineRenderer.kt:105-126,222-243` 执行 FBO、外部纹理拷贝和 CNN shader 链
- `Anime4KConfig.kt:49-63` 只按最大纹理尺寸限制，不按热状态主动降分辨率

**建议：** 任意转场期间强制 bypass；增加 thermal、省电、刷新率、输入像素数和 GPU frame-time gate。QUALITY 档不应与实时共享返回同时工作。

### P1-4 转场中可能发生 Surface 输出解绑/重绑

**证据：高概率**

- `VideoOutputRouter.kt:21-78`
- `VideoPlayerSectionPolicy.kt:1444-1467`

Anime4K/直出或视图类型变化会 clear/set surface，并可能临时把 `playerView.player` 置空后重绑。输出路由集中管理、防竞态的设计本身是正确的；风险在于这些切换若与转场、横竖屏或首帧同时发生，会把 Surface transaction 压进同一帧。

**建议：** 转场冻结期间禁止输出路由、Anime4K 配置和刷新率变化，结束后在下一帧或 idle 阶段统一应用。

### P1-5 首页同时挂载多套全屏背景采样源

**证据：已确认；部分路径由设置开关控制**

- `app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt:294-295,1481-1500`
- `app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt:1365-1380,1498-1518`

首页可同时存在 Kyant LayerBackdrop、Miuix LayerBackdrop、主 HazeSource 和壁纸 HazeSource。部分壁纸卡片采样源只有实时卡片玻璃开启才挂载，这是已经做对的条件化优化；但主内容和底栏仍可能同时维护不同采样体系。

**建议：** 建立单一“当前帧背景源”所有权，按消费者选择一种实现。转场期间冻结一次低分辨率快照给顶栏、底栏和卡片共用，避免多个库各自录制全屏源。

### P1-6 首页 Pager 连续状态在组合阶段被读取

**证据：已确认**

- 顶栏：`app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt:942-960,1069-1112,1804-1809`
- Hero 轮播：`app/src/main/java/com/android/purebilibili/feature/home/components/HomeHeroCarousel.kt:131-151,261-346`
- 液态顶栏隐藏测量树：`TopBar.kt:1245-1250,1417-1560`

Pager offset 是逐帧变化的连续值。如果在 Composable 主体读取，会使对应 UI 每帧重组；液态顶栏还额外组合一套 `alpha(0f)`、非 Lazy 的完整标签树用于测量。

**建议：** 连续 offset 通过 provider/lambda 在 `graphicsLayer` 或 draw 阶段读取；标签宽度只在数据、字体或容器尺寸变化时测量并缓存，不能通过一套常驻透明 UI 树逐帧参与组合。

### P1-7 首页列表身份与封面预取不稳定

**证据：已确认**

- LazyGrid key 混合稳定 ID 与 index：`HomeCategoryPage.kt:60-72,401-403`
- 预取请求：`HomeScreen.kt:2343-2384`、`HomeCoverRequestPolicy.kt:6-35`
- 实际卡片请求：`VideoCard.kt:161-172,824-838`

刷新或头部插入后，index 变化会让大量卡片换身份，引起组合状态失效与重新布局。预取和实际显示使用不同 URL、尺寸或 cache key 时，预取不能稳定命中，转场前后仍可能解码图片。

**建议：** key 只使用真正稳定且唯一的业务 ID；统一预取和展示的 URL 归一化、目标尺寸及 cache key。

### P1-8 底部导航最多保留五页，离屏工作可能继续运行

**证据：已确认/部分条件性**

- `AppTopLevelNavigationPolicy.kt:30-33,181-215`
- Hero 自动播放：`HomeHeroCarousel.kt:445-471`，默认关闭但缺少 `isTopLevelActive` 门控
- 首页骨架：`HomeFeedSkeletonCard.kt:45-59` 使用无限动画

预组合和保留页面能改善切页首帧，但如果离屏页面的自动播放、骨架 shimmer 或其他循环没有跟随可见性暂停，就会持续唤醒帧并增加温度。

**建议：** 保留组合状态，但统一向子页下发 `isTopLevelActive`；离屏停止所有无限动画、自动播放、传感器和高频轮询。

### P1-9 动态页模式切换同时保留两套重页面树

**证据：已确认**

- `app/src/main/java/com/android/purebilibili/feature/dynamic/DynamicScreen.kt:531-546`

`AnimatedContent` 在布局模式切换时同时保留新旧两套页面树；两侧都可能包含 Pager、动态列表、图片与 Haze，造成组合、测量和绘制成倍增加。

**建议：** 保留一套 Pager/列表，只切换必要的顶栏或侧栏；用轻量位移和透明度处理局部过渡。

### P1-10 动态页和番剧页在滚动中动画真实高度

**证据：已确认**

- 动态页：`DynamicScreen.kt:853-864` 使用 `expandVertically/shrinkVertically`
- 番剧页：`app/src/main/java/com/android/purebilibili/feature/bangumi/BangumiScreen.kt:229,259`
- 动态侧栏：`DynamicSidebar.kt:125`

高度或宽度动画会让父子布局逐帧重新测量。在滚动阈值附近反复触发时，列表、Pager、顶栏和模糊区域都会跟着重新布局。番剧筛选还用 AnimatedContent 同时保留两套完整网格。

**建议：** 保留固定布局占位，使用 `graphicsLayer` 位移、裁剪和透明度隐藏；筛选结果一次性替换，只让轻量遮罩或加载指示器淡入淡出。

### P1-11 动态页知道滚动状态，但模糊没有消费它

**证据：已确认**

- `DynamicScreen.kt:685-688,846-849` 已提供滚动状态
- `app/src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicTopBar.kt:46-83` 的 `unifiedBlur` 没有使用该状态降级

这不是动态页独有问题。`WatchLaterScreen.kt:576`、`CommonListScreen.kt:737-740`、`DynamicSidebar.kt:227`、`PartitionScreen.kt:431-433` 和 `TopReadabilityChrome.kt:45-48` 也让 `unifiedBlur` 使用默认的“未滚动、未转场”参数。`SpaceScreen.kt:265-269` 已经展示了正确接入方式。

**建议：** 所有列表 Header 先正确传入 `effectRole=HEADER`、`isScrolling` 和 `isTransitionRunning`。但还要注意，当前 `BlurBudgetPolicy.kt:53-94` 对 HEADER 在滚动/转场时只是禁止实时模式并把 inputScale 降到 0.88，并不会把模糊等级降为 0；因此还需要新增 BALANCED/CRITICAL 策略，在高风险转场中退化为半透明纯色。滚动停止 100-200ms 后再恢复，不要等掉帧守卫事后触发。

### P1-12 音频页是持续发热的独立高风险路径

**证据：高概率**

- `app/src/main/java/com/android/purebilibili/feature/audio/screen/MusicPlayerContent.kt:570`
- `app/src/main/java/com/android/purebilibili/feature/audio/viewmodel/MusicViewModel.kt:299`

音频页长期保留全屏 64dp 背景模糊，并叠加歌词逐行模糊和 Backdrop；播放进度每 250ms 更新整份 UI State，可能带动进度、歌词、背景和播放器信息共同重组。

**建议：** 封面变化时一次性生成低分辨率预模糊位图；播放期间只显示缓存结果。进度拆成独立小状态，只由进度条读取；歌词只在当前行索引变化时更新。

### P1-13 番剧播放器的三个定时循环没有随可见性或播放状态暂停

**证据：已确认，进入番剧播放页后触发**

- `app/src/main/java/com/android/purebilibili/feature/bangumi/BangumiPlayerScreen.kt:212-218`：SponsorBlock 开启且加载成功后，每 `500ms` 调用一次 `checkAndSkipSponsor`，循环条件只有设置和页面状态。
- `app/src/main/java/com/android/purebilibili/feature/bangumi/ui/player/BangumiPlayerComponents.kt:299-315`：播放器主控件每 `200ms` 读取播放状态、时长、位置并写入四个 Compose State。
- 同文件 `:757-775`：迷你进度条再以 `200ms` 独立读取当前位置和缓冲位置。
- 该文件其实已经取得 `hostLifecycleStarted`（`:181-183`），并用于 Anime4K Surface 暂停（`:254-274`）；上述三处循环却没有使用它，也没有检查 `exoPlayer.isPlaying` 或“控件当前可见”。

**影响：** 这三条任务会让同一个播放器最多每秒触发约 12 次主线程状态更新/位置读取，并且在暂停播放、控制层隐藏、Activity 进入后台或导航容器暂时保留 Composable 时仍可能继续运行，直到 Composable 真正销毁。它不是视频卡共享转场的首要根因，但会在番剧页面切换、返回和热机状态下持续吃掉本可留给动画的主线程预算。

**建议：** 抽出一个单一进度源，只在 `hostLifecycleStarted && player.isPlaying && controlsVisible` 时以 250-500ms 更新；迷你进度条复用该状态，不能再各自轮询。SponsorBlock 改为监听播放器位置/播放状态，并在暂停、后台、转场和页面不可见时停止检查。这里的“停止”是暂停 UI 轮询，不是丢弃播放进度或 SponsorBlock 数据。

### P1-14 直播聊天高频消息会反复破坏列表身份并启动滚动动画

**证据：已确认，直播聊天活跃时触发**

- `app/src/main/java/com/android/purebilibili/feature/live/components/LiveChatSection.kt:106-116,194`
- `app/src/main/java/com/android/purebilibili/feature/live/components/LandscapeChatOverlay.kt:45-53,95`

每条消息都在主线程加入 `mutableStateList`；超过 200/50 条时从头部 `removeAt(0)`。两个 LazyColumn 的 `items(messages)` 没有稳定 key，并且每条消息都调用 `animateScrollToItem`。头删会改变全部 index 身份，使列表复用和行状态失效；高频消息还会让上一条滚动动画没结束就被下一条打断并重启。横屏时它与视频解码、弹幕和播放器图层同时运行。

**建议：** 给消息补单调 receipt id 并作为稳定 key；使用环形缓冲或反向列表避免频繁头删；直播自动跟随可合帧/节流并使用 `scrollToItem`，只在用户从非底部主动恢复跟随时做一次动画滚动。

### P1-15 图片预览逐帧新建全屏 RenderEffect

**证据：已确认，打开/关闭大图预览时触发**

- `app/src/main/java/com/android/purebilibili/feature/dynamic/components/ImagePreviewDialog.kt:379-416,562-589`
- `app/src/main/java/com/android/purebilibili/feature/dynamic/components/ImagePreviewTransitionPolicy.kt:12-14,156-174`

320/300ms 的进退场会让包含 HorizontalPager、当前大图和相邻预取页的近全屏内容层，在 `graphicsLayer` 更新时直接调用 `RenderEffect.createBlurEffect(...).asComposeRenderEffect()`。模糊半径随 progress 连续变化，无法复用缓存，因此每个动画帧都会创建新的 GPU 效果对象。

**建议：** 默认只做 opacity/scale；必须保留模糊时，将半径量化为 0/轻/中等少量档位并缓存 RenderEffect，或只模糊缩略图阶段的小图层。该路径也必须消费统一视觉预算。

### P1-16 删除卡片会同时启动 PixelCopy、粒子 GL 与全屏卡片抖动

**证据：已确认，启用消散删除动画时触发**

- `app/src/main/java/com/android/purebilibili/core/ui/animation/ParticleDissolveEffect.kt:101-167,225-235,246-283,336-346,359-395,451-473`
- `app/src/main/java/com/android/purebilibili/core/ui/animation/gl/ParticleRenderer.kt:146-189,194-260`
- `app/src/main/java/com/android/purebilibili/feature/list/WatchLaterScreen.kt:823-831`

删除单卡会用 PixelCopy 捕获 ARGB_8888 位图，再按 2/3 像素步长生成大量粒子及四套 FloatBuffer；GL 使用 `RENDERMODE_CONTINUOUSLY` 持续绘制。同时全局 DissolveAnimationManager 会让当前屏内其他卡片进入两条无限抖动动画，卡片收拢又通过真实 `height` 动画逐帧重测列表。

**建议：** 性能档只保留一种反馈：约 135ms alpha/scale 或有限粒子，不同时启动全屏卡片抖动。限制粒子数量并先缩小采样图；动画完成立即停止连续 GL；列表收拢交给 Lazy item placement 或最终一次布局，不逐帧动画高度。

### P1-17 听视频页液态分段控件在组合阶段逐帧读取 Pager offset

**证据：已确认，滑动音频/听视频 Pager 且液态控件启用时触发**

- `app/src/main/java/com/android/purebilibili/feature/audio/screen/ListenVideoScreen.kt:190-205`
- `app/src/main/java/com/android/purebilibili/feature/audio/screen/MusicPlayerContent.kt:281-303`
- `app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarLiquidSegmentedControl.kt:511-535,680-880`

`pagerState.currentPageOffsetFraction` 通过 lambda 传入控件，但控件在 `BoxWithConstraints` 的组合阶段两次调用 provider，再根据结果计算 Dp offset、指示器宽度，并同时维护多套标签树和 Backdrop 捕获。它没有真正把连续状态限制在 draw/graphicsLayer 阶段。

**建议：** 每帧 offset 只读取一次并下沉至 draw/graphicsLayer；滑动期间使用轻量 underline 或暂停液态背景采样，标签树布局仅在容器、字体或项目变化时更新。

### P1-18 视频评论楼中楼切换同时保活两套列表

**证据：已确认，打开或关闭楼中楼评论时触发**

- `app/src/main/java/com/android/purebilibili/feature/video/ui/components/VideoCommentSheetHost.kt:642-791`
- `app/src/main/java/com/android/purebilibili/feature/video/ui/components/SubReplyDetailComponents.kt:438,678`

主评论 LazyColumn 与子回复 LazyColumn 通过 AnimatedContent 同时保活约 220-260ms，并使用 `SizeTransform(clip=false)`。主列表还包含 LayerBackdrop。视频继续播放时，两套全屏列表会同时组合、测量和绘制。

**建议：** 性能档只保留一个 active LazyList，内容即时切换；动画仅作用于轻量 header/前景层。FULL 档若保留双树，也应在切换时暂停背景采样和播放器非必要 Overlay。

### P1-19 横屏播放器末端抽屉以真实 padding 逐帧挤压视频和全部控制层

**证据：已确认；手机横屏全屏打开或关闭末端抽屉时触发**

- 宽度动画：`app/src/main/java/com/android/purebilibili/feature/video/ui/section/VideoPlayerSection.kt:1207-1216`
- 播放器内容：同文件 `:1403-1411`
- 字幕：同文件 `:3799-3819`
- Overlay 参数传递：同文件 `:4786-4795`
- 控制层、上下渐变和进度条消费该 padding：`app/src/main/java/com/android/purebilibili/feature/video/ui/overlay/VideoPlayerOverlay.kt:950-952,1203-1266`

横屏末端抽屉的保留宽度由 220ms `animateDpAsState` 驱动，并直接用于播放器内容、字幕、整个 Overlay、顶部/底部渐变和进度条的 `padding(end = ...)`。padding 是布局约束，不是合成属性；每一帧都会重新测量这些覆盖层并改变视频可用区域。

**建议：** 抽屉采用固定尺寸 Overlay，以 `graphicsLayer.translationX` 从右侧进入；动画期间播放器和控制层保持完整宽度。视觉必须避让时，在抽屉稳定后一次性提交最终安全区域，不能把逐帧 Dp padding 传给整套控制树。

## 5. P2：条件性或局部问题

1. `BottomBar.kt:676`：启用“首页+搜索”底栏后会同时动画 Dock、搜索框、间距的真实宽高，并叠加玻璃/模糊。该功能默认关闭。
2. `FollowingListScreen.kt:1183-1190`：批量选择时，计数变化叠加两个 Animatable、AnimatedContent 与文字模糊。
3. `MainActivity.kt:680`：Android 14/15 冷启动飞出动画最长约 920ms，每帧创建多项模糊效果；影响启动峰值，不是持续发热主因。
4. `OnboardingScreen.kt:707-747`、`LoginComponents.kt:193-205`、多个骨架组件存在无限动画。单页问题不大，但必须跟生命周期与可见性暂停。
5. `OnboardingBottomSheet.kt:321-352,452-471`：首次引导的两个页面同时使用无限循环 Lottie，第二页还叠加 `rememberInfiniteTransition`。它只在首次引导弹窗存在时发生，且切走 Pager 页面时通常会被释放，因此不是当前 K60 转场主因；若底部弹窗以后改为预组合或长期保留，必须把动画门控到 `visible && pagerState.currentPage == page`。
6. 直播弹幕已有活动/等待队列上限和 100ms 批处理，是正确方向；每批仍执行排序、复制、setData/start/invalidate，应该纳入持续播放功耗基准，但不是视频卡共享转场 P0。
7. `TabletCinemaLayout.kt:214-218,352-354`：仅平板影院布局会在约 240ms 内动画侧栏真实宽度，逐帧改变播放器和列表约束；K60 手机布局不触发。建议改为 overlay + translationX，动画结束后一次性提交最终宽度。
8. `CacheClearAnimation.kt:107-225,242-342,730-819`：清缓存弹窗以固定 16ms 循环复制最多约 70 个粒子状态，并叠加三条无限圆环动画，Canvas 还会反复创建 sweepGradient。作用范围仅 200dp 且只在清缓存时触发，应缓存 Brush、原地更新粒子并在后台/完成后停止。

## 6. 当前实现中应保留的正确优化

以下代码不应被误判或推倒重写：

1. 视频卡来源页景深只有一层，不是两层全屏模糊。
2. `VideoCardTransitionBackgroundPolicy.kt:655-802` 只在开始时录制一次全屏 display list，后续复用图层；不是每帧重录 Feed。
3. `VideoCardShellSharedBounds.kt:202-239` 使用 `scaleToBounds`，主要风险是大纹理缩放、裁剪与合成，不是每帧重新测量整个详情页。
4. 视频共享返回明确排除普通预测返回模糊，两条链路没有同时叠加。
5. `HomeGlassVisualPolicy.kt:29-44` 已关闭全局壁纸第二层动态景深。
6. 根节点旧 `animateContentSize` 已移除。
7. 详情页重型内容已经延后到入场结束，首页返回的顶栏、交互和卡片入场也已有错峰设计。
8. 系统“减弱动效”能够关闭卡片共享转场。
9. `VideoOutputRouter` 集中管理输出绑定、Anime4K 使用 `RENDERMODE_WHEN_DIRTY`、普通弹幕使用被动配置，都是合理基础设施。

## 7. 不要重复的错误实验

历史报告 `PERFORMANCE_AUDIT.md:199-232` 记录过旧版 60Hz 模拟器 Debug 数据：p95 中位数约 62.68ms、Janky frames 中位数约 15.61%，返回时 `measureAndLayout` 峰值约 267.7-449.8ms。这些数据能说明历史问题很重，但不能代表当前 K60 成绩。

历史上以下单变量实验没有改善，部分还变差：

- 单独移除 BlurEffect。
- 整个会话不挂 RenderEffect。
- 移除全屏冻结层。
- 延后整个播放器宿主挂载。
- 用过度轻量的返回落点，曾导致白屏。

因此不要再把“全部关模糊”或“延迟创建播放器”当万能方案。应优先拆开同帧工作，测量首页恢复布局、Surface/TextureView、高刷切换和视频/弹幕合成。

## 8. 建议实施顺序

### 第一阶段：P0 峰值削减

1. 建立转场性能档，并在动画开始前锁定档位。
2. 转场期间保持原刷新率、输出 Surface、Anime4K 配置和视觉树不变。
3. 默认封面 morph + SurfaceView；BALANCED/CRITICAL 返回冻结视频并关闭 Overlay。
4. 普通预测返回改为立即提交，模糊清理与退出动画并行，总收尾 220-280ms。
5. 把所有 Haze/Kyant/Miuix/RenderEffect 接入统一预算，消除硬编码 `forceLowBlurBudget=false`。

### 第二阶段：持续负载和布局削减

1. 高级弹幕去除 16ms Compose 全表扫描；转场期间统一冻结 Overlay。
2. 首页合并背景采样源，Pager 连续状态下沉到绘制/合成阶段。
3. 修复稳定 key、封面预取 cache key 与离屏页面生命周期。
4. 动态/番剧取消重页面双树和真实尺寸动画。
5. 音频背景改预模糊缓存，拆分播放进度状态。

### 第三阶段：编译与测量体系

1. 接通 Baseline Profile（基线配置文件，用来让常用代码在安装后提前编译）。当前只有 ProfileInstaller 依赖，`app` 未接入 `androidx.baselineprofile` 插件，也没有生成的 profile 文件。
2. Macrobenchmark 除 FrameTiming 外增加连续热衰减实验，记录温度、CPU/GPU 频率、功耗和首次降频时间。
3. 给所有转场添加稳定 trace section，区分 Compose measure/layout、图层录制、GPU 模糊、Surface transaction 与图片解码。

## 9. Redmi K60 真机验证矩阵

项目规则禁止审查者读取或分析截图，因此下面的真机交互与视觉正确性需要用户亲自确认；自动采集可用 Perfetto、Macrobenchmark 和 `dumpsys` 完成。

### 测试组合

- 60Hz / 120Hz。
- 卡片共享转场开 / 关。
- 顶栏、底栏实时模糊或液态玻璃开 / 关。
- 冷机首轮 / 连续往返 20-30 次。
- 弹幕关 / 普通弹幕开 / 高级弹幕较多的视频。
- Anime4K 关 / FAST / QUALITY。

### 固定动作

1. 首页同一张视频卡进入详情，等首帧，立即返回，连续 30 次。
2. 视频播放 10 分钟后重复同样往返。
3. 普通页面执行预测返回：慢拖取消、慢拖提交、快速提交各 10 次。
4. 动态页在折叠阈值附近滚动，并切换横向/侧栏模式。
5. 番剧页切换筛选，音频页连续播放 10 分钟。

### 必须记录

- p50 / p95 / p99 帧耗时。
- Janky frames 百分比和连续超时帧数量。
- 主线程 Compose `recompose / measure / layout / draw` 时间。
- RenderThread、GPU completion 和 SurfaceFlinger 时间。
- CPU/GPU 频率、设备 thermal status、功耗和首次降频时间。
- 返回落位是否白屏、黑帧、封面跳变、视频瞬断或底栏闪烁。

### 建议验收线

- 120Hz：大多数动画帧低于 8.33ms，p95 至少显著接近单帧预算；不出现长串连续超时。
- 60Hz：p95 目标低于 16.67ms，单次转场不出现超过 100ms 的主线程停顿。
- 连续 30 次往返后，p95 与冷机相比退化不超过 15%，不因动画触发明显热降频。
- FULL/BALANCED/CRITICAL 三档的视觉差异可接受，BALANCED 作为默认时不出现黑帧或白屏。

## 10. 测量缺口与当前边界

- 现有 Macrobenchmark 已覆盖 FrameTiming、视频卡打开/返回和预测返回，但指标几乎只有 FrameTiming。
- `baselineprofile/build.gradle.kts:19-20,50-55` 虽然启用了 Perfetto tracing 依赖与 full tracing 参数，但各帧基准仍只声明 `FrameTimingMetric`；没有用 trace section 指标把 Compose、RenderThread、Surface transaction、图片解码和播放器输出分开归因。
- `scripts/card_transition_gfxinfo.sh:132-145`、`scripts/mobile_perf_collect.sh:94-106` 只采集 `gfxinfo` 和进程 PSS（内存占用）。这些脚本没有采集电池电流/电压、thermal status、CPU/GPU 频率、显示刷新率实际值或连续样本时间线，因此不能验证“发热、降频、120Hz 合成”这条因果链，也无法比较冷机与连续往返后的退化。
- `scripts/card_transition_gfxinfo.sh:25-29` 有意在采样窗口关闭 Perfetto 以降低干扰，这是合理的低侵入帧统计策略；但它必须搭配另一套短时 Perfetto/系统计数器采样，而不是被当作性能归因工具。
- `app/src/test/java/com/android/purebilibili/core/ui/performance/FrameBudgetLintTest.kt:77-98,211-213` 只限制无半径守卫的 `createBlurEffect` 文件数量，当前仍显式允许图片预览和冷启动等已知热点；这个护栏能阻止数量继续扩散，但不能发现“同一调用点每帧创建”的实际成本。
- 当前 Baseline Profile 生成流程覆盖启动、首页、搜索、设置、空间、网页和一次视频详情（`baselineprofile/src/main/kotlin/com/android/purebilibili/baselineprofile/BiliPaiBaselineProfileGenerator.kt:20-61`），但没有反复“卡片进入 -> 首帧 -> 返回”的共享转场热路径，也没有番剧播放器、TextureView/SufaceView 两种输出、120Hz 或热状态分组；即使插件接通，profile 也不会自动覆盖本报告的核心峰值路径。
- Baseline Profile 生成器存在：`baselineprofile/src/main/kotlin/com/android/purebilibili/baselineprofile/BiliPaiBaselineProfileGenerator.kt:15-25`。
- `app/build.gradle.kts:426-427` 只有 ProfileInstaller 依赖；ProfileInstaller 不等于发布包已经包含 Baseline Profile。
- 本报告没有构建 APK、运行单元测试、修改业务源码或读取截图。
- 源码审查能确定高风险链路，但不能替代 K60 上的 Perfetto CPU/GPU/thermal 实测。
