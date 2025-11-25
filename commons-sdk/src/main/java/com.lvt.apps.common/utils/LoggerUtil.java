package com.lvt.apps.common.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LoggerUtil {

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[\\r\\n]", "_");
    }
}
