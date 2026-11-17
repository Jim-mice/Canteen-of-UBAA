package edu.buaa.canteen.domain.order;

import edu.buaa.canteen.domain.menu.DishType;
import edu.buaa.canteen.domain.user.RegularUser;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    private Order createdOrder() {
        return new Order("O-1", new RegularUser("U-1", "测试用户"), DishType.BEEF_NOODLES,
                new BigDecimal("12.50"), new ResourceRequirement(2, false), Instant.EPOCH, OrderStatus.CREATED);
    }
    @Test void validTransitionReturnsNewOrder() {
        var created = createdOrder();
        var waiting = created.transitionTo(OrderStatus.WAITING);
        assertEquals(OrderStatus.CREATED, created.status());
        assertEquals(OrderStatus.WAITING, waiting.status());
    }
    @Test void terminalOrderCannotTransitionAgain() {
        var completed = createdOrder().transitionTo(OrderStatus.ALLOCATED)
                .transitionTo(OrderStatus.IN_PROGRESS).transitionTo(OrderStatus.COMPLETED);
        assertThrows(IllegalStateException.class, () -> completed.transitionTo(OrderStatus.CANCELLED));
    }
}
