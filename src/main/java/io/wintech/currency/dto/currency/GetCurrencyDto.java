package io.wintech.currency.dto.currency;

import java.util.List;

public record GetCurrencyDto(
        String symbol,
        String name,
        String code,
        String type,
        List<String> countries
) {
}
