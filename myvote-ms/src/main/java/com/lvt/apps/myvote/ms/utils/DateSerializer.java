package com.lvt.apps.myvote.ms.utils;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.optum.ofsc.hba.commonssdk.constants.ValidationMessageConstants;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Class defined to provide custom deserialization and validation for Date types. */
public class YyyyMmDdDateDeserializer extends JsonDeserializer<LocalDate> {
  @Override
  public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String fieldName = p.getParsingContext().getCurrentName();
    String value = p.getText();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ValidationMessageConstants.JSON_DATE_FORMAT_YYYY_MM_DD);
    LocalDate date = null;
    if (Objects.nonNull(value)) {
      try {
        date = LocalDate.parse(value, formatter);
      } catch (Exception e) {
        throw new InvalidFormatException(
            p, ValidationMessageConstants.fieldsErrorMsg.get(fieldName), value, LocalDate.class);
      }
    }
    return date;
  }
}
