# 组员模块任务

| 模块 | 建议入口 | 依赖接口 | 预期交付物 |
|---|---|---|---|
| 用户与权限管理 | `domain/user`、`application/access/WindowAccessPolicy` | `UserRepository`、`WindowAccessPolicy` | 用户组维护、权限策略实现、成员关系与湖南窗口权限测试 |
| 菜品与窗口管理 | `domain/menu`、`domain/resource`、`config/CanteenConfiguration` | `WindowRepository` | 可配置菜单/窗口装载、容量一致性校验、窗口状态维护实现 |
| 订单管理 | `application/order/OrderService`、`domain/order` | `OrderRepository`、`UserRepository`、`SchedulerService`、`WorkloadCalculator` | 下单校验、容量拒绝、状态转换规则、订单服务测试 |
| 资源分配 | `application/resource/ResourceAllocator`、`ResourceManager` | `WindowRepository` | 同窗/跨窗分配、原子占用释放、厨师与窗口状态维护及边界测试 |
| 调度算法 | `application/scheduling/SchedulerService`、`SchedulingStrategy` | `OrderRepository`、`ResourceAllocator`、`WindowAccessPolicy` | FIFO 等系统策略、等待队列重试、调度结果与状态更新测试 |
| 仿真运行与统计 | `application/simulation/SimulationClock` | `OrderRepository`、`ResourceManager`、`WorkloadCalculator` | 可控仿真时钟、制作进度与完成释放；后续再定义统计接口 |
| 内存适配器与组装 | `infrastructure` | 所有 Repository / 服务端口 | 仅用于本地演示的内存实现、启动装配；不得将存储细节放进领域层 |

建议先合并接口与领域对象约定，再由模块分别实现。跨模块需要变更签名时，先同步更新 `INTERFACES.md` 与调用方测试。
