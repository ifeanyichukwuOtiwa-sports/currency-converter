package io.wintech.currency.dto.currency;

import java.util.Map;

public record ConvertResponse(
        Map<String, CurrencyDataDto> data
) {
}
