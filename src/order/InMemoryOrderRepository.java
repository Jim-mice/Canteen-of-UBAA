package order;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory order storage preserving insertion order for deterministic queries. */
public final class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> orders = new LinkedHashMap<>();

    @Override
    public Optional<Order> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(orders.get(id));
    }

    /** Stores or replaces an order by ID and returns the stored immutable value. */
    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null");
        }
        String id = order.id();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("order ID must not be null or blank");
        }
        orders.put(id, order);
        return order;
    }

    /** Returns matching orders in insertion order as an immutable snapshot. */
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        List<Order> matches = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.status() == status) {
                matches.add(order);
            }
        }
        return List.copyOf(matches);
    }
}
