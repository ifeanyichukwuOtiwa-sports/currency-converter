package io.wintech.currency.entity;

import java.time.ZonedDateTime;
import java.util.UUID;

public record RecentRate(
        UUID id,
        String baseSymbol,
        ZonedDateTime requestTime,
        boolean status
) {
}
