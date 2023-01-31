package api.maven.plugin.angular.client.utils;

import api.maven.plugin.core.model.ApiEnumModel;

import java.util.Locale;
import java.util.regex.Pattern;

public final class TypeScriptUtils {

    private static final Pattern CAPITAL_LETTER_PATTERN = Pattern.compile("[(A-Z)]");

    private TypeScriptUtils() {
        // No instances
    }

    public static String toLowerCaseName(String str) {
        var lowerCaseName = CAPITAL_LETTER_PATTERN.matcher(str)
                .replaceAll(matchResult -> "-" + matchResult.group(0).toLowerCase(Locale.ROOT));

        if (lowerCaseName.startsWith("-")) {
            lowerCaseName = lowerCaseName.substring(1);
        }

        return lowerCaseName;
    }
}
