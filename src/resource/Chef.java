package resource;

import java.util.Objects;

/** Chef identity and home window are immutable; status changes belong to ResourceManager. */
public final class Chef {
    private final String id;
    private final String windowId;
    private final ChefStatus status;
    public Chef(String id, String windowId) { this(id, windowId, ChefStatus.IDLE); }
    public Chef(String id, String windowId, ChefStatus status) {
        this.id = Objects.requireNonNull(id); this.windowId = Objects.requireNonNull(windowId);
        this.status = Objects.requireNonNull(status);
    }
    public String id() { return id; }
    public String windowId() { return windowId; }
    public ChefStatus status() { return status; }
}
