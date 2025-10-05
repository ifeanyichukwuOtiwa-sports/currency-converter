package io.wintech.currency.rest.response;

import java.math.BigDecimal;

public record ConvertRequest(
        String from,
        String to,
        BigDecimal amount
) {
}
