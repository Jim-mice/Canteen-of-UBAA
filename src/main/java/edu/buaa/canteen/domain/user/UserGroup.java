package edu.buaa.canteen.domain.user;

import java.util.Objects;

public record UserGroup(String id, String name) {
    public UserGroup { Objects.requireNonNull(id); Objects.requireNonNull(name); if (id.isBlank() || name.isBlank()) throw new IllegalArgumentException("Group id and name are required"); }
    public static final UserGroup HUNAN = new UserGroup("HUNAN", "湖南用户组");
}
