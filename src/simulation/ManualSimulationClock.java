package simulation;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** An explicitly advanced clock for a single-threaded simulation. */
public final class ManualSimulationClock implements SimulationClock {
    private Instant time;

    public ManualSimulationClock(Instant initialTime) {
        time = Objects.requireNonNull(initialTime, "initialTime");
    }

    @Override
    public Instant now() {
        return time;
    }

    public void advance(Duration duration) {
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Simulation time cannot move backwards");
        }
        time = time.plus(duration);
    }
}
