package order;

import menu.DishType;
import user.User;
import java.math.BigDecimal;

public interface OrderService {
    /**
     * Validates and submits one single-dish order.
     * @throws IllegalArgumentException when required arguments are invalid
     * @throws order.OrderRejectedException when the user lacks access or capacity is impossible
     */
    Order submit(User user, DishType dishType, BigDecimal amount, ResourceRequirement requirement);
}
