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
    private final String templatePath;
    private final List<FtlVariableModel> variables;

    public final String getModelClassName() {
        return className + "Model";
    }

    public String getTemplateIdentifier() {
        return CAMEL_CASE_PATTERN.matcher(className).replaceAll("$1_$2").toUpperCase();
    }
}
