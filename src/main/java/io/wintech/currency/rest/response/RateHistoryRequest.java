package io.wintech.currency.rest.response;

public record RateHistoryRequest(
        String date,
        String base,
        String symbols
) {
}
