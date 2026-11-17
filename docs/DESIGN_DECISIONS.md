# 已确定的业务规则与设计假设

1. 项目是命令行 Java 模拟，JDK 17 兼容，JDK 25 可编译；本阶段不使用 Spring、数据库或 Web UI。
2. 每个订单仅包含一种 `DishType`。不同菜品必须分别提交订单。
3. 金额单位为元，必须为正数，类型为 `BigDecimal`。理想制作分钟数为金额除以实际分配厨师数；精度和显示舍入由工作量实现明示约定。
4. 申请厨师数为正整数。超过该菜品配置总容量的订单应拒绝；资源暂时忙碌时进入等待队列。同窗要求需由单一窗口满足全部人数，否则允许跨同类窗口组合。
5. 默认资源共 8 窗口、45 厨师：牛肉面 4 窗口分配 3/3/2/2；馄饨 2 窗口各 10 人；黄焖鸡 1 窗口 10 人；湘里湘外 1 窗口 5 人。
6. 普通窗口公开。湘里湘外要求 `UserGroup.HUNAN` 成员资格。权限依据菜品定义中的可选用户组和用户组成员关系判定，不依据具体用户类或菜名判断。
7. 用户提交订单，但不选择实际厨师、窗口或调度顺序；调度策略属于系统配置。
8. 窗口是资源节点，厨师是可分配劳动力。厨师归属窗口固定；窗口只能制作其定义菜品。状态变化集中由资源管理服务控制。
9. 领域对象尽量不可变。`Order.transitionTo` 仅允许 CREATED→WAITING/ALLOCATED/CANCELLED/REJECTED、WAITING→ALLOCATED/CANCELLED/REJECTED、ALLOCATED→IN_PROGRESS/CANCELLED、IN_PROGRESS→COMPLETED/CANCELLED；终态不可再转移。
10. Repository 仅负责对象读写，不承担权限、排序或资源分配业务。当前所有 Repository 和服务接口没有持久化或运行实现。
11. `CREATED`、`WAITING`、`ALLOCATED`、`IN_PROGRESS`、`COMPLETED`、`CANCELLED`、`REJECTED` 为订单状态集合。
