# 核心接口清单

Java 包名直接对应 `src/` 的一级业务目录。公共接口签名保持既有定义；新增实现类通过构造注入依赖。

| 接口 | 文件 | 方法与约定 | 负责模块 |
|---|---|---|---|
| `UserRepository` | `src/user/UserRepository.java` | `Optional<User> findById(String id)`、`User save(User user)`；只做数据访问 | 用户 |
| `WindowAccessPolicy` | `src/user/WindowAccessPolicy.java` | `boolean canAccess(User user, DishDefinition dish)`；通过菜品所需组和用户组成员关系判断 | 用户与权限 |
| `OrderService` | `src/order/OrderService.java` | `Order submit(User, DishType, BigDecimal, ResourceRequirement)`；输入校验失败可抛 `IllegalArgumentException`，业务拒绝可抛 `OrderRejectedException` | 订单 |
| `OrderRepository` | `src/order/OrderRepository.java` | `Optional<Order> findById(String)`、`Order save(Order)`、`List<Order> findByStatus(OrderStatus)`；不承担调度 | 订单 |
| `WorkloadCalculator` | `src/order/WorkloadCalculator.java` | `BigDecimal estimatedMinutes(BigDecimal amount, int chefCount)`，并提供订单重载；金额和人数必须为正数 | 订单 |
| `ResourceAllocator` | `src/resource/ResourceAllocator.java` | `Optional<ResourceAllocation> tryAllocate(Order, List<Window>)`；无完整可行分配时返回 empty，不做部分占用 | 资源 |
| `ResourceManager` | `src/resource/ResourceManager.java` | 查询可用窗口、原子预留/释放资源、读取厨师和窗口状态；实现负责说明未知 ID 与重复释放语义 | 资源 |
| `WindowRepository` | `src/resource/WindowRepository.java` | 按 ID、菜品类型查询或列出窗口；只做数据访问 | 资源 |
| `SchedulerService` | `src/scheduling/SchedulerService.java` | `enqueue(Order)`、`waitingOrders()`、`scheduleAvailableOrders()`；不处理登录或身份认证 | 调度 |
| `SchedulingStrategy` | `src/scheduling/SchedulingStrategy.java` | `List<Order> orderWaitingOrders(List<Order>)`；策略由系统选择，用户不指定 | 调度 |
| `SimulationClock` | `src/simulation/SimulationClock.java` | `Instant now()`；仿真时间推进尚未实现 | 仿真 |

## 对象调用规则

- `Order.transitionTo(OrderStatus)` 返回新的不可变订单；非法状态迁移抛 `IllegalStateException`。
- `ResourceRequirement` 在构造时拒绝非正厨师数。
- `DishDefinition`、`Window`、`ResourceAllocation` 对数组或集合做防御性复制。
- `User` 在构造时复制用户组成员关系；湖南组常量是 `UserGroup.HUNAN`。
- `Chef` 身份与窗口归属固定；厨师状态由资源管理实现负责维护。

## A 组现有实现

- `InMemoryUserRepository` 与 `InMemoryOrderRepository` 使用 ID 索引的 `LinkedHashMap`；订单状态查询按保存顺序返回不可变快照。
- `DefaultWindowAccessPolicy` 通过 `DishDefinition.requiredGroup()` 与用户的 `groups()` 做通用成员关系判断。
- `DefaultWorkloadCalculator` 使用 `MathContext.DECIMAL128` 计算正金额除以正厨师数。
- `DefaultOrderService` 构造方法注入用户/订单 Repository、访问策略、调度服务、仿真时钟和菜品定义集合。订单保存后调用 `enqueue`，再查询并返回可能已更新的状态。
- B 组的 `ResourceAllocator`/`ResourceManager` 和 C 组的 `SchedulerService`/`SimulationClock` 仍由对应组实现；A 未代做这些业务。
