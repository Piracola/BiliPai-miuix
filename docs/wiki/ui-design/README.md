# BiliPai UI 设计规范

> 文档编号：UI-INDEX  
> 规范版本：1.1.0-draft<br>
> 状态：草案<br>
> 最后核对日期：2026-08-10<br>
> 适用提交：146819292<br>
> 维护角色：设计系统维护者  
> 相关文档：[Miuix 对齐记录](../MIUIX_ALIGNMENT.md) · [架构说明](../ARCHITECTURE.md) · [QA 手册](../QA.md)

`适用提交`记录本规范最后人工复核的上下文，不是运行时事实来源。页面集合、主题枚举和依赖版本分别由源码和构建配置校验。

## 初学者解释

这套手册是 BiliPai 前端 UI 的正式设计合同。它同时回答三类问题：界面元素**是什么**（What）、为什么要这样设计（Why）、设计与 Compose 代码应当怎样落地（How）。它不修改当前 UI，也不把当前代码中的每个写法都当成正确标准。

## 初学者怎样阅读

先读[术语表](00_GLOSSARY.md)，再读[设计方向](01_DIRECTION.md)和[基础令牌](02_FOUNDATIONS.md)。开发一个页面时，按下面的顺序查阅：

```mermaid
flowchart LR
    A["找到页面 Key"] --> B["查页面目录与领域档案"]
    B --> C["确认页面母版"]
    C --> D["选择组件规范"]
    D --> E["只使用基础 Token"]
    E --> F["按验收手册检查"]
    F --> G["发现差异则登记任务卡"]
```

- **当前实现**：源码现在已经存在的能力，可以通过文件或符号验证。
- **目标规范**：后续 UI 开发必须逐步达到的状态，不代表现在已经全部完成。
- **差距**：当前实现与目标规范之间的距离，集中登记在[差距台账](10_GAP_LEDGER.md)。
- **验收**：用自动检查或人工操作判断规则是否满足，见[验收手册](09_ACCEPTANCE.md)。

## 规范要求与等级

| 等级 | 含义 | 初学者判断方法 |
|---|---|---|
| 必须 | 不满足就不应合并 | 涉及功能一致性、可访问性、状态或公共 API |
| 应该 | 默认遵守，例外要说明原因 | 通常是体验和维护质量要求 |
| 可以 | 按场景选用 | 不影响功能含义的增强 |
| 禁止 | 已知会造成错误或分裂 | 不得新增同义入口、隐形交互或无依据数值 |

## 文档目录

### 基础与方向

| 文档 | 解决的问题 |
|---|---|
| [00 术语表](00_GLOSSARY.md) | Compose、Token、状态、母版等词是什么意思 |
| [01 设计方向](01_DIRECTION.md) | 为什么以 MIUIX 为默认，以及双主题的共同边界 |
| [02 基础令牌](02_FOUNDATIONS.md) | 颜色、间距、形状、尺寸和图标怎样统一 |
| [03 双主题](03_THEMES.md) | MIUIX、Material 3 如何共享语义并保留各自视觉语法 |
| [04 排版与文案](04_TYPOGRAPHY_CONTENT.md) | 字号、层级、截断和中文文案怎样写 |
| [05 布局与自适应](05_LAYOUT_ADAPTIVE.md) | 手机、平板、宽屏怎样共用信息结构 |
| [06 动效与效果](06_MOTION_EFFECTS.md) | 动画、模糊、玻璃和降级怎样处理 |
| [07 无障碍](07_ACCESSIBILITY.md) | 触摸、语义、对比度和读屏怎样验收 |

### 页面、验收与维护

| 文档 | 解决的问题 |
|---|---|
| [08 页面母版](08_PAGE_TEMPLATES.md) | 十类页面的稳定骨架与状态模型 |
| [09 验收手册](09_ACCEPTANCE.md) | 主题、设备、状态和关键流程怎样检查 |
| [10 差距台账](10_GAP_LEDGER.md) | 当前缺口怎样转成可执行任务 |
| [11 维护流程](11_MAINTENANCE.md) | 新页面、新组件和规则变更怎样更新文档 |
| [12 设置页 UI 优化计划](12_SETTINGS_UI_OPTIMIZATION_PLAN.md) | 双主题设置页如何分阶段落地、验证和回滚 |
| [变更日志](CHANGELOG.md) | 规范本身发生了什么变化 |

### 组件规范

- [组件总目录](components/README.md)
- [基础原语](components/PRIMITIVES.md)
- [输入与选择](components/INPUT_SELECTION.md)
- [导航与应用外壳](components/NAVIGATION_CHROME.md)
- [卡片、列表与身份](components/CARDS_LISTS_IDENTITY.md)
- [弹层与反馈](components/OVERLAYS_FEEDBACK.md)
- [媒体与播放器](components/MEDIA_PLAYER.md)

### 页面档案

- [导航 Key 唯一目录](pages/PAGE_CATALOG.md)
- [应用外壳与首页](pages/APP_SHELL_HOME.md)
- [搜索与发现](pages/SEARCH_DISCOVERY.md)
- [社区与消息](pages/COMMUNITY_MESSAGE.md)
- [个人与内容库](pages/PROFILE_LIBRARY.md)
- [视频与播放](pages/VIDEO_PLAYBACK.md)
- [直播、番剧与音频](pages/LIVE_BANGUMI_AUDIO.md)
- [设置](pages/SETTINGS.md)
- [账号、工具与 Web](pages/ACCOUNT_TOOLS_WEB.md)

## 代码映射与事实基线

| 事实 | 当前入口 |
|---|---|
| Navigation 3 的页面 Key 集合 | `BiliPaiNavKey.kt` 中的 `BiliPaiNavKey`，由结构测试与页面目录比对 |
| 两种运行时主题 | `AppUiStyle.MIUIX`、`AppUiStyle.MATERIAL3`；iOS 仅为历史迁移输入 |
| 共享设计系统 | `design-system/src/main/java/com/android/purebilibili/core/` |
| Miuix 依赖版本 | `app/build.gradle.kts`、版本目录与 `design-system/build.gradle.kts` |
| Miuix 技术接入进度 | [MIUIX_ALIGNMENT.md](../MIUIX_ALIGNMENT.md) |

代码映射只使用文件名与符号名，避免文档因行号变化立刻失效。需要定位符号时，优先使用 CodeGraph。

## 当前差距

规范仍处于草案状态。主题运行时已经收敛为 MIUIX 与 Material 3，但设置页仍统一强制扁平分组，外观页存在超长分组、重复当前值和过量常驻说明，未形成两套主题各自完整的官方设置页语法。它们是整改输入，不是允许继续复制的先例。

## 验收方法

1. 所有目录链接均可访问，不能链接被 `.gitignore` 忽略的过程文件。
2. `PAGE_CATALOG.md` 必须与源码中的 `BiliPaiNavKey` 集合完全一致。
3. 每份规范都必须同时出现初学者解释、规范要求、代码映射、当前差距和验收方法。
4. 运行 `powershell -ExecutionPolicy Bypass -File scripts/verify_docs.ps1`，再运行 `:app:testDebugUnitTest --tests '*UiDesignDocumentationStructureTest'`。
