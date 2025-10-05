package io.wintech.currency.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "currency")
public record CurrencyConfigProperties(
        String currencyAccessKey,
        String openExchangeAccessKey,
        String fixerAccessKey
) {
}
