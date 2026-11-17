package edu.buaa.canteen.domain.user;

import java.util.Set;

public final class GroupUser extends User {
    public GroupUser(String id, String displayName, Set<UserGroup> groups) { super(id, displayName, groups); }
}
