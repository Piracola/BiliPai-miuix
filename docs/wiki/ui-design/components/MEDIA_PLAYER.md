# 媒体与播放器组件

> 文档编号：UI-COMP-06  
> 规范版本：1.0.0-draft  
> 状态：草案  
> 最后核对日期：2026-08-02  
> 适用提交：4443e72ff  
> 维护角色：播放器维护者、设计系统维护者  
> 相关文档：[组件目录](README.md) · [动效与效果](../06_MOTION_EFFECTS.md) · [页面母版](../08_PAGE_TEMPLATES.md)

## 初学者解释

播放器不是普通卡片。它同时管理视频画面、播放状态、手势、控制层、全屏、画中画、后台媒体会话和生命周期。因此播放器组件可以保留领域专用入口，但依然必须使用公共 Token、可访问语义和双主题功能合同。

## 规范要求

### 媒体画面与控制层共同规则

- 媒体区域**必须**有稳定比例或全屏边界；加载文字和控制层不能改变画面尺寸。
- 播放、暂停、缓冲、重试、结束、不可播放必须是可区分状态。
- 单击显示/隐藏控制层，拖动进度、双击、长按倍速等手势必须有普通按钮替代。
- 控制层隐藏后仍要保留系统返回和可访问的唤起方式。
- 全屏、竖屏、PiP、后台、迷你播放器只能有明确的播放 session 所有者。
- 双主题可以改变控制壳与动效，不能改变画质、速度、弹幕、选集、播放列表等功能。

### C501 设置面板：`VideoSettingsPanel`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 播放中的画质、速度、弹幕与播放器偏好；页面级永久设置应进入 Settings |
| 结构 / 变体 | 分类、当前值、Switch/Arrow/Slider Preference、关闭；Sheet/侧面板 |
| 尺寸 / Token | 使用 `AppPreference*`；触摸 48dp；不遮掉必须观察的媒体状态 |
| 状态 | loading options、available、restricted、saving、error |
| 交互 | 选择立即或明确确认生效；失败回滚并说明；返回关闭面板而非退出播放 |
| 文案 / 无障碍 | 显示当前值与会员/登录限制；读屏顺序按分类 |
| 响应式 | Compact 用 Sheet；横屏/Expanded 可侧面板，保持媒体可见 |
| 双主题映射 | Preference 底层可变；选项、限制和值一致 |
| Compose 入口 | 当前 `video/ui/components/VideoSettingsPanel.kt` 中 `VideoSettingsPanel` |
| 当前差距 / 验收 | 部分控件仍直接使用主题/局部样式；检查画质失败、面板返回和旋转 |

### C502 分 P 选择：`video.ui.components.PagesSelector`（目标收口）

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 普通视频多 P 选择；番剧选集使用番剧领域组件，不混同数据合同 |
| 结构 / 变体 | 当前项、列表/网格、筛选、标题、进度；compact/expanded |
| 尺寸 / Token | 项目触摸 48dp；长标题两行或详情；当前项位置稳定 |
| 状态 | loading、normal、current、played、disabled、error、empty |
| 交互 | 选择后切换同一播放 session；失败保留当前播放；重新打开定位当前项 |
| 文案 / 无障碍 | 读出“第 n P、标题、当前播放/已看”；不能只用颜色标记 |
| 响应式 | Compact Sheet/列表；Expanded 可固定侧区；旋转保持选择和滚动 |
| 双主题映射 | 选中容器和反馈可变；顺序、状态和切换结果一致 |
| Compose 入口 | 目标为 `feature/video/ui/components/PagesSelector.kt` 中入口 |
| 当前差距 / 验收 | `video/player` 另有同名实现；按 UI-GAP-005 对比迁移，不直接删除 |

### C503 迷你播放器：`MiniPlayerOverlay`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 离开详情后保持当前媒体并提供最少控制；无有效 session 不显示空壳 |
| 结构 / 变体 | 封面/画面、标题、播放暂停、关闭、点击展开；音频/视频 |
| 尺寸 / Token | 壳尺寸稳定，位于主导航上方且不遮 Snackbar；使用 mini-player shell policy |
| 状态 | loading、playing、paused、error、dismissing |
| 交互 | 点击展开回正确目的地；关闭停止/释放的后果明确；按钮事件不触发整壳点击 |
| 文案 / 无障碍 | 读出标题和播放状态；播放/暂停描述随状态变化 |
| 响应式 | Compact 位于底栏上，Expanded 与 Rail/内容区协调；键盘出现时不遮输入 |
| 双主题映射 | MIUIX 壳可用 primary 强调与较扁 elevation；功能与点击区一致 |
| Compose 入口 | 当前 `MiniPlayerOverlay`，外观由 `MiniPlayerOverlayShellPolicy` 决定 |
| 当前差距 / 验收 | 导航/迷你播放器/Snackbar inset 组合需完整矩阵检查 |

### C504 播放器操作按钮：`PlayerActionButton`（目标待建）

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 播放、暂停、快进、画质、全屏、点赞等播放器上下文动作；普通确认不用 |
| 结构 / 变体 | 48dp 命中区、图标、可选标签/数值、选中/禁用/加载 |
| 尺寸 / Token | 图标常见 24dp，命中区至少 48dp；控制层布局尺寸稳定 |
| 状态 / 交互 | pressed、selected、disabled、loading；按压动效不延迟真实动作 |
| 文案 / 无障碍 | 动作名称随状态变化；数值不是唯一名称 |
| 响应式 | 控件不足时低频项进“更多”，核心播放/返回始终可达 |
| 双主题映射 | 图标容器与反馈可变；命中、顺序和状态一致 |
| Compose 入口 | 目标 `PlayerActionButton`；当前多份 `ActionButton/BiliActionButton` 待分类 |
| 当前差距 / 验收 | UI-GAP-001；检查手势冲突、长按、横竖屏与 TalkBack |

### C505 播放状态层：`PlayerStatusOverlay`（目标待建）

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 缓冲、不可播放、重试、播放结束；普通页面网络错误不用播放器层 |
| 结构 / 变体 | 进度/图标、标题、说明、重试/登录/返回；局部提示/阻塞状态 |
| 尺寸 / Token | 位于媒体安全区，不遮返回；文字表面保证动态画面对比 |
| 状态 / 交互 | buffering、recoverable error、restricted、ended；重试防重复 |
| 文案 / 无障碍 | 说明失败/限制与下一步；状态变化不过度重复播报 |
| 响应式 | 全屏与嵌入式都居中在媒体区，不按整个页面居中 |
| 双主题映射 | 指示器和表面可变；原因与动作一致 |
| Compose 入口 | 目标 `PlayerStatusOverlay` |
| 当前差距 / 验收 | 状态反馈分散在视频/番剧/直播；需先定义共同状态合同 |

## 文字线框

```text
┌──────────────────────────────┐
│ 返回           标题   更多   │
│                              │
│         视频画面             │
│      [缓冲/错误状态层]       │
│                              │
│ 00:18 ━━━━━●━━━━━━ 04:20     │
│ 播放  弹幕  画质  倍速  全屏 │
└──────────────────────────────┘
```

## Compose 短示例

```kotlin
VideoSettingsPanel(
    state = settingsState,
    onQualitySelected = onQualitySelected,
    onDismiss = onDismiss
)
```

## 代码映射

- 播放设置：`VideoSettingsPanel.kt`
- 分 P：两份 `PagesSelector.kt`
- 迷你播放器：`MiniPlayerOverlay.kt`、`MiniPlayerOverlayShellPolicy`
- 播放器原语与控制层：`feature/video/player/`、`feature/video/ui/`
- 番剧播放：`feature/bangumi/ui/player/`

## 当前差距

普通视频、番剧、直播、离线与音频播放器的状态组件尚未形成一个共同语义合同；播放器操作按钮和 PagesSelector 有重复。领域差异真实存在，收口前必须先分类，不进行机械替换。

## 验收方法

检查首次加载、缓冲、正常、失败、离线、未登录/会员限制、播放结束；再检查横竖屏、1600dp、全屏、PiP、后台、迷你播放器和三种返回；记录文字结果，不要求截图。

