import config.CanteenConfiguration;
import menu.DishType;
import order.Order;
import order.OrderRepository;
import order.OrderStatus;
import order.ResourceRequirement;
import resource.ChefStatus;
import resource.ResourceAllocation;
import resource.ResourceAllocator;
import resource.ResourceManager;
import resource.Window;
import resource.WindowStatus;
import scheduling.DefaultSchedulerService;
import scheduling.FifoSchedulingStrategy;
import simulation.ManualSimulationClock;
import simulation.SimulationRunner;
import user.RegularUser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Command-line demonstration of two orders sharing a kitchen window. */
public final class CanteenApplication {
    private CanteenApplication() { }

    public static void main(String[] args) {
        var configuration = CanteenConfiguration.defaults();
        var dishType = DishType.YELLOW_BRAISED_CHICKEN;
        Window window = configuration.windows().stream()
                .filter(candidate -> candidate.dishType() == dishType && candidate.status() == WindowStatus.OPEN)
                .findFirst().orElseThrow();

        // Replace these demo dependencies with the order and resource modules when wiring the full app.
        var orders = new DemoOrderRepository();
        var resources = new DemoResourceManager(window);
        ResourceAllocator allocator = (order, windows) -> resources.reserve(order.id(), windows,
                order.requirement().chefCount(), order.requirement().sameWindowRequired());
        var clock = new ManualSimulationClock(Instant.EPOCH);
        var runner = new SimulationRunner(clock, orders,
                (amount, chefCount) -> amount.divide(BigDecimal.valueOf(chefCount), MathContext.DECIMAL128),
                resources);
        var scheduler = new DefaultSchedulerService(new FifoSchedulingStrategy(), orders, allocator, resources,
                (user, dish) -> dish.requiredGroup().map(user.groups()::contains).orElse(true),
                configuration.dishes(), runner::start);

        var step = Duration.ofMinutes(2);
        int chefCount = window.chefs().size();
        var amount = BigDecimal.valueOf(step.toMinutes() * chefCount);
        var requirement = new ResourceRequirement(chefCount, true);
        var customer = new RegularUser("U-1", "演示用户");
        var orderIds = List.of("O-1", "O-2");

        System.out.printf("食堂调度演示 · 默认配置 %d 个窗口、%d 名厨师%n",
                configuration.windows().size(), configuration.totalChefCount());
        System.out.printf("两笔黄焖鸡订单各 %s 元，共用一个窗口，每单申请 %d 名厨师。%n", amount, chefCount);

        for (String orderId : orderIds) {
            var order = new Order(orderId, customer, dishType, amount, requirement, clock.now(), OrderStatus.CREATED);
            scheduler.enqueue(orders.save(order));
        }

        printStatus(clock, orders, orderIds);
        for (int i = 0; i < orderIds.size(); i++) {
            runner.advance(step, scheduler);
            printStatus(clock, orders, orderIds);
        }

        long idleChefs = window.chefs().stream()
                .filter(chef -> resources.chefStatus(chef.id()) == ChefStatus.IDLE)
                .count();
        System.out.printf("演示结束，黄焖鸡窗口有 %d/%d 名厨师空闲。%n", idleChefs, chefCount);
    }

    private static void printStatus(ManualSimulationClock clock, OrderRepository orders, List<String> orderIds) {
        long minutes = Duration.between(Instant.EPOCH, clock.now()).toMinutes();
        String statuses = orderIds.stream()
                .map(id -> id + "=" + orders.findById(id).orElseThrow().status())
                .collect(Collectors.joining("，"));
        System.out.printf("第 %d 分钟  %s%n", minutes, statuses);
    }

    private static final class DemoOrderRepository implements OrderRepository {
        private final Map<String, Order> orders = new LinkedHashMap<>();

        @Override
        public Optional<Order> findById(String id) {
            return Optional.ofNullable(orders.get(id));
        }

        @Override
        public Order save(Order order) {
            orders.put(order.id(), order);
            return order;
        }

        @Override
        public List<Order> findByStatus(OrderStatus status) {
            return orders.values().stream().filter(order -> order.status() == status).toList();
        }
    }

    /** Manages one demo window where each order uses the whole kitchen crew. */
    private static final class DemoResourceManager implements ResourceManager {
        private final Window window;
        private ResourceAllocation reserved;

        private DemoResourceManager(Window window) {
            this.window = window;
        }

        @Override
        public List<Window> availableWindows() {
            return window.status() == WindowStatus.OPEN ? List.of(window) : List.of();
        }

        @Override
        public Optional<ResourceAllocation> reserve(String orderId, List<Window> windows, int chefCount,
                                                    boolean sameWindowRequired) {
            if (reserved != null || !windows.contains(window) || chefCount != window.chefs().size()
                    || window.status() != WindowStatus.OPEN
                    || window.chefs().stream().anyMatch(chef -> chef.status() != ChefStatus.IDLE)) {
                return Optional.empty();
            }
            reserved = new ResourceAllocation(orderId, List.of(window), window.chefs());
            return Optional.of(reserved);
        }

        @Override
        public void release(ResourceAllocation allocation) {
            if (reserved != null && reserved.equals(allocation)) {
                reserved = null;
            }
        }

        @Override
        public ChefStatus chefStatus(String chefId) {
            var chef = window.chefs().stream()
                    .filter(candidate -> candidate.id().equals(chefId))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown chef: " + chefId));
            return reserved == null ? chef.status() : ChefStatus.WORKING;
        }

        @Override
        public WindowStatus windowStatus(String windowId) {
            if (!window.id().equals(windowId)) {
                throw new IllegalArgumentException("Unknown window: " + windowId);
            }
            return window.status();
        }
    }
}
