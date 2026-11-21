package scheduling;

import menu.DishDefinition;
import menu.DishType;
import order.Order;
import order.OrderRepository;
import order.OrderStatus;
import resource.ResourceAllocation;
import resource.ResourceAllocator;
import resource.ResourceManager;
import user.WindowAccessPolicy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class DefaultSchedulerService implements SchedulerService {
    private final SchedulingStrategy strategy;
    private final OrderRepository orderRepository;
    private final ResourceAllocator resourceAllocator;
    private final ResourceManager resourceManager;
    private final WindowAccessPolicy accessPolicy;
    private final Map<DishType, DishDefinition> dishes;
    private final BiConsumer<Order, ResourceAllocation> onAllocated;
    private final Map<String, Order> waiting = new LinkedHashMap<>();
    private PendingHandoff pendingHandoff;

    /**
     * The allocator must return an already reserved allocation using the same resource manager.
     * The callback takes ownership of the allocation on success. It must accept retries by
     * order ID if it throws, and must not call back into this scheduler during handoff.
     */
    public DefaultSchedulerService(
            SchedulingStrategy strategy,
            OrderRepository orderRepository,
            ResourceAllocator resourceAllocator,
            ResourceManager resourceManager,
            WindowAccessPolicy accessPolicy,
            List<DishDefinition> dishes,
            BiConsumer<Order, ResourceAllocation> onAllocated) {
        this.strategy = Objects.requireNonNull(strategy);
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.resourceAllocator = Objects.requireNonNull(resourceAllocator);
        this.resourceManager = Objects.requireNonNull(resourceManager);
        this.accessPolicy = Objects.requireNonNull(accessPolicy);
        this.dishes = dishes.stream().collect(Collectors.toUnmodifiableMap(DishDefinition::type, dish -> dish));
        this.onAllocated = Objects.requireNonNull(onAllocated);
    }

    @Override
    public synchronized void enqueue(Order order) {
        Objects.requireNonNull(order);
        Order current = orderRepository.findById(order.id()).orElse(order);
        if (current.status() == OrderStatus.CREATED) {
            current = orderRepository.save(current.transitionTo(OrderStatus.WAITING));
        }
        if (current.status() == OrderStatus.WAITING) {
            waiting.putIfAbsent(current.id(), current);
            scheduleAvailableOrders();
        }
    }

    @Override
    public synchronized List<Order> waitingOrders() {
        return List.copyOf(waiting.values());
    }

    @Override
    public synchronized void scheduleAvailableOrders() {
        handOffAllocation();
        for (Order queued : strategy.orderWaitingOrders(waitingOrders())) {
            Order current = orderRepository.findById(queued.id()).orElse(queued);
            if (current.status() != OrderStatus.WAITING) {
                waiting.remove(queued.id());
                continue;
            }

            DishDefinition dish = dishes.get(current.dishType());
            if (dish == null || !accessPolicy.canAccess(current.submitter(), dish)) {
                orderRepository.save(current.transitionTo(OrderStatus.REJECTED));
                waiting.remove(current.id());
                continue;
            }

            var candidates = resourceManager.availableWindows().stream()
                    .filter(window -> window.dishType() == current.dishType())
                    .toList();
            var allocation = resourceAllocator.tryAllocate(current, candidates);
            if (allocation.isEmpty()) {
                return;
            }

            ResourceAllocation reserved = allocation.get();
            Order allocated = saveAllocatedOrder(current, reserved);
            waiting.remove(current.id());
            pendingHandoff = new PendingHandoff(allocated, reserved);
            handOffAllocation();
        }
    }

    private Order saveAllocatedOrder(Order order, ResourceAllocation reserved) {
        try {
            return orderRepository.save(order.transitionTo(OrderStatus.ALLOCATED));
        } catch (RuntimeException saveFailure) {
            try {
                resourceManager.release(reserved);
            } catch (RuntimeException releaseFailure) {
                saveFailure.addSuppressed(releaseFailure);
            }
            throw saveFailure;
        }
    }

    private void handOffAllocation() {
        if (pendingHandoff != null) {
            onAllocated.accept(pendingHandoff.order(), pendingHandoff.allocation());
            pendingHandoff = null;
        }
    }

    private record PendingHandoff(Order order, ResourceAllocation allocation) { }
}
