import order.DefaultWorkloadCalculator;

import java.math.BigDecimal;

public final class WorkloadCalculatorTest {
    private WorkloadCalculatorTest() { }

    public static void run() {
        var calculator = new DefaultWorkloadCalculator();
        TestSupport.assertDecimalEquals("10", calculator.estimatedMinutes(new BigDecimal("40"), 4), "integer result");
        TestSupport.assertDecimalEquals("6.25", calculator.estimatedMinutes(new BigDecimal("12.5"), 2), "decimal amount");
        TestSupport.assertDecimalEquals("20", calculator.estimatedMinutes(new BigDecimal("100"), 5), "second integer result");
        TestSupport.assertDecimalEquals("3.333333333333333333333333333333333",
                calculator.estimatedMinutes(new BigDecimal("10"), 3), "non-terminating division uses DECIMAL128");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> calculator.estimatedMinutes(null, 1), "null amount");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> calculator.estimatedMinutes(BigDecimal.ZERO, 1), "zero amount");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> calculator.estimatedMinutes(new BigDecimal("-1"), 1), "negative amount");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> calculator.estimatedMinutes(BigDecimal.ONE, 0), "zero chef count");
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> calculator.estimatedMinutes(BigDecimal.ONE, -1), "negative chef count");
    }
}
