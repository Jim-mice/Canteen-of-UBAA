package edu.buaa.canteen.config;

import edu.buaa.canteen.domain.menu.DishType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CanteenConfigurationTest {
    @Test void defaultsHaveEightWindowsAndFortyFiveChefs() {
        var config = CanteenConfiguration.defaults();
        assertEquals(8, config.windows().size());
        assertEquals(45, config.totalChefCount());
        assertArrayEquals(new int[]{3, 3, 2, 2}, config.dishes().stream()
                .filter(d -> d.type() == DishType.BEEF_NOODLES).findFirst().orElseThrow().chefsPerWindow());
    }
}
