# 弹层与反馈组件

> 文档编号：UI-COMP-05  
> 规范版本：1.0.0-draft  
> 状态：草案  
> 最后核对日期：2026-08-02  
> 适用提交：901a11954  
> 维护角色：设计系统维护者  
> 相关文档：[组件目录](README.md) · [无障碍](../07_ACCESSIBILITY.md)

## 初学者解释

弹层暂时盖在当前页面上，反馈告诉用户操作结果或当前状态。选择对话框、底部面板、菜单还是 Snackbar，要看用户是否必须立即决定、内容有多少、是否与触发位置相关。弹层越强，越会打断当前任务，因此不能所有提示都做成对话框。

## 规范要求

### C401 对话框：`AppAlertDialog`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 必须确认的风险、阻塞错误或少量关键选择；普通成功不弹窗 |
| 结构 / 变体 | 标题、说明、确认、取消、可选图标；确认/警告/危险 |
| 尺寸 / Token | `ContainerLevel.Dialog`；Compact 留边，Expanded 限制阅读宽度 |
| 状态 / 交互 | open、submitting、error；系统返回/点外关闭是否允许必须明确 |
| 文案 / 无障碍 | 标题说对象，正文说后果；打开聚焦标题/主要内容，关闭回触发项 |
| 响应式 | 按钮文字长时纵向排列；不能横向挤压截断 |
| 双主题映射 | 对话框外观和按钮顺序遵循适配，但主要/危险含义一致 |
| Compose 入口 | 当前 `AppAlertDialog`，动作可用 `AppDialogAction` |
| 当前差距 / 验收 | 各业务确认文案质量不一；检查返回、外部点击、提交失败和大字体 |

### C402 底部面板：`AppModalBottomSheet`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 与当前上下文相关的选项、筛选或补充内容；关键不可逆确认不用只靠下滑关闭 |
| 结构 / 变体 | drag handle、标题、内容、动作；短选项/可滚动/表单 |
| 尺寸 / Token | `ContainerLevel.Sheet`；内容避开导航栏；Expanded 限宽或改适合的弹层 |
| 状态 / 交互 | hidden/partial/expanded/submitting；拖动、点外与返回关闭结果一致 |
| 文案 / 无障碍 | 打开后焦点限制在面板，关闭回触发项；drag handle 有合理语义或不聚焦 |
| 响应式 | Compact 从底部；宽屏可保持底部但限制宽度，复杂编辑考虑 Dialog/Pane |
| 双主题映射 | 圆角、handle、动画可变；关闭与提交语义一致 |
| Compose 入口 | 当前 `AppModalBottomSheet`、`AppBottomSheetDragHandle` |
| 当前差距 / 验收 | 业务 Sheet 高度与滚动策略不统一；检查键盘、旋转和预测返回 |

### C403 Snackbar：`AppSnackbar`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 非阻塞的短结果与一个可选恢复动作；需要阅读或输入时不用 |
| 结构 / 变体 | 消息、可选动作、可选 dismiss；普通/错误（非阻塞） |
| 尺寸 / Token | 不遮底栏、迷你播放器和输入法；文本可两行但不无限增长 |
| 状态 / 交互 | queued/visible/dismissed/action；相同消息去重，危险撤销时间足够 |
| 文案 / 无障碍 | 结果明确，如“已从稍后再看移除”；live region 不频繁打断 |
| 响应式 | Compact 留边，Expanded 限宽并靠近内容语境 |
| 双主题映射 | 造型与时长细节可变；消息、动作和队列一致 |
| Compose 入口 | 当前 `AppSnackbar`、`AppSnackbarHost` |
| 当前差距 / 验收 | 提示系统可能混用 Toast；检查队列、底栏遮挡与 TalkBack 时间 |

### C404 菜单：`AppDropdownMenu`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 与触发点相关的低频操作集合；高频模式选择应直接可见 |
| 结构 / 变体 | 菜单容器、项目、图标/选中、分隔；普通/单选/危险项 |
| 尺寸 / Token | 项目至少 48dp；宽度容纳最长关键文案并限制最大值 |
| 状态 / 交互 | open/selected/disabled；点击外部或返回关闭，不丢失页面状态 |
| 文案 / 无障碍 | 项目为动词或选项名，读出选中/禁用；触发按钮说明“更多选项” |
| 响应式 | 空间不足调整弹出方向；移动端复杂多选考虑 Sheet |
| 双主题映射 | 菜单表面与动画可变，项目顺序/状态一致 |
| Compose 入口 | 当前 `AppDropdownMenu`、`AppDropdownMenuItem` |
| 当前差距 / 验收 | 领域 Popup 仍存在；检查屏幕边缘、键盘/遥控焦点和长文案 |

### C405/C406 共享错误与空状态

| 字段 | `AppErrorState` | `AppEmptyState` |
|---|---|---|
| 用途 | 整页或局部失败 | 数据集合确实为空 |
| 禁用场景 | 未登录/权限/纯空不要伪装错误 | 首次加载未完成时不能提前显示空 |
| 结构 | 图标、标题、原因、重试/返回 | 图标、标题、解释、可选开始动作 |
| 尺寸/Token | 状态区稳定，不遮可用内容 | 与列表内容区域对齐 |
| 状态/交互 | retrying、persistent、partial | empty、filtered-empty |
| 文案 | 发生什么 + 下一步 | 没有什么 + 可做什么 |
| 无障碍 | 错误播报一次，恢复动作清楚 | 不持续播报；动作名称完整 |
| 响应式 | 局部失败留在所属区块 | 不用巨型插画占满宽屏 |
| 双主题 | 图标/表面可变 | 图标/表面可变 |
| 目标入口 | `AppErrorState` | `AppEmptyState` |
| 当前入口 | `design-system/.../AppContentStateComponents.kt` | `design-system/.../AppContentStateComponents.kt` |
| 差距/验收 | P005 已使用 PAGE/INLINE；其他页面仍待迁移 | P005 已使用 PAGE；其他页面空状态仍待迁移 |

## Compose 短示例

```kotlin
AppAlertDialog(
    onDismissRequest = onCancel,
    title = { AppText("删除下载任务？") },
    text = { AppText("已下载的本地文件也会移除。") },
    confirmButton = { AppDialogAction("删除", onDelete) },
    dismissButton = { AppDialogAction("取消", onCancel) }
)
```

## 代码映射

- `AppDialogComponents.kt`
- `AppSheetComponents.kt`
- `AppPrimitiveComponents.kt` 中 Snackbar/Menu
- 当前业务 Error/Empty 状态分布于各 feature

## 当前差距

对话框、Sheet、Snackbar 已有公共入口。共享错误与空状态组件已经建立，P005 综合搜索完成首轮试点；Toast、局部提示和 Dialog 的选择边界仍需逐页审计。

## 验收方法

检查打开/关闭/提交失败/返回/点外/键盘/旋转；焦点进入弹层并在关闭后恢复；长中文文案和 1.3 倍字体不截断按钮；Snackbar 不遮底栏或迷你播放器。
