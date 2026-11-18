package resource;

import order.Order;
import java.util.List;
import java.util.Optional;

public interface ResourceAllocator {
    /** Returns an allocation only when the full chef requirement can be met; partial reservations are forbidden. */
    Optional<ResourceAllocation> tryAllocate(Order order, List<Window> candidateWindows);
}
