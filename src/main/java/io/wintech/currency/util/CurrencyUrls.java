package io.wintech.currency.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CurrencyUrls {
    public static final String CURRENCY_API_BASE_URL = "https://api.currencyapi.com/v3/";
    public static final String CURRENCY_CONVERT_URL = CURRENCY_API_BASE_URL + "convert";
    public static final String CURRENCIES_GET_URL = CURRENCY_API_BASE_URL + "currencies";
    public static final String HISTORICAL_GET_URL = CURRENCY_API_BASE_URL + "historical";
    public static final String CURRENCY_LATEST_URL = CURRENCY_API_BASE_URL + "latest";

    public static final String OPEN_EXCHANGE_BASE_URL = "https://openexchangerates.org/api/";
    public static final String OPEN_EXCHANGE_CURRENCIES_URL = OPEN_EXCHANGE_BASE_URL + "currencies.json";
    public static final String OPEN_EXCHANGE_RATES_URL = OPEN_EXCHANGE_BASE_URL + "latest.json";

    public static final String FIXER_BASE_URL = "https://data.fixer.io/api/";
    public static final String FIXER_LATEST_URL = FIXER_BASE_URL + "latest";
    public static final String FIXER_CONVERT_URL = FIXER_BASE_URL + "convert";
    public static final String FIXER_SYMBOLS_URL = FIXER_BASE_URL + "symbols";


    public String openExchangeHistoricalUrl(String date) {
        return OPEN_EXCHANGE_BASE_URL + "historical/" + date + ".json";
    }
}