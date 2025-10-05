package io.wintech.currency.service.provider;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface CurrencyProvider {
    String provider();
    Optional<Map<String, String>> getCurrencies();
    Optional<Map<String, String>> convert(String from, String to, BigDecimal amount);
    Optional<Map<String, String>> historical(String date, String base, String symbolsCsv);
    Optional<Map<String, String>> latest(String base, String symbolsCsv);
    Integer order();
}
