# A 组：用户与订单模块

## 已完成

- `user.InMemoryUserRepository`：通过用户 ID 保存、更新和查询用户；不做权限判断。
- `user.DefaultWindowAccessPolicy`：按菜品可选的 `requiredGroup` 与用户组成员关系判断访问权限；不检查用户子类或菜名。
- `order.InMemoryOrderRepository`：通过订单 ID 保存和替换不可变订单；状态查询按插入顺序返回不可变快照。
- `order.DefaultWorkloadCalculator`：用 `BigDecimal` 和 `MathContext.DECIMAL128` 计算金额除以厨师数量的分钟数。
- `order.DefaultOrderService`：校验登记用户、菜品、权限、静态资源容量；先保存 CREATED 订单，再提交给注入的调度器。

现有 `User`、`RegularUser`、`GroupUser`、`UserGroup`、`Order`、`OrderStatus`、`ResourceRequirement` 和公共接口均保留。用户组集合、菜品配置、订单与 Repository 查询结果均防止调用方直接修改。

## 创建服务

订单服务的构造参数为：

```java
DefaultOrderService(
    UserRepository userRepository,
    OrderRepository orderRepository,
    WindowAccessPolicy accessPolicy,
    SchedulerService schedulerService,
    SimulationClock clock,
    List<DishDefinition> dishes
)
```

调用方负责共享实例和系统配置，不由订单服务内部创建存储或调度器：

```java
var configuration = CanteenConfiguration.defaults();
var userRepository = new InMemoryUserRepository();
var orderRepository = new InMemoryOrderRepository();
userRepository.save(new RegularUser("U-100", "示例用户"));

// schedulerService 和 clock 由负责 C 的组提供具体实现
OrderService orderService = new DefaultOrderService(
    userRepository,
    orderRepository,
    new DefaultWindowAccessPolicy(),
    schedulerService,
    clock,
    configuration.dishes()
);
Order order = orderService.submit(
    userRepository.findById("U-100").orElseThrow(),
    DishType.BEEF_NOODLES,
    new BigDecimal("40.00"),
    new ResourceRequirement(4, false)
);
```

## 订单提交顺序

1. 拒绝 null 用户、菜品、金额、需求及非正金额/厨师数。
2. 用传入用户 ID 从 `UserRepository` 查正式记录；未登记时抛 `OrderRejectedException`。后续授权和订单关联使用查到的正式对象，不信任调用方对象附带的组声明。
3. 从注入的菜品集合查找定义；未配置菜品拒绝。
4. 调用 `WindowAccessPolicy` 验证正式用户。
5. 读取 `DishDefinition.totalChefCount()` 和 `chefsPerWindow()` 验证不可满足的静态需求。跨窗需求不能超过总厨师数；同窗需求不能超过最大单窗口人数。这里不读取空闲人数，也不分配厨师。
6. 以 UUID 创建订单，时间取自 `SimulationClock.now()`，状态为 CREATED。
7. 先调用 `OrderRepository.save`，再调用一次 `SchedulerService.enqueue`。
8. enqueue 正常返回后重新查询订单，若 C 已同步保存 WAITING/ALLOCATED 等新状态，则返回最新对象；否则返回刚保存的订单。

以默认牛肉面配置为例，同窗 3 人可提交、4 人拒绝；跨窗 4 或 10 人可提交、11 人拒绝。此校验只判断配置容量，不保证当前资源空闲。

## C 组接入与状态保存

C 应向 `DefaultOrderService` 注入系统共享的 `SchedulerService`、`SimulationClock` 和 `OrderRepository`。`enqueue` 收到的订单已经写入共享 Repository。状态改变时必须保存 `transitionTo` 返回的新 record：

```java
Order current = orderRepository.findById(orderId).orElseThrow();
Order waiting = current.transitionTo(OrderStatus.WAITING);
orderRepository.save(waiting);
```

不要只调用 `current.transitionTo(...)` 而丢弃返回值。若 enqueue 同步保存了新状态，`submit` 会重新查询并返回该状态。

如果 `enqueue` 抛出异常，A 不吞掉异常、不自动重试、不回滚先前已保存订单，也不再次保存旧 CREATED 值覆盖 C 的状态。异常原样向调用方传播；如果 C 在抛异常前已保存 WAITING 状态，该状态会保留。调用方应先查询原订单 ID，避免盲目重复下单产生新订单。

## 测试与联调边界

`test.bat` 编译 `test/` 中所有测试类，并运行 `TestSuite`。`TestSupport.RecordingScheduler` 和 `FixedSimulationClock` 仅用于测试，不是 C 的生产实现。

测试覆盖用户保存/更新/缺失查询/组集合封装、公共与专属权限/自定义组/虚假身份、工作量整除和 DECIMAL128 除法/非法值、订单状态替换与顺序查询，以及下单校验、容量边界、保存后 enqueue 一次、仿真时间注入、同步状态刷新和 enqueue 异常保留状态。

尚待联调：C 的 SchedulerService 是否同步写入状态、异常传播策略是否符合 C 的队列语义；B 菜品配置对象应使用同一个 `CanteenConfiguration` 配置实例。A 没有实现资源占用、调度策略、等待队列、仿真时间推进或制作完成逻辑。
