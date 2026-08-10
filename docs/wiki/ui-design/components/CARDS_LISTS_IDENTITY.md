# 卡片、列表与身份组件

> 文档编号：UI-COMP-04  
> 规范版本：1.0.0-draft  
> 状态：草案  
> 最后核对日期：2026-08-02  
> 适用提交：4443e72ff  
> 维护角色：设计系统维护者  
> 相关文档：[组件目录](README.md) · [基础原语](PRIMITIVES.md)

## 初学者解释

卡片强调“这是一个可独立理解的内容对象”，列表强调“这些对象属于同一集合”，身份组件帮助识别人、等级或角色。不要用卡片装饰每个页面区块，也不要因为两个标签颜色相似就把不同身份混成一种。

## 规范要求

### C301 标准列表项：`AppListItem`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 紧凑展示同类对象或设置结果；富媒体推荐卡不用标准行硬塞 |
| 结构 / 变体 | leading、标题、说明、trailing、可选 divider；单行/双行/多行 |
| 尺寸 / Token | 整行至少 48dp；内边距用 spacing Token；leading/trailing 稳定尺寸 |
| 状态 | placeholder、normal、pressed、selected、disabled、partial/error |
| 交互 | 整行一个主动作；尾部独立动作需扩大命中区并防事件冲突 |
| 文案 | 标题优先，说明最多承担一个次要信息层；时间/状态位置稳定 |
| 无障碍 | 合并标题、说明、状态；尾部独立动作单独命名 |
| 响应式 | 长标题换行/省略，trailing 设最大宽度；宽屏列表保持可扫读 |
| 双主题映射 | 行高、分隔和按压可变；字段顺序与动作一致 |
| Compose 入口 | 当前 `AppListItem` |
| 当前差距 / 验收 | 业务列表仍有自建 Row；检查稳定 key、批量选择与大字体 |

### C302 用户等级：`UserLevelBadge`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 显示 Bilibili 用户等级；UP、直播房管、会员等身份另用明确组件 |
| 结构 / 变体 | 等级文本/图形 + 等级色 + 可访问说明；0-6 与未知值 |
| 尺寸 / Token | `ContainerLevel.Tag`；不挤压用户名；触摸不是必要交互 |
| 状态 / 交互 | 静态；未知值使用中性降级，不伪造最高等级 |
| 文案 / 无障碍 | 读作“用户等级 5”，不能只读“LV5”或只用颜色 |
| 响应式 | 空间不足优先保留用户名，badge 可按页面规则移到下一行 |
| 双主题映射 | 圆角/字体可微调；等级和值含义一致 |
| Compose 入口 | 当前 `core/ui/components/UserLevelBadge.kt` 中 `UserLevelBadge` |
| 当前差距 / 验收 | 直播内有私有同名实现、另有 `LevelTag`；检查值域、长名与明暗主题 |

### C303 内容卡面策略：`ContentCardSurfacePolicy`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 给普通消息、搜索、动态等内容卡选择表面/边框/圆角；媒体播放器壳不用 |
| 结构 / 变体 | 语义 surface spec + `AppCard`/`AppSurface`；普通、选中、禁用 |
| 尺寸 / Token | `ContainerLevel.Card`；Miuix 当前使用 tonal container、0.8dp/0.22 alpha 边框、平面 elevation 策略 |
| 状态 / 交互 | 卡面本身不决定业务点击；按压由容器入口统一 |
| 文案 / 无障碍 | 不新增无意义分组；嵌套内容按卡语义合并 |
| 响应式 | 网格列数变化时表面和媒体比例稳定 |
| 双主题映射 | MIUIX tonal/描边；Material 3 使用其 surface/shape 映射 |
| Compose 入口 | 当前策略符号 `ContentCardSurfacePolicy` / `rememberContentCardSurfaceSpec` |
| 当前差距 / 验收 | 多份 `GlassCard` 待迁移；关闭模糊、双主题和长列表滚动检查 |

### 内容卡结构

```text
┌──────────────────────────┐
│ 媒体（固定比例，可选）   │
├──────────────────────────┤
│ 标题                     │
│ 作者 / 时间 / 统计       │
│ 状态或少量次要操作       │
└──────────────────────────┘
```

- 卡片媒体必须使用真实内容封面或明确占位，不使用仅氛围性的模糊图替代可检查内容。
- 整卡点击与内部收藏/菜单等动作必须拥有不重叠的命中区。
- 卡片内禁止再放纯装饰“卡”；转发、合集等真实嵌套对象可用较轻表面并说明层级。
- 加载占位与真实卡片应使用相同边界和媒体比例，避免布局跳动。

## Compose 短示例

```kotlin
val surface = rememberContentCardSurfaceSpec()
AppCard(
    shape = AppShapes.borderedContainer(surface.cornerLevel),
    onClick = onOpen
) { VideoCardContent(item) }
```

## 代码映射

- 标准列表：`AppPrimitiveComponents.kt` 中 `AppListItem`
- 内容卡策略：`ContentCardSurfacePolicy.kt`
- 用户等级：`UserLevelBadge.kt`
- 首页媒体卡：`feature/home/components/cards/`

## 当前差距

消息、搜索、动态已采用共享卡面策略的部分能力，但 `GlassCard`、等级标签和各类列表行仍未完全收口。卡片的媒体比例与最大标题行数仍按领域分散。

## 验收方法

检查列表刷新、分页、选择、删除后的稳定性；双主题和关闭模糊下层级清楚；TalkBack 不逐项重复装饰信息；长标题和大字体不覆盖 trailing 操作。

