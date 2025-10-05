package io.wintech.currency.service.provider.chain;

import io.wintech.currency.dto.MapDto;
import io.wintech.currency.dto.currency.ConvertResponse;
import io.wintech.currency.dto.currency.CurrencyDataDto;
import io.wintech.currency.dto.currency.GetCurrencyDto;
import io.wintech.currency.service.CurrencyAPIClient;
import io.wintech.currency.service.provider.CurrencyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
class CurrencyApiProvider implements CurrencyProvider {
    private static final String CURRENCY_API = "CURRENCY_API";
    private final CurrencyAPIClient client;


    @Override
    public String provider() {
        return CURRENCY_API;
    }

    @Override
    public Optional<Map<String, String>> getCurrencies() {
        try {
            final MapDto<GetCurrencyDto> currencies = client.getCurrencies();
            if (currencies != null && !CollectionUtils.isEmpty(currencies.data())) {
                final Map<String, String> resp = currencies.data()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                            final GetCurrencyDto value = entry.getValue();
                            return value.name();
                        }));
                log.info("Fetched {} currencies from Currency API", resp.size());
                return Optional.of(resp);
            }
        } catch (final Exception e) {
            log.warn("Failed to get currencies from Currency API", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> convert(final String from, final String to, final BigDecimal amount) {
        try {
            final ConvertResponse resp = client.convertCurrency(from, amount, to);
            if (resp == null || CollectionUtils.isEmpty(resp.data())) {
                return Optional.empty();
            }
            final CurrencyDataDto currencyResponse = resp.data().get(to);
            if (currencyResponse == null || currencyResponse.value() == null) {
                return Optional.empty();
            }

            return Optional.of(Map.of(
                    "from", from,
                    "to", to,
                    "result", currencyResponse.value()
            ));

        } catch (final Exception e) {
            log.warn("Failed to convert currencies from Currency API", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> historical(final String date, final String base, final String symbolsCsv) {
        try {
            final ConvertResponse resp = client.historical(LocalDate.parse(date), base, symbolsCsv);
            if (resp == null || CollectionUtils.isEmpty(resp.data())) {
                return Optional.empty();
            }

            final Map<String, String> data = resp.data().entrySet()
                    .stream()
                    .filter(e -> e.getValue() != null && e.getValue().value() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().value()));

            return Optional.of(data);
        } catch (final Exception e) {
            log.warn("Failed to get historical currencies from Currency API", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> latest(final String base, final String symbolsCsv) {
        try {
            final ConvertResponse latest = client.latest(base, symbolsCsv);
            if (latest == null || CollectionUtils.isEmpty(latest.data())) {
                return Optional.empty();
            }

            final Map<String, String> result = latest.data().entrySet()
                    .stream()
                    .filter(e -> e.getValue() != null && e.getValue().value() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().value()));

            return Optional.ofNullable(result);
        } catch (final Exception e) {
            log.warn("CurrencyAPI latest failed: base={}, symbols={}", base, symbolsCsv, e);
        }
        return Optional.empty();
    }

    @Override
    public Integer order() {
        return 100;
    }
}
