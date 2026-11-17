package edu.buaa.canteen.application.resource;

import edu.buaa.canteen.domain.order.Order;
import edu.buaa.canteen.domain.resource.Window;
import edu.buaa.canteen.domain.scheduling.ResourceAllocation;
import java.util.List;
import java.util.Optional;

public interface ResourceAllocator {
    /** Returns an allocation only when the full chef requirement can be met; partial reservations are forbidden. */
    Optional<ResourceAllocation> tryAllocate(Order order, List<Window> candidateWindows);
}
