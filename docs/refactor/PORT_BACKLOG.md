# Lite 分支 port backlog（版本化文档）

初始化日期：2026-08-16
同步基线：`upstream/main @ 379c968b9`（0.2.3-beta.6 之后）

## 用途

按 FULL_REFACTOR_PLAN.md §6：Tier 3 文件（Lite 页面 UI）**永不尝试合并上游**，只做行为对照；每次同步窗口把上游对相应页面的行为变更登记到本表，由对应页面的功能对照清单（阶段 4）或日常维护消化。

本表只减不增（条目消化后标记 DONE 并保留一行归档），不删除历史条目。

## 条目格式

```
- [ ] 日期 | 上游提交 | 涉及页面 | 行为变更摘要 | 状态（OPEN / PORTED / DONE）
```

## 待移植条目（OPEN）

| 日期 | 上游提交 | 页面 | 行为变更摘要 | 状态 |
|---|---|---|---|---|
| 2026-08-16 | 379c968b9（`fix(login): align Android HD identity with PiliPlus`） | login | 登录页 Android HD 身份对齐 PiliPlus | OPEN |
| 2026-08-16 | 801cd0ec9（`fix(login): retry Passport captcha challenges`） | login | Passport 验证码挑战重试 | OPEN |

> 说明：登录页属 Tier A（完整结构），其 UI 迁移按阶段 4 切片推进；上述两条为同步窗口内未消化的上游行为变更，登记待移植。

## 消化记录（DONE）

（空）

## 同步节律提醒

- 上游近 4 个月（2026-05~08）月均约 580 提交、UI 占 80.8%（见 `docs/refactor/phase0/UPSTREAM_CHURN.md`）。
- 洪峰期降频：同步窗口按滚动 90 天速度自动调整；Core（Tier 1）窗口 2–4 周，UI（Tier 3）只登记不合并。
- 每次 `sync(upstream)` 后：更新本表 + 更新 `docs/refactor/phase0/BASELINE.md` 的同步点。
