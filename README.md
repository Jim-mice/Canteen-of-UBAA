# Canteen of UBAA

智能食堂课程项目的 Java 架构骨架。源码按业务模块直接放在 `src/` 下，JDK 17 是语言兼容基线，支持使用 JDK 25 编译运行。项目不依赖 Maven、Gradle、数据库或第三方库。

## 快速开始

在 Windows PowerShell 或 CMD 中，从项目目录运行：

```powershell
.\build.bat
.\run.bat
.\test.bat
```

`build.bat` 将正式源码编译到 `build/`；`run.bat` 编译后启动架构入口；`test.bat` 编译正式源码和普通 Java 测试程序并运行全部测试。需要安装 JDK 17 或更高版本，并确保 `java`、`javac` 位于 PATH。

## 项目结构

```text
src/       主程序与按业务划分的 Java 包
test/      不依赖测试框架的 Java 测试程序
docs/      架构、接口、开发、分工与设计规则
build/     本地生成的 class 文件（已加入 .gitignore）
```

各模块与后续任务见 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)、[`docs/INTERFACES.md`](docs/INTERFACES.md) 和 [`docs/TEAM_TASKS.md`](docs/TEAM_TASKS.md)。A 组已完成用户存储、通用窗口权限策略、订单存储、工作量计算和订单提交服务。B 组的资源分配与资源管理、C 组的调度和仿真实现仍待接入；使用和联调方式见 [`docs/MODULE_A.md`](docs/MODULE_A.md)。
