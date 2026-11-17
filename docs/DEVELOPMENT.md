# 开发指南

## 环境

- 语言兼容基线：JDK 17；JDK 25 可用。
- Maven 3.9.11 由 `mvnw.cmd` 自动下载到当前用户 Maven Wrapper 缓存；首次使用需网络访问 Maven Central。
- 不需要安装 Spring、数据库或其他运行时。

## IDE 导入

在 VS Code 安装 Java Extension Pack 后打开仓库目录，等待 Maven 项目导入。IntelliJ IDEA 选择 **Open** 并打开根目录或 `pom.xml`，以 Maven 项目导入。项目编码为 UTF-8。

## 编译、测试、运行

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd package
java -jar target\canteen-of-ubaa-1.0.0-SNAPSHOT.jar
```

启动入口只打印默认食堂资源数量，不会模拟点餐或调度。

## 增加代码

按职责放入 `domain`、`application`、`repository` 或 `infrastructure` 下的对应子包。领域对象不依赖应用层；应用层依赖接口；实现类建议放在 `infrastructure`，由配置/组合根装配。提交前运行 `clean test`，新增规则需要添加对应单元测试和接口文档。

## Git 协作

每项功能使用独立分支，提交信息写清模块和变更目的；提交前查看 `git status`，避免提交 `target/`、IDE 缓存、个人配置或密钥。通过 Pull Request 合并，并在 PR 描述中记录接口变化与验证命令。不要直接改写共享分支历史。
