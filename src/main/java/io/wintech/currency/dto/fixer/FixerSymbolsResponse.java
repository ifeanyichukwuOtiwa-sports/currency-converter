package io.wintech.currency.dto.fixer;

import java.util.Map;

public record FixerSymbolsResponse(
        boolean success,
        Map<String, String> symbols
) {}
