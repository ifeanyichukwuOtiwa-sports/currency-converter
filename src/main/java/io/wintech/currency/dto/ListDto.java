package io.wintech.currency.dto;

import java.util.List;

public record ListDto<T>(
        List<T> data
) {
}
