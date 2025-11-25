package com.lvt.apps.common.utils;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Class defined to provide custom deserialization and validation for Date types. */
public class DateSerializer extends JsonDeserializer<LocalDate> {
  @Override
  public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String fieldName = p.getParsingContext().getCurrentName();
    String value = p.getText();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date = null;
    if (Objects.nonNull(value)) {
      try {
        date = LocalDate.parse(value, formatter);
      } catch (Exception e) {
        throw new InvalidFormatException(
            p, "Invalid date format for field: " + fieldName, value, LocalDate.class);
      }
    }
    return date;
  }
}
