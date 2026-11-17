package edu.buaa.canteen.application.access;

import edu.buaa.canteen.domain.menu.DishDefinition;
import edu.buaa.canteen.domain.user.User;

/** Checks required group membership; public dishes have no required group. */
public interface WindowAccessPolicy { boolean canAccess(User user, DishDefinition dish); }
