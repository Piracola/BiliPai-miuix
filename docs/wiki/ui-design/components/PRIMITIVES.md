# 基础原语组件

> 文档编号：UI-COMP-01  
> 规范版本：1.0.0-draft  
> 状态：草案  
> 最后核对日期：2026-08-02  
> 适用提交：4443e72ff  
> 维护角色：设计系统维护者  
> 相关文档：[组件目录](README.md) · [基础令牌](../02_FOUNDATIONS.md)

## 初学者解释

原语是搭建界面的最小积木。业务页面应该从 `App*` 入口使用它们，因为这些入口负责把同一语义交给 MIUIX 或 Material 3 渲染。直接使用某个主题库的组件，会让另一个主题无法保持一致。

## 规范要求

### C001 文本：`AppText`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 显示所有普通 UI 文本；品牌字标或播放器画面内原生字幕可有独立渲染 |
| 结构 / 变体 | 文本内容 + typography + 颜色；标题、正文、辅助、标签按语义变体 |
| 尺寸 / Token | 使用主题 typography 与 sp；禁止 viewport 缩放和无理由局部字号 |
| 状态 / 交互 | 可选中复制文本要明确；普通文本不伪装成可点击 |
| 文案 / 无障碍 | 遵循中文文案规范；读屏顺序与视觉顺序一致 |
| 响应式 | 允许换行或明确省略；字体放大不覆盖相邻操作 |
| 双主题映射 | 字体细节可变，语义层级、内容和最大行策略一致 |
| Compose 入口 | 当前 `AppPrimitiveComponents.kt` 中的 `AppText` |
| 当前差距 / 验收 | 局部字号较多；检查 360dp、1.3 倍字体与三主题可读性 |

### C002 图标：`AppIcon`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 表达熟悉动作、状态或类别；不能用含糊图标替代必要文字 |
| 结构 / 变体 | 图形 + tint + 可访问描述；选中/未选中使用成对资源 |
| 尺寸 / Token | 常见视觉尺寸 16/20/24dp 由组件策略确定，点击区域另算 48dp |
| 状态 / 交互 | 图标自身通常不处理点击；交互使用 `AppIconButton` |
| 文案 / 无障碍 | 装饰为 `null`；独立传达信息时提供描述或由父语义合并 |
| 响应式 | 不随 viewport 缩放，不挤压长文本 |
| 双主题映射 | 可以使用主题匹配图标，但动作含义必须一致 |
| Compose 入口 | 当前 `AppPrimitiveComponents.kt` 中的 `AppIcon` |
| 当前差距 / 验收 | 图标家族仍混合；检查相同动作资源和可访问名称 |

### C003 语义表面：`AppSurface`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 承载背景、形状、内容色或点击；纯留白区不需要额外 Surface |
| 结构 / 变体 | 容器 + contentColor + 可选边框/点击；不嵌套装饰卡 |
| 尺寸 / Token | 颜色用 `AppSurfaceTokens`，形状用 `AppShapes`，间距由内容决定 |
| 状态 / 交互 | clickable 变体必须有 enabled 和反馈；禁用不接收事件 |
| 文案 / 无障碍 | 点击表面要暴露正确角色和名称，不重复子内容 |
| 响应式 | 填充父级前要有宽度上限或页面母版依据 |
| 双主题映射 | 表面层级与反馈可变，内容语义不变 |
| Compose 入口 | 当前 `AppPrimitiveComponents.kt` 中的 `AppSurface` |
| 当前差距 / 验收 | 部分页面直接使用具体 Surface；关闭模糊后检查边界和层级 |

### C004/C005 按钮：`AppPrimaryButton` 与 `AppButton`

| 字段 | 规范 |
|---|---|
| 用途 | `AppPrimaryButton` 用于当前页面唯一主要推进动作；`AppButton` 用于普通命令 |
| 禁用场景 | 导航页签、开关、纯图标工具、视频互动计数不用普通按钮 |
| 结构 / 变体 | 可选图标 + 标签 + 加载/禁用；危险操作必须使用明确语义变体和确认策略 |
| 尺寸 / Token | 触摸区至少 48dp；文字和图标不改变稳定高度；形状使用 `Pill`/组件映射 |
| 状态 | normal、pressed、focused、disabled、loading；loading 时防重复提交 |
| 交互 / 文案 | 点击一次触发一个动作；用“保存”“重试”等动词，不写含糊“确定” |
| 无障碍 | 角色为 Button，名称完整，加载和禁用状态可读 |
| 响应式 | 长文字可换行或扩宽，不能截掉关键动作；按钮组窄屏改纵向 |
| 双主题映射 | 底层、圆角、按压反馈可变；优先级与状态一致 |
| Compose 入口 | 当前 `AppPrimaryButton`、`AppButton` |
| 当前差距 / 验收 | 多份 `ActionButton` 待分类；逐项检查 normal/pressed/disabled/loading |

### C006 图标按钮：`AppIconButton`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 顶栏返回、收藏、菜单等熟悉工具；陌生命令应使用图标+文字 |
| 结构 / 变体 | 48dp 点击容器 + 图标 + tooltip/描述；filled 变体只用于强调状态 |
| 尺寸 / Token | 点击区至少 48dp，图标通常 20/24dp |
| 状态 / 交互 | normal、pressed、selected、disabled；长按 tooltip 不能成为唯一说明 |
| 文案 / 无障碍 | `contentDescription` 写动作，如已收藏时写“取消收藏” |
| 响应式 | 数量多时进入菜单，不把顶栏标题挤没 |
| 双主题映射 | 按压与容器形状可变，图标语义和命中范围一致 |
| Compose 入口 | 当前 `AppIconButton`；强调图标可用 `AppFilledIconButton` |
| 当前差距 / 验收 | 小图标直接 clickable 需审计；检查边缘点击与 TalkBack |

### C007 内容卡：`AppCard`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 可重复内容单元或有明确边界的对象；页面区块和卡内装饰不再套卡 |
| 结构 / 变体 | 媒体、标题、元信息、状态、可选操作；整卡点击与内部操作不能冲突 |
| 尺寸 / Token | `ContainerLevel.Card`、`ContentCardSurfacePolicy`、稳定媒体比例 |
| 状态 / 交互 | placeholder、normal、pressed、selected、disabled、partial/error |
| 文案 / 无障碍 | 合理合并卡片语义，读出标题和必要状态，不逐个读装饰图标 |
| 响应式 | 列数变化不拉伸封面；标题最长两行或由具体卡规范决定 |
| 双主题映射 | 表面/边框/圆角可变；内容顺序和点击含义一致 |
| Compose 入口 | 当前 `AppCard`；表面决策配合 `ContentCardSurfacePolicy` |
| 当前差距 / 验收 | 多份 `GlassCard` 待收口；检查滚动、加载替换和整卡返回 |

### C008/C009 分隔与加载

| 字段 | `AppHorizontalDivider` | `AdaptiveLoadingIndicator` |
|---|---|---|
| 用途 | 同一表面内分组 | 不确定时长加载 |
| 禁用场景 | 用留白已可分组时 | 有精确进度时、可用旧内容刷新时遮整页 |
| 尺寸 / Token | 主题 outline 与语义 inset | 稳定容器尺寸，不使布局跳动 |
| 状态 / 交互 | 无交互 | 加载、取消（若支持）、结束 |
| 无障碍 | 通常不单独聚焦 | 暴露“正在加载”且避免重复播报 |
| 双主题 | 线条可有细微差异 | MIUIX/Material 3 指示器可不同 |
| 入口 | `AppHorizontalDivider` | `AdaptiveLoadingIndicator` |
| 差距 / 验收 | 检查是否过度分隔 | 检查首次、刷新、分页三种位置 |

## Compose 短示例

```kotlin
AppButton(onClick = onRetry) {
    AppIcon(rememberAppRefreshIcon(), contentDescription = null)
    AppText("重试")
}
```

父按钮提供完整语义，内部装饰图标无需重复描述。

## 代码映射

- `AppPrimitiveComponents.kt`
- `AppPrimaryButton.kt`
- `AdaptiveLoadingIndicator.kt`
- `ContentCardSurfacePolicy.kt`

## 当前差距

原语入口已较完整，但调用层仍绕过公共入口或使用局部动画、形状和点击区。已知重复项见[差距台账](../10_GAP_LEDGER.md)。

## 验收方法

为每个原语检查双主题、明暗模式、enabled/disabled/pressed/loading、360dp 长文案、1.3 倍字体、48dp 命中区和读屏名称。

