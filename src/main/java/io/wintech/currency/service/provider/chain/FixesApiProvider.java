package io.wintech.currency.service.provider.chain;

import io.wintech.currency.dto.MapDto;
import io.wintech.currency.dto.currency.GetCurrencyDto;
import io.wintech.currency.dto.fixer.FixerConvertResponse;
import io.wintech.currency.dto.fixer.FixerLatestResponse;
import io.wintech.currency.service.CurrencyAPIClient;
import io.wintech.currency.service.FixerApiClient;
import io.wintech.currency.service.provider.CurrencyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
class FixesApiProvider implements CurrencyProvider {
    private static final String FIXER_API = "FIXER_API";
    private final FixerApiClient client;


    @Override
    public String provider() {
        return FIXER_API;
    }

    @Override
    public Optional<Map<String, String>> getCurrencies() {
        try {
            final Map<String, String> currencies = client.getCurrencies();
            if (!CollectionUtils.isEmpty(currencies)) {
                return Optional.of(currencies);
            }
        } catch (final Exception e) {
            log.warn("Failed to get currencies from Fixers API", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> convert(final String from, final String to, final BigDecimal amount) {
        try {
            final FixerConvertResponse resp = client.convert(from, to, amount, "");
            if (resp == null || resp.result() == null) {
                return Optional.empty();
            }
            return Optional.of(Map.of(
                    "from", resp.query().from(),
                    "to", resp.query().to(),
                    "result", resp.result().toString()
            ));
        } catch (final Exception e) {
            log.warn("Fixer convert failed: {} -> {} amount {}", from, to, amount, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> historical(final String date, final String base, final String symbolsCsv) {
        try {
            final Map<String, Double> result = client.getHistoricalRates(date, base, symbolsCsv);
            if (CollectionUtils.isEmpty(result)) {
                return Optional.empty();
            }
            final Map<String, String> resp = result.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

            return Optional.of(resp);
        } catch (final Exception e) {
            log.warn("Failed to get historical currencies from Fixers API", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> latest(final String base, final String symbolsCsv) {
        try {
            final FixerLatestResponse latest = client.getLatest(base, symbolsCsv);
            if (emptyResponse(latest)) {
                return Optional.empty();
            }

            final Map<String, String> resp = latest.rates()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

            return Optional.of(resp);

        } catch (final Exception e) {
            log.warn("Fixer latest failed: base={}, symbols={}", base, symbolsCsv, e);
        }
        return Optional.empty();
    }

    private static boolean emptyResponse(final FixerLatestResponse latest) {
        return latest == null || CollectionUtils.isEmpty(latest.rates());
    }

    @Override
    public Integer order() {
        return 200;
    }
}
