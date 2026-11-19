package user;

import menu.DishDefinition;

/** Checks required group membership; public dishes have no required group. */
public interface WindowAccessPolicy { boolean canAccess(User user, DishDefinition dish); }
