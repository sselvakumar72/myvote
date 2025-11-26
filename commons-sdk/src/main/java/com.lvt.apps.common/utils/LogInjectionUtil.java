package com.lvt.apps.common.utils;

import lombok.experimental.UtilityClass;
import java.util.regex.Pattern;

@UtilityClass
public class LogInjectionUtil {
    private static final Pattern LOG_INJECTION_PATTERN = Pattern.compile("(?i)(\\n|\\r|%0a|%0d)");

    public static boolean containsLogInjection(String value) {
        if (value == null) {
            return false;
        }
        return LOG_INJECTION_PATTERN.matcher(value).find();
    }
}
