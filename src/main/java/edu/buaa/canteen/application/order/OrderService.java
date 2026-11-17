package edu.buaa.canteen.application.order;

import edu.buaa.canteen.domain.menu.DishType;
import edu.buaa.canteen.domain.order.Order;
import edu.buaa.canteen.domain.order.ResourceRequirement;
import edu.buaa.canteen.domain.user.User;
import java.math.BigDecimal;

public interface OrderService {
    /**
     * Validates and submits one single-dish order.
     * @throws IllegalArgumentException when required arguments are invalid
     * @throws edu.buaa.canteen.exception.OrderRejectedException when the user lacks access or capacity is impossible
     */
    Order submit(User user, DishType dishType, BigDecimal amount, ResourceRequirement requirement);
}
