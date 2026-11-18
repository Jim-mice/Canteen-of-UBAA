package resource;

import java.util.List;
import java.util.Optional;

/** Owns resource status and atomic reservation/release operations. */
public interface ResourceManager {
    /** Returns currently open windows as an immutable snapshot. */
    List<Window> availableWindows();
    /** Atomically reserves all requested chefs, or returns empty without changing any resource. */
    Optional<ResourceAllocation> reserve(String orderId, List<Window> windows, int chefCount, boolean sameWindowRequired);
    /** Releases every chef held by the allocation; repeated release behavior must be documented by implementation. */
    void release(ResourceAllocation allocation);
    ChefStatus chefStatus(String chefId);
    WindowStatus windowStatus(String windowId);
}
