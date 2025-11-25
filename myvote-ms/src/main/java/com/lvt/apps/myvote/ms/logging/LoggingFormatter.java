package com.lvt.apps.myvote.ms.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingFormatter {

    private static final String HEADER_START = "\n\t";
    private static final String KEY_VALUE_START = "\n\t\t";
    private static final String KEY_VALUE_SEPARATOR = ": ";
    private static final String NULL_OR_EMPTY = "[null or empty]";

    /**
     * Generate standardized request header text for logging
     * @param externalSystem system name
     * @return standardized request header text
     */
    public static String generateRequestHeadingText(String externalSystem) {
        return HEADER_START + (StringUtils.isNotEmpty(externalSystem) ? externalSystem.toUpperCase() : "SYSTEM") + " request:";
    }

    /**
     * Generate standardized response header text for logging
     * @param externalSystem system name
     * @return standardized response header text
     */
    public static String generateResponseHeadingText(String externalSystem) {
        return HEADER_START + (StringUtils.isNotEmpty(externalSystem) ? externalSystem.toUpperCase() : "SYSTEM") + " response:" ;
    }

    /**
     * Generate standardized key/value text for logging
     * @param name key name
     * @param value key value
     * @return standardized key/value text
     */
    public static String formatNameValue(String name, String value) {
        return KEY_VALUE_START + name + KEY_VALUE_SEPARATOR + (StringUtils.isNotBlank(value) ? value : NULL_OR_EMPTY);
    }

}

