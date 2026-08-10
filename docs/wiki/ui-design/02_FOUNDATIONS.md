# 02 基础令牌

> 文档编号：UI-02  
> 规范版本：1.1.0-draft<br>
> 状态：草案  
> 最后核对日期：2026-08-10<br>
> 适用提交：146819292<br>
> 维护角色：设计系统维护者  
> 相关文档：[主题规范](03_THEMES.md) · [组件目录](components/README.md)

## 初学者解释

Token（设计令牌）就是“有名字的设计数值”。写 `AppSpacingTokens.Large` 比写 `16.dp` 多了一层含义：前者说明这里是“大间距”，未来设计系统调整时也能统一变化。Token 不是为了让代码变长，而是防止几十个页面各自发明相近数值。

## 规范要求

基础数值必须先使用现有共享 Token；缺少语义时先登记差距并验证复用场景。以下间距、形状、颜色和尺寸规则共同组成 1.0 基线。

## 间距

| Token | 当前值 | 常见用途 |
|---|---:|---|
| `None` | 0dp | 明确无间距 |
| `Micro` | 2dp | 图标内部、紧凑媒体标记 |
| `ExtraSmall` | 4dp | 紧密文字与图标 |
| `Small` | 8dp | 同一组元素 |
| `Medium` | 12dp | 紧凑容器内边距 |
| `Large` | 16dp | 标准页面边距、卡片内边距 |
| `ExtraLarge` | 24dp | 区块间距 |
| `DoubleExtraLarge` | 32dp | 大区块分隔 |
| `TripleExtraLarge` | 48dp | 强分区或安全留白 |

**必须**先判断语义再选择 Token；**禁止**为了“看着顺眼”新增 10dp、14dp、18dp 等近似值。若确有媒体比例或第三方控件约束，记录理由并登记 Token 差距。

### 设置页布局基线

| 语义 | 目标规则 |
|---|---|
| Compact 页面水平边距 | 默认使用 `Large`（16dp） |
| 组间距 | 使用 `Large` 或 `ExtraLarge`，由设置页视觉策略统一决定 |
| 组内边距 | 由官方 Preference/ListItem 组件和主题 Renderer 决定，页面不手工重复叠加 |
| 行高 | 不写固定高度；保证至少 48dp 命中区并允许两行说明自然增高 |
| 内容宽度 | Expanded 使用共享最大宽度/双栏策略，不让单行文字无限拉伸 |
| Divider | MIUIX 默认依靠分组和行距；Material 3 仅在组内需要时使用弱分隔 |

截图参考只决定这些语义，不用于直接测量像素并创建新的零散 dp 值。

## 形状

| `ContainerLevel` | 语义 | 语义基准；MIUIX/Material 3 由映射解析 |
|---|---|---:|
| `Tag` | 小标签、角标 | 4dp |
| `Chip` | 筛选项、小操作 | 6dp |
| `Field` | 输入框、搜索框 | 10dp |
| `Card` | 标准内容卡 | 12dp |
| `Dialog` | 确认与警告弹窗 | 14dp |
| `Sheet` | 底部面板顶部圆角 | 20dp |
| `Floating` | 悬浮栏、FAB | 28dp |
| `Pill` | 胶囊、分段选择 | 由风格 chrome token 决定 |

**必须**使用 `AppShapes.container(level)`；带描边的容器使用 `AppShapes.borderedContainer(level)`。**禁止**在新业务组件中直接写 `RoundedCornerShape(N.dp)`，除非形状由媒体裁切、进度轨道或外部格式严格定义。

## 颜色与表面

颜色应按角色选择，而不是按十六进制选择：背景、内容表面、次级表面、主操作、错误、边框、文字各自表达含义。`AppSurfaceTokens` 负责把 Material `ColorScheme` 与双主题语义连接起来。

- **必须**使用主题角色或 `AppSurfaceTokens`，同时检查浅色、深色与 AMOLED。
- **必须**让错误色只表达错误/破坏性语义，品牌粉色不能自动代表危险。
- **应该**用层级、边框或留白区分容器，不依赖阴影一种手段。
- **禁止**把 feature 层直接读取 `MiuixTheme.colorScheme` 作为常规做法。
- **禁止**把页面里的偶然硬编码颜色升级成公共规范。

## 尺寸、图标和触摸

- 可点击区域**必须**至少为 `AppChromeSizeTokens.MinimumTouchTarget`，当前是 48dp。
- 图标视觉尺寸可以小于 48dp，但承载点击的父容器不能小于 48dp。
- 图标**必须**来自项目当前图标入口或已接入图标库；相同语义使用相同图标家族。
- 纯装饰图标的 `contentDescription` **必须**为 `null`；独立可点击图标必须提供动作名称。
- 紧凑控件 44dp 高度只代表可见外壳，仍需通过外部布局保证 48dp 触摸区域。

## Compose 短示例

```kotlin
AppCard(
    onClick = onOpen,
    shape = AppShapes.container(ContainerLevel.Card),
    modifier = Modifier.padding(AppSpacingTokens.Large)
) {
    AppText(title)
}
```

## 代码映射

| 规范 | 代码入口 |
|---|---|
| 间距 | `AppSpacingTokens.kt` 中的 `AppSpacingTokens` |
| 圆角 | `AppShapes.kt` 中的 `ContainerLevel`、`AppShapes` |
| 触摸与 chrome 尺寸 | `AppChromeSizeTokens.kt` |
| 表面角色色 | `AppSurfaceTokens.kt` |
| 动画 | `AppMotionTokens.kt` |
| 排版 | `Type.kt` 与主题 typography |

## 当前差距

代码中仍有大量 `padding(N.dp)`、局部 `RoundedCornerShape` 和直接颜色值。它们要区分为合理领域值与重复设计值，不能机械替换。共享 Token 当前也未覆盖头像尺寸、媒体比例、列表密度和内容最大宽度。

## 验收方法

1. 新组件不出现无理由的布局、圆角、动画或颜色硬编码。
2. 360dp 宽度下文本不被 48dp 点击目标挤出容器。
3. 切换 MIUIX/Material 3 与明暗主题，语义层级仍可辨认，且设置页保持各自分组语法。
4. 缺少 Token 时先在[差距台账](10_GAP_LEDGER.md)登记，再由设计系统维护者决定是否新增。
