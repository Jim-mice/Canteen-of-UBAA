# 开发指南

## 环境要求

- 安装 JDK 17 或更高版本；Java 语言/API 兼容基线为 17，支持 JDK 25。
- 将 JDK 的 `bin` 目录加入 PATH，确保 `java`、`javac` 可用。
- 不需要 Maven、Gradle、Python、测试框架或第三方库。

## IDE 导入

在 VS Code 或 IntelliJ IDEA 中直接打开仓库根目录。项目没有 Maven/Gradle 项目模型；如 IDE 要求指定源目录，将 `src/` 标记为源码目录、`test/` 标记为测试源码目录，并使用 JDK 17 语言级别。包目录位于 `src/` 下一级，测试程序使用默认包。

## 编译、运行、测试

从项目根目录使用 Windows PowerShell、CMD 或双击脚本：

```powershell
.\build.bat
.\run.bat
.\test.bat
```

`build.bat` 递归编译 `src/` 并清理重建 `build/`；`run.bat` 成功编译后执行 `CanteenApplication`；`test.bat` 先编译正式源码，再编译测试类并运行 `CanteenConfigurationTest.main()`，该入口会调用订单测试。脚本返回非零退出码表示失败。

## 新增类与协作

把类直接放入对应业务模块目录，并声明匹配的包名，例如 `src/order/NewOrderPolicy.java` 声明 `package order;`。测试类放 `test/`；新增测试后，在 `test/CanteenConfigurationTest.java` 的 `main()` 中调用其 `run()`。接口变更同步维护 `docs/INTERFACES.md`。

使用独立 Git 分支完成任务，提交前运行 `test.bat` 并检查 `git status`。不要提交 `build/`、`.class` 文件、IDE 缓存、个人配置或凭证。通过 Pull Request 协作，不要改写共享分支历史。
