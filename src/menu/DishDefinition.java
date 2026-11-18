package menu;

import user.UserGroup;
import java.util.Optional;

/** Immutable menu and capacity definition for one dish type. */
public record DishDefinition(DishType type, String name, int windowCount, int totalChefCount,
                             Optional<UserGroup> requiredGroup, int[] chefsPerWindow) {
    public DishDefinition {
        if (type == null || name == null || name.isBlank() || requiredGroup == null || chefsPerWindow == null
                || windowCount < 1 || totalChefCount < 1 || chefsPerWindow.length != windowCount
                || java.util.Arrays.stream(chefsPerWindow).anyMatch(n -> n < 1)
                || java.util.Arrays.stream(chefsPerWindow).sum() != totalChefCount)
            throw new IllegalArgumentException("Invalid dish definition");
        chefsPerWindow = chefsPerWindow.clone();
    }
    @Override public int[] chefsPerWindow() { return chefsPerWindow.clone(); }
}
