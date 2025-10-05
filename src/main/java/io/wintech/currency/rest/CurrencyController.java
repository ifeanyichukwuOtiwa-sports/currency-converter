package io.wintech.currency.rest;

import io.wintech.currency.rest.response.ConvertRequest;
import io.wintech.currency.rest.response.RateHistoryRequest;
import io.wintech.currency.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/currency")
@RestController
public class CurrencyController {
    private final CurrencyService service;


    @PostMapping("/convert")
    public Map<String, String> convertCurrency(@RequestBody final ConvertRequest request) {
        return service.convert(request.from(), request.to(), request.amount());
    }

    @GetMapping("/currencies")
    public Map<String, String> getCurrencies() {
        return service.getCurrencies();
    }

    @GetMapping("/rate-history")
    public Map<String, String> getRateHistory(@RequestBody final RateHistoryRequest request) {
        return service.getHistorical(request.date(), request.base(), request.symbols());
    }

    @GetMapping("/rates")
    public Map<String, String> getRates(final @RequestParam(required = true) String base,
                                        final @RequestParam(required = true) String symbols) {
        return service.getLatest(base, symbols);
    }
}
