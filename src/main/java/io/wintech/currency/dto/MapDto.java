package io.wintech.currency.dto;

import java.util.Map;

public record MapDto<T>(
        Map<String, T> data
) {
}
