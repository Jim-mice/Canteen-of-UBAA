package order;

import menu.DishType;
import user.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** Order data is immutable; lifecycle transitions are controlled by the application service. */
public record Order(String id, User submitter, DishType dishType, BigDecimal amount,
                    ResourceRequirement requirement, Instant createdAt, OrderStatus status) {
    public Order {
        Objects.requireNonNull(id); Objects.requireNonNull(submitter); Objects.requireNonNull(dishType);
        Objects.requireNonNull(amount); Objects.requireNonNull(requirement); Objects.requireNonNull(createdAt); Objects.requireNonNull(status);
        if (id.isBlank() || amount.signum() <= 0) throw new IllegalArgumentException("Order id and positive amount are required");
    }
    /** Creates an updated immutable order only when the requested lifecycle transition is allowed. */
    public Order transitionTo(OrderStatus next) {
        Objects.requireNonNull(next);
        boolean allowed = switch (status) {
            case CREATED -> next == OrderStatus.WAITING || next == OrderStatus.ALLOCATED || next == OrderStatus.CANCELLED || next == OrderStatus.REJECTED;
            case WAITING -> next == OrderStatus.ALLOCATED || next == OrderStatus.CANCELLED || next == OrderStatus.REJECTED;
            case ALLOCATED -> next == OrderStatus.IN_PROGRESS || next == OrderStatus.CANCELLED;
            case IN_PROGRESS -> next == OrderStatus.COMPLETED || next == OrderStatus.CANCELLED;
            case COMPLETED, CANCELLED, REJECTED -> false;
        };
        if (!allowed) throw new IllegalStateException("Illegal order status transition: " + status + " -> " + next);
        return new Order(id, submitter, dishType, amount, requirement, createdAt, next);
    }
}
