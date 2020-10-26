package org.diehl.wedoogift.domain.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.diehl.wedoogift.domain.service.utils.JsonUtil;

import java.io.IOException;
import java.time.LocalDate;

public class CustomDateSerializer extends StdSerializer<LocalDate> {

    public CustomDateSerializer() {
        this(null);
    }

    public CustomDateSerializer(Class<LocalDate> t) {
        super(t);
    }

    @Override
    public void serialize(
            LocalDate value, JsonGenerator gen, SerializerProvider arg2)
            throws IOException {
        gen.writeString(JsonUtil.formatter.format(value));
    }
}
