package io.wintech.currency.dto.fixer;

import java.util.Map;

public record FixerLatestResponse(
        boolean success,
        Map<String, Double> rates
) {}
