package scheduling;

import order.Order;

import java.util.List;

/** Preserves the order in which the scheduler accepted the orders. */
public final class FifoSchedulingStrategy implements SchedulingStrategy {
    @Override
    public List<Order> orderWaitingOrders(List<Order> waitingOrders) {
        return List.copyOf(waitingOrders);
    }
}
