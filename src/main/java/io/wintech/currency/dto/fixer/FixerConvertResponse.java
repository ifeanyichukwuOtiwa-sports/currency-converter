package io.wintech.currency.dto.fixer;

public record FixerConvertResponse(
        boolean success,
        Query query,
        Boolean historical,
        Double result
) {
    public record Query(String from, String to, double amount) {}
    public record Info(long timestamp, double rate) {}
}
