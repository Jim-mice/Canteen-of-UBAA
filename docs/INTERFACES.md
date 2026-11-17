# 核心接口约定

所有接口位于 `src/main/java/edu/buaa/canteen` 对应职责包。此阶段定义调用契约，不代表其业务实现已经完成。

| 接口 | 用途与主要方法 | 输入 / 输出与异常 | 负责模块 / 约定 |
|---|---|---|---|
| `OrderService` | `submit(User, DishType, BigDecimal, ResourceRequirement): Order` | 金额须大于 0、厨师数须正数；可由实现抛 `OrderRejectedException` 拒绝超容量或无权限请求 | 订单模块。负责提交校验和保存，不执行排程 |
| `SchedulerService` | `enqueue(Order): void`；`waitingOrders(): List<Order>`；`scheduleAvailableOrders(): void` | 队列快照不可由调用方修改；不接收用户身份认证参数 | 调度模块。内部通过策略与资源端口推进状态 |
| `SchedulingStrategy` | `orderWaitingOrders(List<Order>): List<Order>` | 输入为等待订单快照，返回确定顺序；实现应返回输入订单的排列 | 调度算法模块。策略由系统配置，不由用户指定 |
| `ResourceAllocator` | `tryAllocate(Order, List<Window>): Optional<ResourceAllocation>` | 找不到可行组合返回 `Optional.empty()`；非法订单/候选窗口可抛 `IllegalArgumentException` | 资源分配模块。不足时不得部分占用资源 |
| `ResourceManager` | `availableWindows()`；`reserve(orderId, windows, chefCount, sameWindowRequired)`；`release(allocation)`；查询状态 | `reserve` 原子成功返回 allocation，否则 empty；未知 ID 查询的异常策略由实现文档明确 | 资源模块。唯一管理厨师/窗口运行状态的入口；不得决定订单优先级 |
| `WindowAccessPolicy` | `canAccess(User, DishDefinition): boolean` | 无权限返回 false；不修改用户或菜品 | 权限模块。只根据 `requiredGroup` 与成员关系判断，不检查用户子类或菜名 |
| `WorkloadCalculator` | `estimatedMinutes(BigDecimal, int): BigDecimal`；订单便捷重载 | 金额正数、厨师数正数；估算值采用 `amount / chefCount`，由实现选择明确的 `MathContext` | 订单/工作量模块。金额使用 BigDecimal |
| `SimulationClock` | `now(): Instant` | 返回当前仿真时刻 | 仿真模块。本阶段不实现时间推进 |
| `UserRepository` | `findById(String): Optional<User>`；`save(User): User` | 空 ID 应由实现拒绝 | 用户持久化模块。仅数据访问 |
| `OrderRepository` | `findById`、`save`、`findByStatus` | 列表应不可变或防御性复制 | 订单持久化模块。不得排程 |
| `WindowRepository` | `findById`、`findByDishType`、`findAll` | 返回窗口快照 | 资源持久化模块。不得分配资源 |

## 领域对象调用规则

- `Order` 为不可变记录；`transitionTo(OrderStatus)` 返回新值，并拒绝非法状态迁移（抛 `IllegalStateException`）。
- `ResourceRequirement` 在构造时拒绝非正厨师数。
- `DishDefinition` 与 `Window` 对集合/数组做防御性复制，避免外部随意更改配置。
- `User` 的组成员资格在构造时复制为不可变集合；湖南用户组常量为 `UserGroup.HUNAN`。
- `Chef` 身份、归属和状态值不可变；`Window` 也携带不可变状态值。运行态更新由 `ResourceManager` 实现通过替换领域快照维护，不由调度器直接操作。

## 当前未提供的实现

除 `CanteenConfiguration`、领域对象和启动入口外，订单服务实现、权限策略实现、FIFO 策略、分配器、资源管理器、内存 Repository、状态机、执行器与仿真时钟都尚未提供。接口是组员可依赖的编译期契约，不能当作已经可运行的业务功能。
