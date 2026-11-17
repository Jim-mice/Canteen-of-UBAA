package edu.buaa.canteen.domain.user;

import java.util.Set;

public final class RegularUser extends User {
    public RegularUser(String id, String displayName) { super(id, displayName, Set.of()); }
}
