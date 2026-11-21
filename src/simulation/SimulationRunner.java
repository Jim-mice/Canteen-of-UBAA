package simulation;

import order.Order;
import order.OrderRepository;
import order.OrderStatus;
import order.WorkloadCalculator;
import resource.ResourceAllocation;
import resource.ResourceManager;
import scheduling.SchedulerService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Runs production on the same thread as the scheduler and the manual clock. */
public final class SimulationRunner {
    private static final BigDecimal NANOS_PER_MINUTE = BigDecimal.valueOf(60_000_000_000L);
    private static final BigDecimal NANOS_PER_SECOND = BigDecimal.valueOf(1_000_000_000L);

    private final ManualSimulationClock clock;
    private final OrderRepository orderRepository;
    private final WorkloadCalculator workloadCalculator;
    private final ResourceManager resourceManager;
    private final Map<String, Production> activeOrders = new LinkedHashMap<>();
    private boolean reschedulePending;

    public SimulationRunner(ManualSimulationClock clock, OrderRepository orderRepository,
                            WorkloadCalculator workloadCalculator, ResourceManager resourceManager) {
        this.clock = Objects.requireNonNull(clock);
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.workloadCalculator = Objects.requireNonNull(workloadCalculator);
        this.resourceManager = Objects.requireNonNull(resourceManager);
    }

    /** Accepts a reserved allocation from the scheduler. Repeated handoff keeps the original finish time. */
    public void start(Order allocatedOrder, ResourceAllocation allocation) {
        String orderId = allocatedOrder.id();
        if (allocatedOrder.status() != OrderStatus.ALLOCATED || !orderId.equals(allocation.orderId())) {
            throw new IllegalArgumentException("An allocated order must match its resource allocation");
        }
        if (activeOrders.containsKey(orderId)) {
            return;
        }

        Order current = findOrder(orderId);
        if (current.status() == OrderStatus.COMPLETED) {
            return;
        }
        if (current.status() == OrderStatus.CANCELLED) {
            resourceManager.release(allocation);
            reschedulePending = true;
            return;
        }

        BigDecimal minutes = workloadCalculator.estimatedMinutes(current.amount(), allocation.chefs().size());
        Instant finishTime = clock.now().plus(productionDuration(minutes));
        if (current.status() != OrderStatus.IN_PROGRESS) {
            orderRepository.save(current.transitionTo(OrderStatus.IN_PROGRESS));
        }
        activeOrders.put(orderId, new Production(allocation, finishTime));
    }

    /**
     * Advances in completion order. All orders due at the same time release their resources
     * before the scheduler retries the waiting queue. Use this method to drive the clock
     * during production.
     */
    public void advance(Duration duration, SchedulerService scheduler) {
        Objects.requireNonNull(scheduler, "scheduler");
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Simulation time cannot move backwards");
        }
        Instant targetTime = clock.now().plus(duration);

        completeDueOrders(scheduler);
        while (true) {
            Instant nextFinish = activeOrders.values().stream()
                    .map(Production::finishTime)
                    .min(Instant::compareTo)
                    .orElse(targetTime);
            if (nextFinish.isAfter(targetTime)) {
                nextFinish = targetTime;
            }
            clock.advance(Duration.between(clock.now(), nextFinish));
            completeDueOrders(scheduler);
            if (nextFinish.equals(targetTime)) {
                return;
            }
        }
    }

    private void completeDueOrders(SchedulerService scheduler) {
        var iterator = activeOrders.values().iterator();
        while (iterator.hasNext()) {
            Production production = iterator.next();
            if (production.finishTime().isAfter(clock.now())) {
                continue;
            }

            Order current = findOrder(production.allocation().orderId());
            if (current.status() != OrderStatus.CANCELLED && current.status() != OrderStatus.COMPLETED) {
                orderRepository.save(current.transitionTo(OrderStatus.COMPLETED));
            }
            resourceManager.release(production.allocation());
            iterator.remove();
            reschedulePending = true;
        }

        if (reschedulePending) {
            scheduler.scheduleAvailableOrders();
            reschedulePending = false;
        }
    }

    private Order findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order is missing from the repository: " + orderId));
    }

    private static Duration productionDuration(BigDecimal minutes) {
        if (minutes.signum() <= 0) {
            throw new IllegalArgumentException("Production duration must be positive");
        }

        BigDecimal nanos = minutes.multiply(NANOS_PER_MINUTE).setScale(0, RoundingMode.CEILING);
        BigDecimal[] secondsAndNanos = nanos.divideAndRemainder(NANOS_PER_SECOND);
        return Duration.ofSeconds(secondsAndNanos[0].longValueExact(), secondsAndNanos[1].longValueExact());
    }

    private record Production(ResourceAllocation allocation, Instant finishTime) { }
}
