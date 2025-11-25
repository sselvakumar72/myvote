package com.lvt.apps.myvote.ms.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.lvt.apps.myvote.ms.dtos.EmptyObject;

import java.io.IOException;
import java.io.Serial;

/**
 * Serializer used in conjunction with DefaultDataDTO to output
 * empty curly braces in the json output if an object is null
 */
public class EmptyObjectSerializer extends StdSerializer<EmptyObject> {

    @Serial
    private static final long serialVersionUID = 33L;

    public EmptyObjectSerializer() {
        this(null);
    }

    protected EmptyObjectSerializer(Class<EmptyObject> t) {
        super(t);
    }

    @Override
    public void serialize(EmptyObject defaultDataDTO, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeEndObject();
    }
}
