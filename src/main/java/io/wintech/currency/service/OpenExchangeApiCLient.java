package io.wintech.currency.service;

import io.wintech.currency.config.CurrencyConfigProperties;
import io.wintech.currency.dto.LatestRateDto;
import io.wintech.currency.util.CurrencyUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static io.wintech.currency.util.CurrencyUrls.OPEN_EXCHANGE_CURRENCIES_URL;
import static io.wintech.currency.util.CurrencyUrls.OPEN_EXCHANGE_RATES_URL;

@RequiredArgsConstructor
@Component
public class OpenExchangeApiCLient {
    private static final String APP_ID = "?app_id=";
    private final RestClient restClient;
    private final CurrencyConfigProperties props;

    public Map<String, String> getCurrencies() {
        final String url = OPEN_EXCHANGE_CURRENCIES_URL + APP_ID + props.openExchangeAccessKey();
        return restClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public LatestRateDto getRate() {
        final String urlEndings = "&base=Optional&symbols=Optional&prettyprint=false&show_alternative=false";
        final String url = OPEN_EXCHANGE_RATES_URL + APP_ID + props.openExchangeAccessKey() + urlEndings;
        return restClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(LatestRateDto.class);
    }

    public LatestRateDto getHistoricalRate(final String date, final String base, final String symbolsCsv) {
        final StringBuilder url = new StringBuilder(CurrencyUrls.OPEN_EXCHANGE_BASE_URL)
                .append("historical/").append(date).append(".json")
                .append(APP_ID).append(props.openExchangeAccessKey());
        if (base != null && !base.isBlank()) {
            url.append("&base=").append(base);
        }
        if (symbolsCsv != null && !symbolsCsv.isBlank()) {
            url.append("&symbols=").append(symbolsCsv);
        }

        return restClient.get()
                .uri(url.toString())
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .retrieve()
                .body(LatestRateDto.class);
    }
}
