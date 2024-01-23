package de.devx.project.commons.client.typescript;

import de.devx.project.commons.client.typescript.data.TypeScriptDTOModel;
import de.devx.project.commons.client.typescript.data.TypeScriptEnumModel;
import de.devx.project.commons.client.typescript.io.TypeScriptPackageAlias;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TypescriptClientGenerator {

    protected final SourceFileGenerator fileGenerator;
    protected final Configuration configuration;
    private final List<TypeScriptPackageAlias> packageAliases;
    private final String defaultPackage;

    public TypescriptClientGenerator(SourceFileGenerator fileGenerator, List<TypeScriptPackageAlias> packageAliases, String defaultPackage) {
        this.fileGenerator = fileGenerator;
        this.packageAliases = packageAliases;
        this.defaultPackage = defaultPackage;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(TypescriptClientGenerator.class.getClassLoader(), "templates");
    }

    public void generateEnum(TypeScriptEnumModel model) throws IOException {
        var packageName = extractPackageNameFromClassName(model.getClassName());

        try (var writer = fileGenerator.createSourceFile(packageName, model.getName())) {
            var template = configuration.getTemplate("enum-template.ts.ftl");
            template.process(Map.of("model", model), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    public void generateDTO(TypeScriptDTOModel model) throws IOException {
        var packageName = extractPackageNameFromClassName(model.getClassName());

        try (var writer = fileGenerator.createSourceFile(packageName, model.getName())) {
            var template = configuration.getTemplate("dto-template.ts.ftl");
            template.process(Map.of("model", model), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    public String extractPackageNameFromClassName(String className) {
        var i = className.lastIndexOf('.');
        if (i == -1) {
            throw new IllegalArgumentException("Invalid class name " + className);
        }

        var packageName = className.substring(0, i);
        for (var packageAlias : packageAliases) {
            if (packageName.startsWith(packageAlias.packagePrefix() + ".")) {
                return (packageAlias.alias() == null ? "" : packageAlias.alias() + ".") + packageName.substring(packageAlias.packagePrefix().length() + 1);
            }
        }

        return defaultPackage == null ? packageName : defaultPackage;
    }
}
