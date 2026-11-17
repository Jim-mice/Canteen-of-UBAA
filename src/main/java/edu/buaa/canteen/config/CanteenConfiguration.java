package edu.buaa.canteen.config;

import edu.buaa.canteen.domain.menu.DishDefinition;
import edu.buaa.canteen.domain.menu.DishType;
import edu.buaa.canteen.domain.resource.Chef;
import edu.buaa.canteen.domain.resource.Window;
import edu.buaa.canteen.domain.user.UserGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Central source for the default menu, window count, and chef capacity configuration. */
public record CanteenConfiguration(List<DishDefinition> dishes, List<Window> windows) {
    public CanteenConfiguration { dishes = List.copyOf(dishes); windows = List.copyOf(windows); }
    public int totalChefCount() { return windows.stream().mapToInt(w -> w.chefs().size()).sum(); }

    public static CanteenConfiguration defaults() {
        var definitions = List.of(
                new DishDefinition(DishType.BEEF_NOODLES, "牛肉面", 4, 10, Optional.empty(), new int[]{3,3,2,2}),
                new DishDefinition(DishType.WONTON, "抄手 / 馄饨", 2, 20, Optional.empty(), new int[]{10,10}),
                new DishDefinition(DishType.YELLOW_BRAISED_CHICKEN, "黄焖鸡", 1, 10, Optional.empty(), new int[]{10}),
                new DishDefinition(DishType.HUNAN_SPECIALTY, "湘里湘外（湖南特色菜）", 1, 5, Optional.of(UserGroup.HUNAN), new int[]{5}));
        var windows = new ArrayList<Window>();
        for (var definition : definitions) {
            int[] capacities = definition.chefsPerWindow();
            for (int i = 0; i < capacities.length; i++) {
                String windowId = definition.type().name() + "-W" + (i + 1);
                var chefs = new ArrayList<Chef>();
                for (int j = 0; j < capacities[i]; j++) chefs.add(new Chef(windowId + "-C" + (j + 1), windowId));
                windows.add(new Window(windowId, definition.type(), chefs));
            }
        }
        return new CanteenConfiguration(definitions, windows);
    }
}
