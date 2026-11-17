package edu.buaa.canteen.application.order;

import edu.buaa.canteen.domain.order.Order;
import java.math.BigDecimal;
import java.math.MathContext;

/** Computes ideal duration in minutes as amount divided by allocated chef count. */
public interface WorkloadCalculator { BigDecimal estimatedMinutes(BigDecimal amount, int chefCount); default BigDecimal estimatedMinutes(Order order) { return estimatedMinutes(order.amount(), order.requirement().chefCount()); } }
