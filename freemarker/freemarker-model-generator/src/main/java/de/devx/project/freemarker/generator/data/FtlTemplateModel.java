package de.devx.project.freemarker.generator.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@Data
@RequiredArgsConstructor
public class FtlTemplateModel {

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([a-z])([A-Z]+)");

    private final String templateName;
    private final String className;
    private final String packageName;
    private final String groupDirectory;
    private final String templatePath;
    private final List<FtlVariableModel> variables;

    public final String getModelClassName() {
        return className + "Model";
    }

    public String getTemplateIdentifier() {
        return CAMEL_CASE_PATTERN.matcher(className).replaceAll("$1_$2").toUpperCase();
    }

    public String getGroupClassName() {
        if (groupDirectory.isEmpty()) {
            return "Templates";
        }
        var sb = new StringBuilder();
        for (var segment : groupDirectory.split("[/\\\\]")) {
            for (var part : segment.split("[^a-zA-Z0-9]+")) {
                if (!part.isEmpty()) {
                    sb.append(Character.toUpperCase(part.charAt(0)));
                    sb.append(part.substring(1));
                }
            }
        }
        sb.append("Templates");
        return sb.toString();
    }
}
