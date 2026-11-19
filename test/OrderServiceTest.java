import config.CanteenConfiguration;
import menu.DishDefinition;
import menu.DishType;
import order.DefaultOrderService;
import order.InMemoryOrderRepository;
import order.Order;
import order.OrderRejectedException;
import order.OrderStatus;
import order.ResourceRequirement;
import user.DefaultWindowAccessPolicy;
import user.GroupUser;
import user.InMemoryUserRepository;
import user.RegularUser;
import user.User;
import user.UserGroup;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public final class OrderServiceTest {
    private static final Instant FIXED_TIME = Instant.parse("2026-11-19T08:00:00Z");

    private OrderServiceTest() { }

    public static void run() {
        submitsRegisteredUserOrderInSaveThenEnqueueOrder();
        rejectsInvalidInputsWithoutSideEffects();
        rejectsUnknownAndUnprivilegedUsers();
        checksDishConfigurationAndStaticCapacity();
        acceptsValidSameWindowAndCrossWindowCapacity();
        returnsSynchronouslyUpdatedOrder();
        propagatesEnqueueFailureWithoutOverwritingSchedulerState();
    }

    private static void submitsRegisteredUserOrderInSaveThenEnqueueOrder() {
        Fixture fixture = fixture(new RegularUser("U-1", "Regular"), CanteenConfiguration.defaults().dishes());
        User registered = fixture.users.findById("U-1").orElseThrow();
        fixture.scheduler.onEnqueue(enqueued -> TestSupport.assertTrue(
                fixture.orders.findById(enqueued.id()).orElse(null) == enqueued,
                "order must be saved before enqueue"));

        Order submitted = fixture.service.submit(new RegularUser("U-1", "Caller copy"), DishType.BEEF_NOODLES,
                new BigDecimal("40"), new ResourceRequirement(4, false));

        TestSupport.assertTrue(submitted.submitter() == registered, "order must use registered user object");
        TestSupport.assertEquals(OrderStatus.CREATED, submitted.status(), "initial order status");
        TestSupport.assertEquals(FIXED_TIME, submitted.createdAt(), "order time comes from injected clock");
        TestSupport.assertTrue(fixture.orders.findById(submitted.id()).orElseThrow() == submitted,
                "submitted order stored");
        TestSupport.assertEquals(1, fixture.scheduler.enqueuedOrders().size(), "one enqueue call");
        TestSupport.assertTrue(fixture.scheduler.enqueuedOrders().get(0) == submitted,
                "scheduler receives saved order");
    }

    private static void rejectsInvalidInputsWithoutSideEffects() {
        Fixture fixture = fixture(new RegularUser("U-1", "Regular"), CanteenConfiguration.defaults().dishes());
        var requirement = new ResourceRequirement(1, false);
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> fixture.service.submit(null, DishType.BEEF_NOODLES, BigDecimal.ONE, requirement), "null user");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> fixture.service.submit(new RegularUser("U-1", "Caller"), null, BigDecimal.ONE, requirement), "null dish type");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> fixture.service.submit(new RegularUser("U-1", "Caller"), DishType.BEEF_NOODLES, null, requirement), "null amount");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> fixture.service.submit(new RegularUser("U-1", "Caller"), DishType.BEEF_NOODLES, BigDecimal.ZERO, requirement), "zero amount");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> fixture.service.submit(new RegularUser("U-1", "Caller"), DishType.BEEF_NOODLES, new BigDecimal("-1"), requirement), "negative amount");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> fixture.service.submit(new RegularUser("U-1", "Caller"), DishType.BEEF_NOODLES, BigDecimal.ONE, null), "null requirement");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> new ResourceRequirement(0, false), "zero chef requirement");
        TestSupport.assertTrue(fixture.orders.findByStatus(OrderStatus.CREATED).isEmpty(), "invalid input creates no order");
        TestSupport.assertTrue(fixture.scheduler.enqueuedOrders().isEmpty(), "invalid input is not enqueued");
    }

    private static void rejectsUnknownAndUnprivilegedUsers() {
        var definitions = CanteenConfiguration.defaults().dishes();
        Fixture unknown = fixture(null, definitions);
        User fakeHunanUser = new GroupUser("U-FAKE", "Unregistered", Set.of(UserGroup.HUNAN));
        TestSupport.assertThrows(OrderRejectedException.class,
                () -> unknown.service.submit(fakeHunanUser, DishType.HUNAN_SPECIALTY, BigDecimal.TEN,
                        new ResourceRequirement(1, false)), "unregistered user is rejected");
        assertRejectedWithoutSideEffects(unknown, "unregistered user");

        Fixture spoofed = fixture(new RegularUser("U-1", "Registered regular"), definitions);
        User callerClaim = new GroupUser("U-1", "Forged group claim", Set.of(UserGroup.HUNAN));
        TestSupport.assertThrows(OrderRejectedException.class,
                () -> spoofed.service.submit(callerClaim, DishType.HUNAN_SPECIALTY, BigDecimal.TEN,
                        new ResourceRequirement(1, false)), "permissions use repository user, not caller object");
        assertRejectedWithoutSideEffects(spoofed, "spoofed privileges");

        Fixture regular = fixture(new RegularUser("U-1", "Regular"), definitions);
        TestSupport.assertThrows(OrderRejectedException.class,
                () -> regular.service.submit(new RegularUser("U-1", "Caller"), DishType.HUNAN_SPECIALTY,
                        BigDecimal.TEN, new ResourceRequirement(1, false)), "regular user cannot access Hunan-only dish");
        assertRejectedWithoutSideEffects(regular, "access denied");
    }

    private static void checksDishConfigurationAndStaticCapacity() {
        List<DishDefinition> withoutBeef = CanteenConfiguration.defaults().dishes().stream()
                .filter(dish -> dish.type() != DishType.BEEF_NOODLES).toList();
        Fixture unknownDish = fixture(new RegularUser("U-1", "Regular"), withoutBeef);
        TestSupport.assertThrows(OrderRejectedException.class,
                () -> unknownDish.service.submit(new RegularUser("U-1", "Caller"), DishType.BEEF_NOODLES,
                        BigDecimal.TEN, new ResourceRequirement(1, false)), "dish absent from config is rejected");
        assertRejectedWithoutSideEffects(unknownDish, "unknown dish");

        Fixture overTotal = fixture(new RegularUser("U-1", "Regular"), CanteenConfiguration.defaults().dishes());
        TestSupport.assertThrows(OrderRejectedException.class,
                () -> overTotal.service.submit(new RegularUser("U-1", "Caller"), DishType.BEEF_NOODLES,
                        BigDecimal.TEN, new ResourceRequirement(11, false)), "above total beef capacity is rejected");
        assertRejectedWithoutSideEffects(overTotal, "above total capacity");

        Fixture overWindow = fixture(new RegularUser("U-1", "Regular"), CanteenConfiguration.defaults().dishes());
        TestSupport.assertThrows(OrderRejectedException.class,
                () -> overWindow.service.submit(new RegularUser("U-1", "Caller"), DishType.BEEF_NOODLES,
                        BigDecimal.TEN, new ResourceRequirement(4, true)), "above maximum single-window capacity is rejected");
        assertRejectedWithoutSideEffects(overWindow, "above single-window capacity");
    }

    private static void acceptsValidSameWindowAndCrossWindowCapacity() {
        assertAccepted(DishType.BEEF_NOODLES, 3, true, "same-window maximum");
        assertAccepted(DishType.BEEF_NOODLES, 4, false, "cross-window combination");
        assertAccepted(DishType.BEEF_NOODLES, 10, false, "total configured capacity");
    }

    private static void returnsSynchronouslyUpdatedOrder() {
        Fixture fixture = fixture(new RegularUser("U-1", "Regular"), CanteenConfiguration.defaults().dishes());
        fixture.scheduler.onEnqueue(order -> fixture.orders.save(order.transitionTo(OrderStatus.WAITING)));
        Order result = fixture.service.submit(new RegularUser("U-1", "Caller"), DishType.BEEF_NOODLES,
                BigDecimal.TEN, new ResourceRequirement(1, false));
        TestSupport.assertEquals(OrderStatus.WAITING, result.status(), "return latest scheduler-persisted status");
        TestSupport.assertEquals(OrderStatus.WAITING,
                fixture.orders.findById(result.id()).orElseThrow().status(), "waiting state is persisted");
    }

    private static void propagatesEnqueueFailureWithoutOverwritingSchedulerState() {
        Fixture fixture = fixture(new RegularUser("U-1", "Regular"), CanteenConfiguration.defaults().dishes());
        fixture.scheduler.onEnqueue(order -> {
            fixture.orders.save(order.transitionTo(OrderStatus.WAITING));
            throw new IllegalStateException("scheduler unavailable");
        });
        TestSupport.assertThrows(IllegalStateException.class,
                () -> fixture.service.submit(new RegularUser("U-1", "Caller"), DishType.BEEF_NOODLES,
                        BigDecimal.TEN, new ResourceRequirement(1, false)), "enqueue exception propagates");
        TestSupport.assertEquals(1, fixture.orders.findByStatus(OrderStatus.WAITING).size(),
                "scheduler state remains persisted after its exception");
        TestSupport.assertEquals(1, fixture.scheduler.enqueuedOrders().size(), "no retry or duplicate enqueue");
    }

    private static void assertAccepted(DishType dishType, int chefCount, boolean sameWindow, String label) {
        Fixture fixture = fixture(new RegularUser("U-1", "Regular"), CanteenConfiguration.defaults().dishes());
        Order order = fixture.service.submit(new RegularUser("U-1", "Caller"), dishType,
                BigDecimal.TEN, new ResourceRequirement(chefCount, sameWindow));
        TestSupport.assertEquals(OrderStatus.CREATED, order.status(), label + " status");
        TestSupport.assertEquals(1, fixture.scheduler.enqueuedOrders().size(), label + " enqueued once");
    }

    private static void assertRejectedWithoutSideEffects(Fixture fixture, String label) {
        TestSupport.assertTrue(fixture.orders.findByStatus(OrderStatus.CREATED).isEmpty(), label + " creates no order");
        TestSupport.assertTrue(fixture.scheduler.enqueuedOrders().isEmpty(), label + " does not enqueue");
    }

    private static Fixture fixture(User registeredUser, List<DishDefinition> dishes) {
        var users = new InMemoryUserRepository();
        if (registeredUser != null) users.save(registeredUser);
        var orders = new InMemoryOrderRepository();
        var scheduler = new TestSupport.RecordingScheduler();
        var service = new DefaultOrderService(users, orders, new DefaultWindowAccessPolicy(), scheduler,
                new TestSupport.FixedSimulationClock(FIXED_TIME), dishes);
        return new Fixture(users, orders, scheduler, service);
    }

    private record Fixture(InMemoryUserRepository users, InMemoryOrderRepository orders,
                           TestSupport.RecordingScheduler scheduler, DefaultOrderService service) { }
}
