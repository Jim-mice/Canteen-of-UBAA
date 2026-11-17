package edu.buaa.canteen.domain.scheduling;

import edu.buaa.canteen.domain.order.Order;
import java.util.List;

/** Chooses which waiting order to consider next; users do not select the strategy. */
public interface SchedulingStrategy { List<Order> orderWaitingOrders(List<Order> waitingOrders); }
