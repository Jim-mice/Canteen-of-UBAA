package edu.buaa.canteen.domain.resource;

import edu.buaa.canteen.domain.menu.DishType;
import java.util.List;
import java.util.Objects;

/** Immutable window structure; mutable operational status is maintained by ResourceManager. */
public record Window(String id, DishType dishType, List<Chef> chefs, WindowStatus status) {
    public Window(String id, DishType dishType, List<Chef> chefs) { this(id, dishType, chefs, WindowStatus.OPEN); }
    public Window { Objects.requireNonNull(id); Objects.requireNonNull(dishType); Objects.requireNonNull(status); chefs = List.copyOf(chefs); if (chefs.isEmpty() || chefs.stream().anyMatch(c -> !c.windowId().equals(id))) throw new IllegalArgumentException("Window must own its chefs"); }
}
