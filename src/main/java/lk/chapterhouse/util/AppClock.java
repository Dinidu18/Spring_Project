package lk.chapterhouse.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public final class AppClock {
    public static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Colombo");
    private static volatile Clock clock = Clock.systemUTC();

    private AppClock() {
    }

    public static Instant now() {
        return Instant.now(clock);
    }

    public static Clock clock() {
        return clock;
    }

    /** Test-only hook. */
    public static void use(Clock testClock) {
        clock = testClock == null ? Clock.systemUTC() : testClock;
    }

    public static void reset() {
        clock = Clock.systemUTC();
    }
}
