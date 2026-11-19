import menu.DishType;
import order.InMemoryOrderRepository;
import order.Order;
import order.OrderStatus;
import order.ResourceRequirement;
import user.RegularUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderRepositoryTest {
    private OrderRepositoryTest() { }

    public static void run() {
        missingOrdersReturnEmpty();
        saveAndStatusUpdateReplaceTheImmutableOrder();
        statusResultsAreDeterministicAndImmutable();
        rejectsNullOrdersAndStatuses();
    }

    private static void missingOrdersReturnEmpty() {
        var repository = new InMemoryOrderRepository();
        TestSupport.assertTrue(repository.findById("missing").isEmpty(), "unknown order should be empty");
        TestSupport.assertTrue(repository.findById(null).isEmpty(), "null ID lookup should be empty");
    }

    private static void saveAndStatusUpdateReplaceTheImmutableOrder() {
        var repository = new InMemoryOrderRepository();
        Order created = order("O-1", OrderStatus.CREATED);
        repository.save(created);
        Order waiting = created.transitionTo(OrderStatus.WAITING);
        TestSupport.assertEquals(OrderStatus.CREATED, created.status(), "transition does not mutate original order");
        repository.save(waiting);
        TestSupport.assertTrue(repository.findById("O-1").orElseThrow() == waiting,
                "repository must store replacement object returned by transitionTo");
        TestSupport.assertEquals(List.of(waiting), repository.findByStatus(OrderStatus.WAITING),
                "updated status query");
    }

    private static void statusResultsAreDeterministicAndImmutable() {
        var repository = new InMemoryOrderRepository();
        Order first = order("O-1", OrderStatus.WAITING);
        Order second = order("O-2", OrderStatus.WAITING);
        Order otherStatus = order("O-3", OrderStatus.CREATED);
        repository.save(first);
        repository.save(second);
        repository.save(otherStatus);
        List<Order> found = repository.findByStatus(OrderStatus.WAITING);
        TestSupport.assertEquals(List.of(first, second), found, "status results preserve insertion order");
        TestSupport.assertThrows(UnsupportedOperationException.class,
                () -> found.add(otherStatus), "status results must be immutable");
    }

    private static void rejectsNullOrdersAndStatuses() {
        var repository = new InMemoryOrderRepository();
        TestSupport.assertThrows(IllegalArgumentException.class, () -> repository.save(null), "null order");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> repository.findByStatus(null), "null status");
    }

    private static Order order(String id, OrderStatus status) {
        return new Order(id, new RegularUser("U-1", "User"), DishType.BEEF_NOODLES,
                new BigDecimal("20"), new ResourceRequirement(2, false), Instant.EPOCH, status);
    }
}
