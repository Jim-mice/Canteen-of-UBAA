package resource;

import java.util.List;

public record ResourceAllocation(String orderId, List<Window> windows, List<Chef> chefs) {
    public ResourceAllocation { windows = List.copyOf(windows); chefs = List.copyOf(chefs); if (orderId == null || chefs.isEmpty()) throw new IllegalArgumentException("Allocation requires an order and chefs"); }
}
