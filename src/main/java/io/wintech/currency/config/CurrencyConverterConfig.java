package io.wintech.currency.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CurrencyConfigProperties.class)
public class CurrencyConverterConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
