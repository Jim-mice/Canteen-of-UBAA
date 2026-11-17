package edu.buaa.canteen.application.scheduling;

import edu.buaa.canteen.domain.order.Order;
import java.util.List;

public interface SchedulerService {
    /** Adds an accepted order to scheduling and attempts eligible waiting work. */
    void enqueue(Order order);
    /** Returns an immutable snapshot of orders currently waiting for resources. */
    List<Order> waitingOrders();
    void scheduleAvailableOrders();
}
