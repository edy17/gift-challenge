package org.diehl.wedoogift.domain.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.diehl.wedoogift.domain.service.utils.JsonUtil;

import java.io.IOException;
import java.time.LocalDate;

public class CustomDateDeserializer extends StdDeserializer<LocalDate> {

    public CustomDateDeserializer() {
        this(null);
    }

    public CustomDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDate deserialize(
            JsonParser jsonparser, DeserializationContext context)
            throws IOException {
        String date = jsonparser.getText();
        return LocalDate.parse(date, JsonUtil.formatter);
    }
}
