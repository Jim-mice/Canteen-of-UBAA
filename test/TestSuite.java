public final class TestSuite {
    private TestSuite() { }

    public static void main(String[] args) {
        run("CanteenConfigurationTest", CanteenConfigurationTest::run);
        run("OrderTest", OrderTest::run);
        run("UserRepositoryTest", UserRepositoryTest::run);
        run("WindowAccessPolicyTest", WindowAccessPolicyTest::run);
        run("OrderRepositoryTest", OrderRepositoryTest::run);
        run("WorkloadCalculatorTest", WorkloadCalculatorTest::run);
        run("OrderServiceTest", OrderServiceTest::run);
        System.out.println("All 7 test groups passed.");
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            System.out.println("PASS " + name);
        } catch (Throwable failure) {
            System.err.println("FAIL " + name + ": " + failure.getMessage());
            failure.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
