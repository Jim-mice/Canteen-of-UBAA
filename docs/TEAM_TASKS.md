# 小组开发任务

| 模块 | 开发入口 | 依赖接口 | 交付内容 |
|---|---|---|---|
| 用户与权限（A 已完成） | `src/user/` | `UserRepository`、`WindowAccessPolicy` | 内存用户存储、基于用户组的访问判断与测试 |
| 菜品与窗口 | `src/menu/`、`src/config/`、`src/resource/Window.java` | `WindowRepository` | 可配置菜单/窗口装载与容量校验 |
| 订单管理（A 已完成基础实现） | `src/order/` | `OrderRepository`、`UserRepository`、`SchedulerService`、`WorkloadCalculator` | 内存订单存储、下单校验、容量拒绝、工作量计算及测试；后续与 C 联调 |
| 资源分配 | `src/resource/` | `ResourceAllocator`、`ResourceManager`、`WindowRepository` | 同窗与跨窗分配、资源占用/释放及测试 |
| 调度算法 | `src/scheduling/` | `SchedulingStrategy`、`ResourceAllocator`、`OrderRepository` | FIFO 或其他系统调度策略、等待队列重试及测试 |
| 仿真运行 | `src/simulation/` | `SimulationClock`、`ResourceManager`、`OrderRepository` | 可控仿真时钟、制作进度与资源释放 |

所有类直接放入对应模块目录，不新增 `domain`、`application`、`repository` 等中间层目录。跨组签名变化需更新接口文档，并确保 `test.bat` 通过。本阶段不要求填写组员姓名。
