# BiliPai Wiki

本索引只负责导航。当前构建与依赖以构建配置为准，发布记录以 `CHANGELOG.md` 为准。

## 目录

- [功能矩阵](FEATURE_MATRIX.md)
- [当前路线图](ROADMAP.md)
- [架构说明](ARCHITECTURE.md)
- [前端架构与主题精简优化计划](FRONTEND_ARCHITECTURE_THEME_SIMPLIFICATION_PLAN.md)
- [AI 导航指南](AI.md)
- [Miuix 对齐记录](MIUIX_ALIGNMENT.md)
- [UI 设计规范](ui-design/README.md)
- [液态玻璃复用与首页底栏同源规范](LIQUID_GLASS_REUSE_PARITY.md)
- [发布流程](RELEASE_WORKFLOW.md)
- [版本规范](VERSIONING.md)
- [更新日志撰写规范](CHANGELOG_GUIDE.md)
- [QA 测试手册](QA.md)
- [用户常见问题](FAQ.md)
- [插件开发指南（JSON）](../PLUGIN_DEVELOPMENT.md)
- [插件开发指南（原生）](../NATIVE_PLUGIN_DEVELOPMENT.md)

## 维护约定

每次 Release 至少核对以下内容：

1. `CHANGELOG.md` 新版本段落
2. `README.md` / `README_EN.md` 的能力概览、入口和路线图摘要
3. `docs/wiki/ROADMAP.md` 的当前优先级、完成条件与失效计划项
4. 受影响的功能矩阵、架构、QA 与发布流程
5. `scripts/verify_docs.ps1` 通过
6. 仅在入口路由或事实优先级变化时更新 `llms.txt` 与 `docs/wiki/AI.md`

## 快速入口

- 设计与 Compose 组件规则：[UI 设计规范](ui-design/README.md)

- Android 主代码：`app/src/main/java/com/android/purebilibili`
- 测试代码：`app/src/test/java/com/android/purebilibili`
- 版本配置：`app/build.gradle.kts`
- 发布日志：`CHANGELOG.md`
- 当前路线图：`docs/wiki/ROADMAP.md`
- AI 入口：`llms.txt`
- 兼容别名：`AI.txt` / `llm.txt`
- 文档校验：`scripts/verify_docs.ps1`
