package io.wintech.currency.service;

import io.wintech.currency.util.CurrencyUrls;
import io.wintech.currency.config.CurrencyConfigProperties;
import io.wintech.currency.dto.currency.ConvertResponse;
import io.wintech.currency.dto.currency.GetCurrencyDto;
import io.wintech.currency.dto.MapDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CurrencyAPIClient {
    private static final String API_KEY = "apikey";

    private final RestClient restClient;
    private final CurrencyConfigProperties props;

    public MapDto<GetCurrencyDto> getCurrencies() {
        return restClient.get()
                .uri(CurrencyUrls.CURRENCIES_GET_URL)
                .header(API_KEY, props.currencyAccessKey())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public ConvertResponse convertCurrency(final String from, final BigDecimal value, final String to) {
        final String url = CurrencyUrls.CURRENCY_CONVERT_URL
                           + "?value=" + URLEncoder.encode(value.toPlainString(), StandardCharsets.UTF_8)
                           + "&base_currency=" + URLEncoder.encode(from, StandardCharsets.UTF_8)
                           + "&currencies=" + URLEncoder.encode(to, StandardCharsets.UTF_8);
        return restClient.get()
                .uri(url)
                .header(API_KEY, props.currencyAccessKey())
                .retrieve()
                .body(ConvertResponse.class);
    }

    public ConvertResponse historical(final LocalDate date, final String baseCurrency, final String symbolsCsv) {
        final var url = new StringBuilder(CurrencyUrls.HISTORICAL_GET_URL)
                .append("?date=").append(URLEncoder.encode(date.toString(), StandardCharsets.UTF_8));

        Optional.ofNullable(baseCurrency)
                .filter(b -> !b.isBlank())
                .ifPresent(b -> url.append("&base_currency=").append(URLEncoder.encode(b, StandardCharsets.UTF_8)));

        Optional.ofNullable(symbolsCsv)
                .filter(s -> !s.isBlank())
                .ifPresent(s -> url.append("&currencies=").append(URLEncoder.encode(s, StandardCharsets.UTF_8)));


        return restClient.get()
                .uri(url.toString())
                .header(API_KEY, props.currencyAccessKey())
                .retrieve()
                .body(ConvertResponse.class);
    }

    public ConvertResponse rateHistoryToday(final String baseCurrency) {
        final LocalDateTime now = LocalDateTime.now();
        return historical(now.toLocalDate(), baseCurrency, "");
    }

    public ConvertResponse latest(final String baseCurrency, final String symbolsCsv) {
        final var url = new StringBuilder(CurrencyUrls.CURRENCY_LATEST_URL);

        Optional.ofNullable(baseCurrency)
                .filter(b -> !b.isBlank())
                .ifPresent(b -> url.append("?base_currency=").append(URLEncoder.encode(b, StandardCharsets.UTF_8)));
        Optional.ofNullable(symbolsCsv)
                .filter(s -> !s.isBlank())
                .ifPresent(s -> url.append("&currencies=").append(URLEncoder.encode(s, StandardCharsets.UTF_8)));

        return restClient.get()
                .uri(url.toString())
                .header(API_KEY, props.currencyAccessKey())
                .retrieve()
                .body(ConvertResponse.class);
    }
}
