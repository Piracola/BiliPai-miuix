# 03 主题系统（MIUIX / Material 3）

> 文档编号：UI-03<br>
> 规范版本：1.1.0-draft<br>
> 状态：草案<br>
> 最后核对日期：2026-08-10<br>
> 适用提交：146819292<br>
> 维护角色：设计系统维护者<br>
> 相关文档：[设计方向](01_DIRECTION.md) · [基础令牌](02_FOUNDATIONS.md) · [设置页 UI 优化计划](12_SETTINGS_UI_OPTIMIZATION_PLAN.md)

## 初学者解释

主题不是一张配色表，而是一套“业务语义到颜色、形状、文字、图标、动效和组件”的映射。页面只说“这是设置分组”“这是开关”“这是当前值”，主题再决定 MIUIX 或 Material 3 中具体怎样显示。这样切换主题时，业务状态和信息架构不需要重写。

## 规范要求

### 主题树

- **必须**保持一个可供 Compose 使用的共同主题合同，业务页面通过语义角色与 `App*` 入口消费它。
- **必须**让 `AppUiStyle.MIUIX` 映射到 Miuix 渲染器，`AppUiStyle.MATERIAL3` 映射到 Material 3 渲染器。
- **必须**把 iOS 限制在历史 DataStore 迁移边界，不产生运行时 Renderer、页面或验收分支。
- **禁止**在 feature 层直接根据 `AppUiStyle` 复制整个页面；差异应放在设计系统渲染器或小型策略中。
- **禁止**在 feature 层直接依赖 `MiuixTheme.colorScheme`；Miuix 色彩先桥接到公共语义角色。

### 主题模式

| 模式 | What | Why | How |
|---|---|---|---|
| 浅色 | 亮背景主题 | 日间可读性 | 检查表面层级、弱文字和品牌色 |
| 深色 | 暗背景主题 | 夜间舒适与系统偏好 | 避免纯白大面积高亮，保持内容层级 |
| AMOLED | 纯黑背景为主的暗色变体 | 用户偏好及部分屏幕功耗 | 只改变背景/表面策略，不改变组件结构 |
| 动态取色 | 从系统壁纸获得角色色 | 个性化 | 品牌、错误、成功等语义不能因取色失真 |

### 双主题映射矩阵

| 设计维度 | MIUIX（默认） | Material 3（对等可选） |
|---|---|---|
| 顶部栏 | MIUIX chrome、居中标题与系统式节奏 | Material TopAppBar 语义与 Material 导航节奏 |
| 导航 | Miuix NavigationBar/Rail | Material NavigationBar/Rail |
| Preference | Miuix Preference 组件优先 | Material ListItem/Switch/Slider 语义渲染 |
| 分组 | Miuix Card、连续行、通常无分隔线 | Material surface/card、必要时轻分隔 |
| 图标 | 彩色 squircle 容器，类别色稳定 | `onSurfaceVariant` 单色图标默认无容器 |
| 圆角 | Miuix smooth rounding/风格 Token | Material shape Token |
| 动效 | Miuix duration/easing | Material duration/easing |
| 字体 | Miuix 主题映射 | Material typography 映射 |
| 功能与状态 | 完整覆盖 | 必须相同 |

### 设置页视觉策略

设置页必须使用主题专属策略，而不是在 `SettingsPageScaffold` 中写死一套局部值。

| 合同 | MIUIX | Material 3 |
|---|---|---|
| 页面背景 | grouped background | `surfaceContainer`/grouped background |
| 标题 | 居中、较充足顶部留白 | Material TopAppBar，按层级选择 small/medium |
| 分组呈现 | `CARD` | `CARD`；不能因兼容历史实现统一降级为 `FLAT` |
| 图标处理 | `FILLED` 彩色容器 | 单色、无容器；显式主题容器仅作为高级覆盖 |
| 导航行 | 标题 + 可选状态值 + 轻箭头 | 标题 + 可选状态值 + Material 导航提示 |
| 二元设置 | Miuix SwitchPreference | Material Switch 行 |
| 多选一 | 选项行/弹层，当前值只显示一次 | Menu/Dialog/单选列表，当前值只显示一次 |
| 分隔 | 主要依靠组和行距 | 组内可用弱 divider，但不与大留白同时堆叠 |

### 主题预设与高级覆盖

- “界面预设”必须是一组完整默认值，而不是只切换主题枚举。
- `AUTO/跟随预设` 必须解析为当前主题的官方设置页表现。
- 图标样式、列表条目样式、单选弹出方式等显式覆盖可以保留，但必须进入“高级外观覆盖”分组。
- 显式覆盖必须提供“恢复主题推荐”，并说明它会偏离主题默认视觉。
- 主题切换不得静默删除用户显式覆盖；界面应标记当前存在覆盖。

### 颜色角色

- `background`：页面最底层，不代表可点击。
- `surface` / `surfaceContainer*`：承载列表、卡片、面板等内容层。
- `primary`：主操作与当前选择，不等于所有图标或所有品牌内容。
- `on*`：位于对应背景上的文字/图标颜色，不应任意降低透明度。
- `error`：失败、危险或破坏性操作，不用于普通提醒。
- `outline`：边界和分隔，不能取代内容层级。

### 状态一致性

两主题中 enabled、selected、checked、loading、error 等状态**必须**具有相同数据含义。颜色、边框或动画可以不同，但不能只靠颜色表达状态；至少再提供图标、形状、文字或可访问状态说明之一。

## Compose 短示例

```kotlin
AppPreferenceGroup {
    AppSwitchPreference(
        title = "自动播放下一集",
        checked = enabled,
        onCheckedChange = onEnabledChange,
    )
}
```

业务代码声明分组和设置语义，不询问当前主题，也不决定 `MiuixCard` 或 Material `Surface`。

## 代码映射

- UI 设置枚举：`AppUiStyle`（MIUIX / MATERIAL3）
- 遗留预设：`UiPreset.kt` 中的 `UiPreset` / `AndroidNativeVariant`，仅用于迁移
- 渲染器决策：`PresetPrimitiveRenderer.kt`
- 表面与视觉策略：`AppSurfaceTokens.kt`、`AppSemanticVisualPolicy.kt`
- 设置页外壳：`SettingsPageScaffold.kt`
- Preference Renderer：`AdaptivePreferenceComponents.kt`

## 当前差距

主题模型与大部分自适应组件已经收敛，但设置页外壳仍统一提供 `AppPreferenceGroupPresentation.FLAT` 和 `AppPreferenceIconTreatment.FILLED`。Material 3 的 `AppListItemStyle.AUTO` 仍解析为自定义行；外观页又把主题预设和高级覆盖混在首屏，尚未达到本节的设置页视觉策略。

## 验收方法

1. MIUIX 在浅色、深色、AMOLED 下完整检查设置首页、分类、搜索和外观页。
2. Material 3 在手机浅/深色与一个 840dp 设置双栏流程中完整检查相同内容。
3. 切换主题后页面不重置业务状态、滚动位置和高级覆盖，不改变按钮含义。
4. 关闭模糊或动态取色后仍能区分页面背景、内容分组和浮层。
5. 两主题应分别符合映射矩阵，不能通过像素相同作为通过标准。
