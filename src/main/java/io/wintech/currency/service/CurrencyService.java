package io.wintech.currency.service;

import io.wintech.currency.service.provider.CurrencyProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CurrencyService {
    private static final Map<String, String> UNAVAILABLE_MESSAGE = Map.of("message", "service currently unavailable, please try again later..");
    private final Map<String, CurrencyProvider> providers;

    public CurrencyService(final List<CurrencyProvider> providers) {
        this.providers = providers.stream()
                .sorted(Comparator.comparingInt(CurrencyProvider::order))
                .collect(Collectors.toMap(
                        CurrencyProvider::provider,
                        Function.identity(),
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));
    }


    public Map<String, String> getCurrencies() {
        final Optional<Map<String, String>> resp = providers.values()
                .stream()
                .flatMap(i -> i.getCurrencies().stream())
                .findFirst();
        return resp.orElse(UNAVAILABLE_MESSAGE);
    }

    public Map<String, String> convert(final String from, final String to, final BigDecimal amount) {
        final Optional<Map<String, String>> resp = providers.values()
                .stream()
                .flatMap(p -> p.convert(from, to, amount).stream())
                .findFirst();
        return resp.orElse(UNAVAILABLE_MESSAGE);
    }

    public Map<String, String> getHistorical(final String date, final String base, final String symbolsCsv) {
        final Optional<Map<String, String>> resp = providers.values()
                .stream()
                .flatMap(p -> p.historical(date, base, symbolsCsv).stream())
                .findFirst();
        return resp.orElse(UNAVAILABLE_MESSAGE);
    }

    public Map<String, String> getLatest(final String base, final String symbolsCsv) {
        return providers.values().stream()
                .map(p -> p.latest(base, symbolsCsv))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(UNAVAILABLE_MESSAGE);
    }
}
