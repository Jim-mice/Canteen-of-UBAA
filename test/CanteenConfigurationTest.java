import config.CanteenConfiguration;
import menu.DishType;

public final class CanteenConfigurationTest {
    private CanteenConfigurationTest() { }

    public static void main(String[] args) {
        run();
        System.out.println("PASS CanteenConfigurationTest");
        OrderTest.run();
        System.out.println("PASS OrderTest");
        System.out.println("All tests passed.");
    }

    public static void run() {
        var config = CanteenConfiguration.defaults();
        assertEquals(8, config.windows().size(), "食堂窗口数量");
        assertEquals(45, config.totalChefCount(), "厨师总数");
        int[] distribution = config.dishes().stream()
                .filter(dish -> dish.type() == DishType.BEEF_NOODLES)
                .findFirst().orElseThrow().chefsPerWindow();
        assertArrayEquals(new int[]{3, 3, 2, 2}, distribution, "牛肉面厨师分布");
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) throw new AssertionError(label + "：期望 " + expected + "，实际 " + actual);
    }

    private static void assertArrayEquals(int[] expected, int[] actual, String label) {
        if (!java.util.Arrays.equals(expected, actual))
            throw new AssertionError(label + "：期望 " + java.util.Arrays.toString(expected)
                    + "，实际 " + java.util.Arrays.toString(actual));
    }
}
