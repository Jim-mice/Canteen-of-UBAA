import order.Order;
import scheduling.SchedulerService;
import simulation.SimulationClock;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class TestSupport {
    private TestSupport() { }

    static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    static void assertDecimalEquals(String expected, java.math.BigDecimal actual, String message) {
        if (new java.math.BigDecimal(expected).compareTo(actual) != 0) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual.toPlainString());
        }
    }

    static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) return expectedType.cast(actual);
            throw new AssertionError(message + ": unexpected exception " + actual, actual);
        }
        throw new AssertionError(message + ": expected " + expectedType.getSimpleName());
    }

    static final class FixedSimulationClock implements SimulationClock {
        private final Instant instant;
        FixedSimulationClock(Instant instant) { this.instant = instant; }
        @Override public Instant now() { return instant; }
    }

    static final class RecordingScheduler implements SchedulerService {
        private final List<Order> enqueuedOrders = new ArrayList<>();
        private Consumer<Order> enqueueAction = order -> { };

        void onEnqueue(Consumer<Order> action) { enqueueAction = action; }
        List<Order> enqueuedOrders() { return List.copyOf(enqueuedOrders); }

        @Override public void enqueue(Order order) {
            enqueuedOrders.add(order);
            enqueueAction.accept(order);
        }

        @Override public List<Order> waitingOrders() { return List.of(); }
        @Override public void scheduleAvailableOrders() { }
    }
}
