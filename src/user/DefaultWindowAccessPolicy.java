package user;

import menu.DishDefinition;

/** Grants access from the configured required-group membership, without user-type or dish-name checks. */
public final class DefaultWindowAccessPolicy implements WindowAccessPolicy {
    @Override
    public boolean canAccess(User user, DishDefinition dish) {
        if (user == null || dish == null) {
            return false;
        }
        return dish.requiredGroup().map(user.groups()::contains).orElse(true);
    }
}
