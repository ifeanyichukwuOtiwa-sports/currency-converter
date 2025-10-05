package io.wintech.currency.service.provider.chain;

import io.wintech.currency.dto.LatestRateDto;
import io.wintech.currency.service.OpenExchangeApiCLient;
import io.wintech.currency.service.provider.CurrencyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
class OpenExchangeApiProvider implements CurrencyProvider {
    private static final String OPEN_EXCHANGE = "OPEN_EXCHANGE";
    private final OpenExchangeApiCLient client;


    @Override
    public String provider() {
        return OPEN_EXCHANGE;
    }

    @Override
    public Optional<Map<String, String>> getCurrencies() {
        try {
            final Map<String, String> currencies = client.getCurrencies();
            if (!CollectionUtils.isEmpty(currencies)) {
                return Optional.of(currencies);
            }
        } catch (final Exception e) {
            log.warn("Failed to get currencies from Open Exchange API", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> convert(final String from, final String to, final BigDecimal amount) {
        try {
            final LatestRateDto rate = client.getRate();
            if (rate == null || CollectionUtils.isEmpty(rate.rates())) {
                return Optional.empty();
            }
            final Double fromRate = rate.rates().get(from);
            final Double toRate = rate.rates().get(to);

            if (fromRate == null || toRate == null) {
                return Optional.empty();
            }

            final BigDecimal result = amount
                    .multiply(BigDecimal.valueOf(toRate))
                    .divide(BigDecimal.valueOf(fromRate), 10, RoundingMode.HALF_UP);

            return Optional.of(Map.of(
                    "from", from,
                    "to", to,
                    "result", result.stripTrailingZeros().toPlainString()
            ));
        } catch (final Exception e) {
            log.warn("OpenExchange convert failed: {} -> {} amount {}", from, to, amount, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> historical(final String date, final String base, final String symbolsCsv) {
        try {
            final LatestRateDto resp = client.getHistoricalRate(date, base, symbolsCsv);
            if (resp == null || CollectionUtils.isEmpty(resp.rates())) {
                return Optional.empty();
            }
            final Map<String, String> rates = resp.rates().entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> String.valueOf(e.getValue())
                    ));
            return Optional.of(rates);
        } catch (final Exception e) {
            log.warn("OpenExchange historical failed: date={}, base={}, symbols={}", date, base, symbolsCsv, e);        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> latest(final String base, final String symbolsCsv) {
        return Optional.of(Map.of());
    }

    @Override
    public Integer order() {
        return 300;
    }
}
