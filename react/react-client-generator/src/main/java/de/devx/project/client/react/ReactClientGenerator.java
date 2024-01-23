package de.devx.project.client.react;

import de.devx.project.commons.client.typescript.TypescriptClientGenerator;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceModel;
import de.devx.project.commons.client.typescript.io.TypeScriptPackageAlias;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ReactClientGenerator extends TypescriptClientGenerator {

    public ReactClientGenerator(SourceFileGenerator fileGenerator,
                                List<TypeScriptPackageAlias> packageAliases,
                                String defaultPackage) {
        super(fileGenerator, packageAliases, defaultPackage);
    }

    public void generateService(TypeScriptServiceModel model) throws IOException {
        var packageName = extractPackageNameFromClassName(model.getClassName());

        try (var writer = fileGenerator.createSourceFile(packageName, model.getName())) {
            var template = configuration.getTemplate("react-service-template.ts.ftl");
            template.process(Map.of("model", model), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }
}
