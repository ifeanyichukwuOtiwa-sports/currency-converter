package io.wintech.currency.dto;

import java.util.Map;

public record LatestRateDto(
        Map<String, Double> rates
) {
}
