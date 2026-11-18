package scheduling;

import order.Order;
import java.util.List;

/** Chooses which waiting order to consider next; users do not select the strategy. */
public interface SchedulingStrategy { List<Order> orderWaitingOrders(List<Order> waitingOrders); }
