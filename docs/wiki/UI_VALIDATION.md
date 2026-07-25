# UI 验证环境

最后更新：2026-07-25

这套环境用于在正式设计稿和页面迁移之前建立稳定的视觉反馈。它不会进入 Release 版本，也不需要登录或联网。

本项目 Gradle/Kotlin toolchain 要求 JDK 21。运行编译、安装或 UI 测试前，先确认：

```powershell
java -version
```

如果系统默认仍是 JDK 17，需要在 Android Studio 的 Gradle JDK 或当前终端 `JAVA_HOME` 中选择 JDK 21。

## 能解决什么

| 工具 | 用途 | 局限 |
| --- | --- | --- |
| Compose Preview | 在 Android Studio 里立即查看手机/平板、浅色/深色 | 不验证真实系统栏、播放器和复杂动画 |
| Debug UI 验证台 | 在 ARM64 模拟器或真机上集中查看主题、排版和基础组件 | 需要安装 Debug 版本 |
| ADB 截图脚本 | 保存可直接对比的真实设备 PNG | 当前不做自动像素判定 |
| Compose UI Test | 验证显示、点击、语义和尺寸 | 不能独立判断视觉是否合理 |

## Android Studio Preview

打开：

`app/src/debug/java/com/android/purebilibili/debug/UiValidationActivity.kt`

文件底部提供四组 Preview：

- Phone Light：393 x 852 dp
- Phone Dark：393 x 852 dp
- Tablet Light：1280 x 800 dp
- Tablet Dark：1280 x 800 dp

验证台固定使用 `UiPreset.MD3 + AndroidNativeVariant.MIUIX`，并关闭动态取色，保证不同电脑看到的基线一致。

如果 Android Studio 不加载交互式预览，可在本地 Gradle 参数中启用已有 UI tooling runtime：

```properties
bili.debug.uiTooling=true
```

不要为了 Preview 把 ViewModel、网络请求或 DataStore 传进纯 UI 内容。新页面优先拆成：

```text
Route/Screen -> 收集状态与处理平台副作用
Content      -> 只接收 UiState 和事件 lambda
Preview      -> 使用固定假数据调用 Content
```

## 真机验证台

Debug Manifest 注册了独立的 `UiValidationActivity`。安装 Debug 版本后可直接启动：

当前应用所有变体只打包 `arm64-v8a`。推荐使用 ARM64 真机；模拟器也必须使用 ARM64 系统镜像。常见 Windows `x86_64` 模拟器无法安装这个 Debug 包，会报告 `INSTALL_FAILED_NO_MATCHING_ABIS`。

```powershell
adb shell am start -n com.android.purebilibili.debug/com.android.purebilibili.debug.UiValidationActivity
```

验证台包含：

- 当前设备可用区域的 dp 尺寸
- 浅色/深色切换
- Miuix 桥接后的语义颜色
- 标题、正文和辅助文字
- 主要/次要操作与 48dp 最小触摸区域
- 实际项目使用的列表、箭头项和开关项

## 保存设备截图

先确认设备并安装 Debug 版本：

```powershell
adb devices
.\gradlew.bat :app:installDebug
```

分别保存浅色和深色截图：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/ui_capture.ps1 -Theme light
powershell -ExecutionPolicy Bypass -File scripts/ui_capture.ps1 -Theme dark
```

多设备连接时指定序列号：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/ui_capture.ps1 -Theme light -Serial emulator-5554
```

图片保存在 `build/reports/ui-validation/`，默认不会提交到 Git。设计评审时应记录设备、主题和界面状态，并同时保留修改前、修改后截图。

脚本会先重启 Debug 应用进程，确保每次传入的浅色/深色参数生效；随后等待验证台成为前台 Activity，并检查拉回文件的 PNG 签名。如果验证台启动崩溃、被其他页面覆盖或截图文件损坏，命令会直接失败，不会把错误画面报告为成功基线。

## Compose UI Test

仓库提供验证台的基础仪器测试，覆盖页面渲染和浅色/深色控件切换。连接 ARM64 设备后运行：

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.android.purebilibili.debug.UiValidationActivityTest
```

该命令会构建并安装 Debug APK 与测试 APK。它验证交互和语义，不替代人工截图检查；其他页面应按改动范围选择对应的 `*UiRegressionTest`。

## 每次 UI 改动的验证顺序

1. 更新或新增对应 Content Preview。
2. 检查手机/平板、浅色/深色和长文字。
3. 运行验证台测试或改动页面对应的 Compose UI Test，确认点击、语义和尺寸没有退化。
4. 在真机验证台或实际页面保存修改前后截图。
5. 播放器、手势、键盘、横竖屏和动画仍需实际设备操作或录屏。
6. 视觉结果确认后再提交代码；截图是否成为长期 Golden 基线应单独评审。

## 后续升级

当前阶段没有新增截图测试依赖。设计体系稳定后，再选择 AndroidX Compose Screenshot Testing 或 Roborazzi 建立自动 Golden 对比，避免在组件和页面仍快速变化时维护大量失效图片。
