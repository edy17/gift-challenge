package org.diehl.wedoogift.domain.service.utils;

import java.time.format.DateTimeFormatter;

public final class JsonUtil {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JsonUtil() {
        throw new UnsupportedOperationException();
    }
}
