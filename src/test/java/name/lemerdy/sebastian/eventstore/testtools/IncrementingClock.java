package name.lemerdy.sebastian.eventstore.testtools;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

public class IncrementingClock extends Clock {

    private final AtomicLong epochSecond;
    private final int nano;

    public IncrementingClock(Instant from) {
        this.nano = from.getNano();
        this.epochSecond = new AtomicLong(from.getEpochSecond());
    }

    @Override
    public ZoneId getZone() {
        throw new IllegalStateException();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new IllegalStateException();
    }

    @Override
    public Instant instant() {
        return Instant.ofEpochSecond(this.epochSecond.getAndIncrement()).plusNanos(nano);
    }

}
