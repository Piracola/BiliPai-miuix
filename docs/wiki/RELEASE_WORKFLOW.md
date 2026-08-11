# 发布流程（维护版）

本页定义发布动作；版本语法见 [版本规范](VERSIONING.md)，日志文案见 [更新日志撰写规范](CHANGELOG_GUIDE.md)。

## 目标

统一版本发布动作，避免出现“代码已发版但 README/Wiki 未同步”的情况。

## 官方社群

| 用途 | 链接 |
| --- | --- |
| 频道（公告 / 发布） | https://t.me/bilipai666 |
| 交流群 | https://t.me/bilipai888/1 |

发布时建议在频道或群组消息中附上：`versionName`、`versionCode`、交付 APK 文件名、短 commit、SHA-256。

## 标准步骤

1. 更新版本号  
   - 文件：`app/build.gradle.kts`
   - 规则：`versionCode + 1`；`versionName` 为 `MAJOR.MINOR.PATCH[-PRERELEASE]`（见 [版本规范](VERSIONING.md)）
   - 规则：大升级升 MAJOR，加功能升 MINOR，修 bug 升 PATCH；`versionCode` 每次 +1
   - 标签：稳定版与预发布标签均为 `v<versionName>`；公告必须明确是否为预发布
   - 注意：版本变更必须补齐 `CHANGELOG.md`，再运行 `scripts/verify_docs.ps1`

2. 更新发布日志  
   - 文件：`CHANGELOG.md`
   - 格式：自 v0.2.0 起使用 **标签行**（`[添加]` / `[修复]` / `[更改]` / `[适配]` / `[移除]`），段末 `vX.Y.Z(<short_sha>)`；详见 [更新日志撰写规范](CHANGELOG_GUIDE.md)
   - 要求：以 GitHub 上一个实际发布标签为比较基准，`git log <prev>..HEAD` 归纳用户可感知结果；**正文不写竞品名**
   - 频道发布：直接复制 `更新日志(ChangeLog)` 至版本行（含），再附 versionCode / SHA-256

3. 同步 README  
   - 文件：`README.md`、`README_EN.md`
   - 要求：只同步产品能力、文档入口和路线图摘要；不要手写构建号、APK 文件名或逐条 Changelog

4. 同步路线图
   - 文件：`docs/wiki/ROADMAP.md`
   - 要求：同步当前优先级、完成条件与已经失效的计划项；版本和依赖继续由构建配置负责

5. 同步 Wiki
   - 文件：`docs/wiki/FEATURE_MATRIX.md`、`docs/wiki/ARCHITECTURE.md`、`docs/wiki/QA.md`、`docs/wiki/RELEASE_WORKFLOW.md`
   - 要求：更新结构说明、回归清单、发布流程与能力状态

6. 同步 AI 入口
   - 文件：`llms.txt`、`docs/wiki/AI.md`
   - 要求：只在入口路由或事实优先级变化时更新，不复制版本和发布状态

7. 最低验证
   - 至少执行与本次改动相关的单测或构建命令
   - 至少执行一次 QA 基础检查清单（见 `docs/wiki/QA.md`）
   - 推荐：`./gradlew :app:testDebugUnitTest`
   - 需要生成可安装测试包时使用 `./gradlew :app:assembleDev`；不要把 `debug` 或 `smooth` 产物作为测试交付包
   - Dev 交付包必须出现在 `app/build/outputs/bilipai/dev/`，Release 交付包必须出现在 `app/build/outputs/bilipai/release/`
   - 运行 `powershell -ExecutionPolicy Bypass -File scripts/verify_docs.ps1`

8. 提交与推送
   - 建议拆分为：
     - `chore(release): bump version to x.y.z`
     - `docs(readme): sync release notes`
     - `docs(wiki): sync docs and routing`

## 发布检查清单

- [ ] `app/build.gradle.kts` 版本号正确
- [ ] Git 标签、Changelog、构建元数据与 APK 文件名使用同一 `versionName`
- [ ] Telegram 频道/群组发布消息包含规范文件名、源码提交和 SHA-256
- [ ] `CHANGELOG.md` 新版本段存在
- [ ] 更新范围使用上一个 GitHub Release 标签，未把未发布的中间版本误作基准
- [ ] `README.md` / `README_EN.md` 已同步能力与文档入口，未复制易漂移事实
- [ ] `docs/wiki/ROADMAP.md` 已同步当前优先级和完成条件
- [ ] `docs/wiki/FEATURE_MATRIX.md` 已同步
- [ ] `docs/wiki/ARCHITECTURE.md` / `QA.md` / `RELEASE_WORKFLOW.md` 已同步
- [ ] `llms.txt` / `docs/wiki/AI.md` 的入口路由和事实优先级已核对
- [ ] `scripts/verify_docs.ps1` 通过
- [ ] 必要测试已执行并记录结果
