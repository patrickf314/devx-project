package de.devx.project.freemarker.generator.parser;

import de.devx.project.freemarker.generator.data.FtlTemplateModel;
import de.devx.project.freemarker.generator.data.FtlVariableModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FtlTemplateParser {

    private static final Pattern FTL_VARIABLE_PATTERN =
            Pattern.compile("<#--\\s*@ftlvariable\\s+name=\"([^\"]+)\"\\s+type=\"([^\"]+)\"\\s*-->");

    public List<FtlTemplateModel> parseTemplates(Path resourcesDirectory, String packageName) throws IOException {
        return parseTemplates(resourcesDirectory, resourcesDirectory, packageName);
    }

    public List<FtlTemplateModel> parseTemplates(Path resourcesDirectory, Path scanDirectory, String packageName) throws IOException {
        if (!Files.isDirectory(scanDirectory)) {
            return Collections.emptyList();
        }

        var result = new ArrayList<FtlTemplateModel>();
        try (var stream = Files.walk(scanDirectory)) {
            var ftlFiles = stream
                    .filter(path -> path.toString().endsWith(".ftl"))
                    .toList();
            for (var ftlFile : ftlFiles) {
                result.add(parseTemplate(resourcesDirectory, ftlFile, packageName));
            }
        }
        return result;
    }

    private FtlTemplateModel parseTemplate(Path resourcesDirectory, Path ftlFile, String packageName) throws IOException {
        var content = Files.readString(ftlFile);
        var variables = new ArrayList<FtlVariableModel>();
        var seenNames = new LinkedHashSet<String>();

        var matcher = FTL_VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            var name = matcher.group(1);
            var fqType = matcher.group(2);
            if (seenNames.add(name)) {
                variables.add(parseVariable(name, fqType));
            }
        }

        var fileName = ftlFile.getFileName().toString();
        var templateName = fileName.substring(0, fileName.lastIndexOf('.'));
        var className = toClassName(templateName);
        var templatePath = resourcesDirectory.relativize(ftlFile).toString().replace('\\', '/');

        return new FtlTemplateModel(templateName, className, packageName, templatePath, variables);
    }

    private FtlVariableModel parseVariable(String name, String fqType) {
        var imports = new LinkedHashSet<String>();
        var simpleType = resolveSimpleType(fqType.trim(), imports);
        return new FtlVariableModel(name, simpleType, new ArrayList<>(imports));
    }

    private String resolveSimpleType(String fqType, Set<String> imports) {
        if (fqType.contains("<")) {
            var angleStart = fqType.indexOf('<');
            var outerType = fqType.substring(0, angleStart).trim();
            var innerPart = fqType.substring(angleStart + 1, fqType.lastIndexOf('>')).trim();

            var simpleOuter = getSimpleName(outerType, imports);
            var innerArgs = splitGenericArgs(innerPart);
            var simpleInnerArgs = innerArgs.stream()
                    .map(arg -> resolveSimpleType(arg.trim(), imports))
                    .collect(Collectors.joining(", "));

            return simpleOuter + "<" + simpleInnerArgs + ">";
        } else if (fqType.endsWith("[]")) {
            return resolveSimpleType(fqType.substring(0, fqType.length() - 2), imports) + "[]";
        } else {
            return getSimpleName(fqType, imports);
        }
    }

    private String getSimpleName(String fqType, Set<String> imports) {
        if (!fqType.contains(".")) {
            return fqType;
        }
        if (fqType.startsWith("java.lang.")) {
            return fqType.substring("java.lang.".length());
        }
        imports.add(fqType);
        return fqType.substring(fqType.lastIndexOf('.') + 1);
    }

    private List<String> splitGenericArgs(String argsStr) {
        var args = new ArrayList<String>();
        var depth = 0;
        var start = 0;
        for (var i = 0; i < argsStr.length(); i++) {
            var c = argsStr.charAt(i);
            if (c == '<') depth++;
            else if (c == '>') depth--;
            else if (c == ',' && depth == 0) {
                args.add(argsStr.substring(start, i).trim());
                start = i + 1;
            }
        }
        if (start < argsStr.length()) {
            args.add(argsStr.substring(start).trim());
        }
        return args;
    }

    private String toClassName(String templateName) {
        var name = templateName;
        var dotIndex = name.indexOf('.');
        if (dotIndex >= 0) {
            name = name.substring(0, dotIndex);
        }
        var parts = name.split("[^a-zA-Z0-9]+");
        var sb = new StringBuilder();
        for (var part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
            }
        }
        return sb.isEmpty() ? "Unknown" : sb.toString();
    }
}
