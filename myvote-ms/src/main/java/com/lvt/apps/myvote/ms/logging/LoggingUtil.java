package com.lvt.apps.myvote.ms.logging;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.HtmlUtils;

/**
 * Utility class for logging.
 */

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingUtil {

    /**
     * In some scenarios, we need to add to our logs unsanitized user inputs, for example, when the user sends invalid
     * parameters as part of the API request.
     * <p>
     * This method allows us to encode the input to avoid the risk of log injection.
     */
    public static String createLogRepresentation(Object input) {
        if (input == null) {
            return null;
        }

        try {
            final String inputString = input.toString();
            String encoded = htmlEscape(inputString);

            if (!encoded.equals(inputString)) {
                encoded += "(encoded)";
            }

            return encoded;
        } catch (Exception e) {
            log.warn("unable to clean input string. Returning null");
            return null;
        }
    }

    private static String htmlEscape(String input) {
        if (input == null)  {
            return null;
        }

        final String clean = input.replaceAll("[\r\n\t]", " ");
        return HtmlUtils.htmlEscape(clean);
    }
}
