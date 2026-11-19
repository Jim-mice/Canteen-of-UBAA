package order;

import java.math.BigDecimal;
import java.math.MathContext;

/** Calculates ideal preparation time in minutes using decimal arithmetic. */
public final class DefaultWorkloadCalculator implements WorkloadCalculator {
    @Override
    public BigDecimal estimatedMinutes(BigDecimal amount, int chefCount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (chefCount <= 0) {
            throw new IllegalArgumentException("chefCount must be positive");
        }
        return amount.divide(BigDecimal.valueOf(chefCount), MathContext.DECIMAL128);
    }
}
