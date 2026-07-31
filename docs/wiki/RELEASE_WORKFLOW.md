# 发布流程（维护版）

最后更新：2026-07-31（按当前文档链路校对）

## 目标

统一版本发布动作，避免出现“代码已发版但 README/Wiki 未同步”的情况。

## 标准步骤

1. 更新版本号  
   - 文件：`app/build.gradle.kts`
   - 规则：`versionCode + 1`，`versionName` 递增
   - 注意：若 `versionName` 已领先公开文档，发布前必须补齐 `CHANGELOG.md` 与 README/Wiki

2. 更新发布日志  
   - 文件：`CHANGELOG.md`
   - 要求：新增本版本段落，记录核心能力、修复点、验证结果

3. 同步 README  
   - 文件：`README.md`、`README_EN.md`
   - 要求：同步顶部版本、快速导航、Latest、Roadmap 摘要（尤其“已完成基线”与当前 P0）

4. 同步路线图
   - 文件：`docs/wiki/ROADMAP.md`
   - 要求：同步版本/依赖基线、当前优先级、完成条件与已经失效的计划项

5. 同步 Wiki
   - 文件：`docs/wiki/FEATURE_MATRIX.md`、`docs/wiki/ARCHITECTURE.md`、`docs/wiki/QA.md`、`docs/wiki/RELEASE_WORKFLOW.md`
   - 要求：更新结构说明、回归清单、发布流程与能力状态

6. 同步 AI 入口
   - 文件：`llms.txt`、`docs/wiki/AI.md`
   - 要求：若 README/Wiki 页头时间、风险提示或文档优先级发生变化，需同步更新

7. 最低验证
   - 至少执行与本次改动相关的单测或构建命令
   - 至少执行一次 QA 基础检查清单（见 `docs/wiki/QA.md`）
   - 推荐：`./gradlew :app:testDebugUnitTest`
   - 需要生成可安装测试包时使用 `./gradlew :app:assembleDev`；不要把 `debug` 或 `smooth` 产物作为测试交付包

8. 提交与推送
   - 建议拆分为：
     - `chore(release): bump version to x.y.z`
     - `docs(readme): sync release notes`
     - `docs(wiki): sync docs and routing`

## 发布检查清单

- [ ] `app/build.gradle.kts` 版本号正确
- [ ] `CHANGELOG.md` 新版本段存在
- [ ] `README.md` 已同步最新版本与已完成功能
- [ ] `README_EN.md` 已同步最新版本与 Latest
- [ ] `docs/wiki/ROADMAP.md` 已同步当前优先级和完成条件
- [ ] `docs/wiki/FEATURE_MATRIX.md` 已同步
- [ ] `docs/wiki/ARCHITECTURE.md` / `QA.md` / `RELEASE_WORKFLOW.md` 已同步
- [ ] `llms.txt` / `docs/wiki/AI.md` 已同步
- [ ] 必要测试已执行并记录结果
