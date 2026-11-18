package user;

import java.util.Objects;
import java.util.Set;

public abstract class User {
    private final String id;
    private final String displayName;
    private final Set<UserGroup> groups;
    protected User(String id, String displayName, Set<UserGroup> groups) {
        this.id = Objects.requireNonNull(id); this.displayName = Objects.requireNonNull(displayName);
        this.groups = Set.copyOf(groups);
        if (id.isBlank() || displayName.isBlank()) throw new IllegalArgumentException("User id and name are required");
    }
    public String id() { return id; }
    public String displayName() { return displayName; }
    public Set<UserGroup> groups() { return groups; }
}
