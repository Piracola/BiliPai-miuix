# 输入与选择组件

> 文档编号：UI-COMP-02  
> 规范版本：1.1.0-draft<br>
> 状态：草案  
> 最后核对日期：2026-08-10<br>
> 适用提交：146819292<br>
> 维护角色：设计系统维护者、设置维护者  
> 相关文档：[组件目录](README.md) · [排版与文案](../04_TYPOGRAPHY_CONTENT.md)

## 初学者解释

输入组件让用户提供数据，选择组件让用户在已知选项中做决定。控件外观可以随风格变化，但值的含义、校验时机、错误恢复和键盘行为必须一致。能用开关表达的二元状态，不应做成文字按钮；少量固定选项通常适合菜单、单选或分段控件。

## 规范要求

### C101 搜索输入：`AppSearchField`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 搜索内容或设置；普通表单字段不用搜索样式 |
| 结构 / 变体 | 搜索图标、输入、清空、可选提交/语音；默认、聚焦、已有值、加载、错误 |
| 尺寸 / Token | `ContainerLevel.Field`；可见高度按风格映射，所有动作 48dp 命中 |
| 交互 | 输入建议防抖；清空不等于返回；IME Search 与显式提交结果一致 |
| 文案 | placeholder 说明搜索对象，如“搜索设置”；不能代替持久标签的表单不使用 |
| 无障碍 | 提供字段标签、当前文本、清空与提交动作；建议列表顺序可读 |
| 响应式 | Compact 占主要宽度；Expanded 可与筛选同排但不无限伸长 |
| 双主题映射 | 输入壳、焦点与清空图标可变；查询、建议和提交语义一致 |
| Compose 入口 | 当前 `AppSearchField`；特定自适应搜索入口需委托公共语义 |
| 当前差距 / 验收 | 搜索页面断点多；检查输入法、清空、建议、旋转和返回后的查询 |

### C102 普通文本输入：`AppTextField`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 登录、URL、名称、验证码等表单；固定选项不要让用户手输 |
| 结构 / 变体 | 标签、值、说明、错误、前后图标；单行/多行、普通/密码/数字 |
| 尺寸 / Token | `ContainerLevel.Field`；内边距用 Token，点击和清除动作至少 48dp |
| 状态 | empty、focused、filled、invalid、disabled、read-only、submitting |
| 交互 | 选择正确 keyboardOptions/IME；校验时机可解释；提交期间防重复 |
| 文案 | 标签说明字段，错误说明修复方式；placeholder 不承载唯一标签 |
| 无障碍 | 关联标签、错误、必填与密码语义；验证码倒计时可被读取但不频繁打断 |
| 响应式 | 设置最大宽度；多字段窄屏纵向，宽屏可以合理并排 |
| 双主题映射 | 边框/容器/焦点反馈可变；数据、校验和错误一致 |
| Compose 入口 | 当前 `AppTextField`；原语场景可用 `AppOutlinedTextField` |
| 当前差距 / 验收 | 登录与插件输入样式不完全统一；检查错误、粘贴、密码显示与大字体 |

### C103 二元偏好：`AppSwitchPreference`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 设置立即开启/关闭；需要确认、导航或三种以上选项时不用开关 |
| 结构 / 变体 | 标题、可选说明、Switch、可选图标；正常、选中、禁用、保存中 |
| 尺寸 / Token | 整行至少 48dp；组内间距和圆角由 Preference 映射 |
| 交互 | 点击行与开关同一结果；若失败必须恢复值并提示，不虚假保持 |
| 文案 | 标题描述开启后的状态，如“自动播放下一集”，避免“是否自动播放” |
| 无障碍 | Switch 角色、标题、开关状态、禁用原因；避免行与开关重复聚焦 |
| 响应式 | 说明可换行，开关不被挤压；双栏中值仍与标题同行或明确关联 |
| 双主题映射 | 使用各自 Switch；值、启用条件和保存结果一致 |
| Compose 入口 | 当前 `AppSwitchPreference` |
| 当前差距 / 验收 | 旧设置行仍可能手写；检查整行点击、失败回滚、重启持久化 |

### C104 连续数值偏好：`AppSliderPreference`

| 字段 | 规范 |
|---|---|
| 用途 / 禁用场景 | 音量、强度等可连续感知的值；精确离散选项优先菜单/步进器 |
| 结构 / 变体 | 标题、当前值、Slider、范围/刻度、可选重置 |
| 尺寸 / Token | 滑轨可见但命中区至少 48dp；数值标签不改变行高 |
| 状态 / 交互 | dragging、settled、disabled；拖动可预览，持久化时机明确 |
| 文案 / 无障碍 | 单位和范围清楚；读屏可增减并读出当前值 |
| 响应式 | 窄屏可把滑轨移到下一行；宽屏不拉到难以精确控制 |
| 双主题映射 | Thumb/track 可变；范围、步长、值与禁用条件一致 |
| Compose 入口 | 当前 `AppSliderPreference` |
| 当前差距 / 验收 | 局部 Slider 的提交策略不同；检查最小/最大/步长与重进值 |

### C105/C106 设置行与分组

| 字段 | `AppPreference` | `AppPreferenceGroup` |
|---|---|---|
| 用途 | 进入详情、执行明确设置动作或展示值 | 组织同一主题的一组设置 |
| 禁用场景 | 二元值应使用 Switch；危险即时动作需确认 | 不为每一行单独套组 |
| 结构 | 标题、说明、图标、尾部值/箭头 | 标题、可选说明、行、分隔策略 |
| 状态 | normal、pressed、disabled、selected | normal、部分禁用 |
| 交互 | 整行点击；尾部控件存在时避免双重动作 | 不独立点击 |
| 文案 | 标题写结果，说明写影响 | 标题短且能描述共同主题 |
| 无障碍 | 合并行语义，读出当前值和是否可用 | 标题进入阅读顺序但不过度重复 |
| 响应式 | 说明换行，尾部值受最大宽度约束 | Expanded 仍保持合理阅读宽度 |
| 双主题 | Preference 原生表现可不同 | 分组容器和间距可不同 |
| 入口 | `AppPreference` | `AppPreferenceGroup` |
| 差距 / 验收 | 检查旧 ArrowPreference 映射 | 检查嵌套卡和过多分隔 |

设置页默认映射：

| 语义 | MIUIX | Material 3 |
|---|---|---|
| 分组 | Miuix Card，连续行，通常无 divider | Material surface/card，必要时弱 divider |
| 导航行 | BasicComponent/Preference；状态值后接轻箭头 | Material ListItem/测量安全的 Row；导航提示按语义出现 |
| Switch | Miuix SwitchPreference | Material Switch 行 |
| Slider | Miuix SliderPreference | Material Slider 行 |
| 图标 | 彩色 squircle 容器 | `onSurfaceVariant` 单色图标，默认无容器 |

- `AppPreferenceGroup` 只能表达同一用户任务的一组设置，不能包住整页。
- 页面不得同时使用大段 Spacer 和 Divider 制造同一层级；优先由分组容器与统一 row spec 建立节奏。
- 标题、trailing 当前值、箭头和 trailing 控件必须在测量后布局，长文案不得与尾部区域重叠。
- `AUTO/跟随预设` 代表表中官方默认；显式自定义属于高级覆盖，不得反向定义主题基线。

## 选择控件决策表

| 数据类型 | 推荐控件 |
|---|---|
| 开/关，立即生效 | Switch |
| 2-4 个高频互斥模式 | 分段控件或单选 |
| 多个互斥但不高频 | 菜单/单选列表 |
| 可多选集合 | Checkbox 列表 |
| 连续数值 | Slider |
| 小范围精确数字 | Stepper 或数字输入 |
| 颜色 | 颜色 swatch，不只写颜色名 |

## Compose 短示例

```kotlin
AppSwitchPreference(
    title = "自动播放下一集",
    summary = "当前视频结束后继续播放",
    checked = autoPlay,
    onCheckedChange = onAutoPlayChange
)
```

## 代码映射

- `AppPreferenceComponents.kt`
- `AdaptivePreferenceComponents.kt`
- 搜索输入的共享 chrome：`AppChromeSizeTokens`、`CompactCapsuleChromeSpec`
- 设置宽屏外壳：`SettingsTabletShell.kt`

## 当前差距

设置页已大量使用公共 Preference，但登录、插件和部分搜索输入仍有领域实现；复选、单选、分段和步进器尚未全部在组件目录形成公共目标入口。

## 验收方法

用键盘、触摸和 TalkBack 分别完成输入、清空、提交、切换与数值调整；检查错误回滚、禁用原因、重启持久化、1.3 倍字体和 360/840dp 布局。

