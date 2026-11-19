package order;

import menu.DishDefinition;
import menu.DishType;
import scheduling.SchedulerService;
import simulation.SimulationClock;
import user.User;
import user.UserRepository;
import user.WindowAccessPolicy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Validates, persists, and submits orders to the injected scheduling service. */
public final class DefaultOrderService implements OrderService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final WindowAccessPolicy accessPolicy;
    private final SchedulerService schedulerService;
    private final SimulationClock clock;
    private final Map<DishType, DishDefinition> dishesByType;

    public DefaultOrderService(
            UserRepository userRepository,
            OrderRepository orderRepository,
            WindowAccessPolicy accessPolicy,
            SchedulerService schedulerService,
            SimulationClock clock,
            List<DishDefinition> dishes) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "accessPolicy");
        this.schedulerService = Objects.requireNonNull(schedulerService, "schedulerService");
        this.clock = Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(dishes, "dishes");

        Map<DishType, DishDefinition> configuredDishes = new EnumMap<>(DishType.class);
        for (DishDefinition dish : List.copyOf(dishes)) {
            if (configuredDishes.putIfAbsent(dish.type(), dish) != null) {
                throw new IllegalArgumentException("Duplicate dish definition: " + dish.type());
            }
        }
        this.dishesByType = Map.copyOf(configuredDishes);
    }

    @Override
    public Order submit(User user, DishType dishType, BigDecimal amount, ResourceRequirement requirement) {
        validateRequest(user, dishType, amount, requirement);

        User registeredUser = userRepository.findById(user.id())
                .orElseThrow(() -> new OrderRejectedException("User is not registered: " + user.id()));
        DishDefinition dish = dishesByType.get(dishType);
        if (dish == null) {
            throw new OrderRejectedException("Dish is not available: " + dishType);
        }
        if (!accessPolicy.canAccess(registeredUser, dish)) {
            throw new OrderRejectedException("User is not allowed to order: " + dish.name());
        }

        int maximumChefCount = requirement.sameWindowRequired()
                ? maximumWindowCapacity(dish)
                : dish.totalChefCount();
        if (requirement.chefCount() > maximumChefCount) {
            throw new OrderRejectedException("Requested chef count exceeds configured capacity for " + dish.name());
        }

        Instant createdAt = Objects.requireNonNull(clock.now(), "simulation clock returned null");
        Order order = new Order(UUID.randomUUID().toString(), registeredUser, dishType, amount,
                requirement, createdAt, OrderStatus.CREATED);
        Order savedOrder = Objects.requireNonNull(orderRepository.save(order), "order repository returned null");

        // If enqueue throws, propagate it and leave any state already saved by the scheduler untouched.
        schedulerService.enqueue(savedOrder);
        return orderRepository.findById(savedOrder.id()).orElse(savedOrder);
    }

    private static void validateRequest(User user, DishType dishType, BigDecimal amount,
                                        ResourceRequirement requirement) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        if (dishType == null) {
            throw new IllegalArgumentException("dishType must not be null");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (requirement == null) {
            throw new IllegalArgumentException("requirement must not be null");
        }
        if (requirement.chefCount() <= 0) {
            throw new IllegalArgumentException("chefCount must be positive");
        }
    }

    private static int maximumWindowCapacity(DishDefinition dish) {
        int maximum = 0;
        for (int capacity : dish.chefsPerWindow()) {
            maximum = Math.max(maximum, capacity);
        }
        return maximum;
    }
}
