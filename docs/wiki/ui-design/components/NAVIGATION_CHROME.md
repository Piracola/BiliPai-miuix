# 导航与应用外壳组件

> 文档编号：UI-COMP-03  
> 规范版本：1.0.0-draft  
> 状态：草案  
> 最后核对日期：2026-08-02  
> 适用提交：4443e72ff  
> 维护角色：设计系统维护者、导航维护者  
> 相关文档：[组件目录](README.md) · [布局与自适应](../05_LAYOUT_ADAPTIVE.md)

## 初学者解释

导航回答“我在哪里、还能去哪里、怎样回来”，应用外壳负责让顶部栏、内容、底栏、侧栏和系统安全区不互相遮挡。它们必须由少数顶层组件统一拥有；业务页若再造一套外壳，常会出现双底栏、重复 inset 或错误返回。

## 规范要求

### C201 页面外壳：`AppScaffold`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 组织单页 topBar/content/bottomBar/snackbar；叶子卡片不用 Scaffold |
| 结构 / 变体 | 顶栏、内容、可选底栏/FAB/Snackbar；应用根与页面两种所有权不能重复 |
| 尺寸 / Token | 系统 inset 与 chrome 高度由外壳计算；内容消费一次 padding |
| 状态 / 交互 | 加载和错误属于内容，不应重建外壳；系统返回由导航所有者处理 |
| 文案 / 无障碍 | 主内容区顺序位于导航之后；全局提示有合适 live region |
| 响应式 | Compact 可底栏，Expanded 可 Rail/Sidebar；不简单拉伸内容 |
| 双主题映射 | 底层 Scaffold 和表面可变；槽位职责与 inset 一致 |
| Compose 入口 | 当前 `AdaptiveChrome.kt` 中 `AppScaffold` |
| 当前差距 / 验收 | 旧页面可能直接用具体 Scaffold；检查双 inset、键盘和迷你播放器 |

### C202 顶部栏：`AppTopBar`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 页面身份、返回与少量高频动作；不能塞入完整筛选表单 |
| 结构 / 变体 | 导航图标、标题、副标题、最多少量动作、overflow；固定/滚动变体 |
| 尺寸 / Token | chrome token 决定高度；每个动作 48dp；标题有稳定剩余宽度 |
| 状态 / 交互 | 返回、动作、滚动折叠；disabled 动作可解释或隐藏但双主题一致 |
| 文案 / 无障碍 | 标题准确；返回描述为“返回”；菜单动作有名称 |
| 响应式 | Compact 收入 overflow；Expanded 可显示上下文但不重复侧栏标题 |
| 双主题映射 | MIUIX/Material 3 标题节奏可变；动作顺序和含义一致 |
| Compose 入口 | 当前 `AdaptiveChrome.kt` 中 `AppTopBar` |
| 当前差距 / 验收 | 首页 TopBar 有局部定制；检查最长标题、滚动、返回与菜单 |

### C203/C204 主底部导航

| 字段 | `AppBottomNavigationHost` | `AppNavigationBar` |
|---|---|---|
| 用途 | 管理主导航与浮动/贴底策略 | 渲染目的地项、标签、角标 |
| 禁用场景 | 子页面不再创建一个宿主 | 临时操作不用导航项 |
| 结构 | 内容、底栏、可选迷你播放器/inset | 图标、选中图标、标签、badge |
| 尺寸 | 稳定高度与安全区；不得被标签撑大 | 每项至少 48dp；数量与标签宽度受约束 |
| 状态 | visible/hidden/floating/docked | selected/unselected/disabled/badged |
| 交互 | 滚动或全屏隐藏策略需可预测 | 选择当前项可回顶等行为统一定义 |
| 文案/无障碍 | 内容不能被底栏遮挡 | 读出标签、选中状态和 badge 含义 |
| 响应式 | Expanded 切 Rail/Sidebar | `TextOnly` 等视觉模式不改变目的地 |
| 双主题 | 壳与动画可不同 | Miuix 可映射其支持的 label 模式 |
| 入口 | `AppBottomNavigationHost` | `AppNavigationBar` |
| 差距/验收 | BottomBar 正在用户改动，本任务不修改 | 检查顺序、显隐、角标、旋转和大字体 |

### C205 侧边导航：`AppPlatformNavigationRail`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | Expanded 主目的地导航；不能与同级底栏同时显示 |
| 结构 / 变体 | 顶部可选动作、目的地、角标；折叠 Rail/展开 Sidebar |
| 尺寸 / Token | 宽度稳定，点击目标 48dp，内容从剩余区开始 |
| 状态 / 交互 | selected、hover/focus、expanded；切换不改变当前目的地 |
| 文案 / 无障碍 | 折叠态图标必须有名称和 tooltip；展开态避免重复读标签 |
| 响应式 | 由可用宽度和用户侧栏设置决定；窄屏回到底栏 |
| 双主题映射 | 使用对应 NavigationRail 表现；目的地与顺序一致 |
| Compose 入口 | 当前 `AppPlatformNavigationRail` |
| 当前差距 / 验收 | 侧栏/底栏切换状态保留需持续验证 |

### C206 页签：`AppScrollableTabRow`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 同一页面内平级内容视图；不同信息架构目的地不用页签伪装 |
| 结构 / 变体 | 标签、选中指示、可选 badge；固定/可滚动/分段 |
| 尺寸 / Token | 标签触摸高度至少 48dp，指示器不改变布局高度 |
| 状态 / 交互 | selected、pressed、disabled；滑动页面与点击同步且不抢系统手势 |
| 文案 / 无障碍 | 短名词；读出“第 n 个，共 m 个”和选中状态 |
| 响应式 | 窄屏可滚动，宽屏不无意义拉满；过多分类考虑菜单/筛选 |
| 双主题映射 | Tab/Segmented 外观可变；索引和内容一致 |
| Compose 入口 | 当前 `AppScrollableTabRow` |
| 当前差距 / 验收 | 各业务分段控件实现仍有差异；检查状态同步和字体放大 |

### C207 分栏：`AppAdaptiveSplitLayout`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 列表-详情或目录-内容双栏；两个无关页面不能硬拼 |
| 结构 / 变体 | 主栏、详情栏、分隔/间距、空详情 placeholder |
| 尺寸 / Token | 明确主次最小/最大宽度；内容不被 divider 或 Rail 遮挡 |
| 状态 / 交互 | 无选择、已选择、详情加载/失败；返回先关闭详情语义 |
| 文案 / 无障碍 | 阅读顺序先主栏后详情；选择变化合理通知，不抢焦点 |
| 响应式 | Compact 退化成 push 导航；切换保留选择和滚动 |
| 双主题映射 | 分隔与表面可变，栏职责和返回一致 |
| Compose 入口 | 当前 `AppSplitLayout.kt` 中 `AppAdaptiveSplitLayout` |
| 当前差距 / 验收 | 设置已有特化 Shell；检查 840dp 临界与预测返回 |

## Compose 短示例

```kotlin
AppScaffold(
    topBar = { AppTopBar(title = { AppText(title) }) }
) { contentPadding ->
    Content(Modifier.padding(contentPadding))
}
```

## 代码映射

- `AdaptiveChrome.kt`
- `AppBottomNavigationHost.kt`
- `AppNavigationComponents.kt`
- `AppPrimitiveComponents.kt` 中的 tab/drawer
- `AppSplitLayout.kt`

## 当前差距

BottomBar、TopBar、设置平板 Shell 存在用户未提交修改，本次规范只读取现状不改变代码。应用根与页面外壳的 inset 所有权仍需逐页核对。

## 验收方法

在 360/700/840/1000dp 切换主目的地和子页面，确认只显示一套同级导航；内容不被系统栏、底栏或迷你播放器遮挡；三种返回方式结果一致。

