# Canteen of UBAA

智能食堂课程项目的 Java 17 架构骨架。使用 Maven 管理，JDK 25 可编译；不依赖 Spring、数据库或 Web 框架。

## 快速开始

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd package
java -jar target\canteen-of-ubaa-1.0.0-SNAPSHOT.jar
```

核心领域对象、服务接口与初始资源配置已经建立；业务调度、持久化、登录和仿真执行仍由后续模块实现。架构与分工见 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)、[`docs/INTERFACES.md`](docs/INTERFACES.md) 与 [`docs/TEAM_TASKS.md`](docs/TEAM_TASKS.md)。
