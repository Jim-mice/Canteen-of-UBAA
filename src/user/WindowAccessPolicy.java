package user;

import menu.DishDefinition;
import user.User;

/** Checks required group membership; public dishes have no required group. */
public interface WindowAccessPolicy { boolean canAccess(User user, DishDefinition dish); }
