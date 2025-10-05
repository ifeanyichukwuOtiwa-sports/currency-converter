package io.wintech.currency.service;

import io.wintech.currency.util.CurrencyUrls;
import io.wintech.currency.config.CurrencyConfigProperties;
import io.wintech.currency.dto.fixer.FixerConvertResponse;
import io.wintech.currency.dto.fixer.FixerLatestResponse;
import io.wintech.currency.dto.fixer.FixerSymbolsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class FixerApiClient {
    private static final String ACCESS_KEY = "access_key";
    private final RestClient restClient;
    private final CurrencyConfigProperties props;

    public FixerLatestResponse getLatest(String base, String symbolsCsv) {
        final MultiValueMap<String, String> q = buildSymbolParams(base, symbolsCsv);

        return restClient.get()
                .uri(builder -> builder.path(CurrencyUrls.FIXER_LATEST_URL)
                        .queryParams(q)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(FixerLatestResponse.class);
    }

    private MultiValueMap<String, String> buildSymbolParams(final String base, final String symbolsCsv) {
        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add(ACCESS_KEY, props.fixerAccessKey());
        Optional.ofNullable(symbolsCsv)
                .filter(s -> !s.isBlank())
                .ifPresent(s -> q.add("symbols", s));
        Optional.ofNullable(base)
                .filter(b -> !b.isBlank())
                .ifPresent(b -> q.add("base", b));
        return q;
    }

    public Map<String, Double> getLatestRates(String base, String symbolsCsv) {
        final FixerLatestResponse resp = getLatest(base, symbolsCsv);
        return resp != null && resp.rates() != null ? resp.rates() : Map.of();
    }

    public Map<String, String> getCurrencies() {
        final FixerSymbolsResponse resp = restClient.get()
                .uri(builder -> builder.path(CurrencyUrls.FIXER_SYMBOLS_URL)
                        .queryParam(ACCESS_KEY, props.fixerAccessKey())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(FixerSymbolsResponse.class);
        return resp != null && resp.symbols() != null ? resp.symbols() : Map.of();
    }

    public Map<String, Double> getHistoricalRates(String date, String base, String symbolsCsv) {
        final FixerLatestResponse resp = getHistorical(date, base, symbolsCsv);
        return resp != null && resp.rates() != null ? resp.rates() : Map.of();
    }

    private FixerLatestResponse getHistorical(String date, String base, String symbolsCsv) {
        final MultiValueMap<String, String> q = buildSymbolParams(base, symbolsCsv);

        return restClient.get()
                .uri(builder -> builder
                        .path(CurrencyUrls.FIXER_BASE_URL + date)
                        .queryParams(q)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(FixerLatestResponse.class);
    }

    public FixerConvertResponse convert(@NonNull final String from,
                                        @NonNull final String to,
                                        @NonNull final BigDecimal amount,
                                        @NonNull final String date
    ) {
        final MultiValueMap<String, String> q = buildConvertQueryParams(from, to, amount, date);

        return restClient.get()
                .uri(builder -> builder.path(CurrencyUrls.FIXER_CONVERT_URL)
                        .queryParams(q)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(FixerConvertResponse.class);
    }

    private MultiValueMap<String, String> buildConvertQueryParams(final String from, final String to, final BigDecimal amount, final String date) {
        final MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add(ACCESS_KEY, props.fixerAccessKey());
        q.add("from", from);
        q.add("to", to);
        q.add("amount", amount.toPlainString());
        if (!date.isBlank()) {
            q.add("date", date);
        }
        return q;
    }
}
