import menu.DishType;
import order.Order;
import order.OrderStatus;
import order.ResourceRequirement;
import user.RegularUser;
import java.math.BigDecimal;
import java.time.Instant;

public final class OrderTest {
    private OrderTest() { }

    public static void run() {
        validTransitionReturnsNewOrder();
        terminalOrderCannotTransitionAgain();
    }

    private static void validTransitionReturnsNewOrder() {
        var created = createdOrder();
        var waiting = created.transitionTo(OrderStatus.WAITING);
        assertEquals(OrderStatus.CREATED, created.status(), "原订单状态保持不变");
        assertEquals(OrderStatus.WAITING, waiting.status(), "合法迁移后的状态");
    }

    private static void terminalOrderCannotTransitionAgain() {
        var completed = createdOrder().transitionTo(OrderStatus.ALLOCATED)
                .transitionTo(OrderStatus.IN_PROGRESS).transitionTo(OrderStatus.COMPLETED);
        assertThrows(IllegalStateException.class,
                () -> completed.transitionTo(OrderStatus.CANCELLED), "完成订单不得再次迁移");
    }

    private static Order createdOrder() {
        return new Order("O-1", new RegularUser("U-1", "测试用户"), DishType.BEEF_NOODLES,
                new BigDecimal("12.50"), new ResourceRequirement(2, false), Instant.EPOCH, OrderStatus.CREATED);
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) throw new AssertionError(label + "：期望 " + expected + "，实际 " + actual);
    }

    private static void assertThrows(Class<? extends Throwable> expected, Runnable action, String label) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) return;
            throw new AssertionError(label + "：抛出了意外异常 " + actual, actual);
        }
        throw new AssertionError(label + "：未抛出预期异常 " + expected.getSimpleName());
    }
}
