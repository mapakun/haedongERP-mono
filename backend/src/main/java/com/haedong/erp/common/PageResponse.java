package com.haedong.erp.common;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        long totalCount,
        int page,
        int size
) {
}