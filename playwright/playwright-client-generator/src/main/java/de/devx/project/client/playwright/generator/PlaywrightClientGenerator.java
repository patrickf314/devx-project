package de.devx.project.client.playwright.generator;

import de.devx.project.client.playwright.generator.data.PlaywrightServiceInfoModel;
import de.devx.project.client.playwright.generator.properties.PlaywrightClientGeneratorProperties;
import de.devx.project.commons.client.typescript.TypescriptClientGenerator;
import de.devx.project.commons.client.typescript.data.TypeScriptImportModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodParameterModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceModel;
import de.devx.project.commons.client.typescript.properties.TypeScriptDependency;
import de.devx.project.commons.generator.io.SourceFileGenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PlaywrightClientGenerator extends TypescriptClientGenerator<PlaywrightClientGeneratorProperties> {

    private static final String SERVICE_COMMONS = "PlaywrightServiceCommons";
    private static final String SERVICE_INFO = "ServiceInfo";

    public PlaywrightClientGenerator(SourceFileGenerator fileGenerator, PlaywrightClientGeneratorProperties properties) {
        super(fileGenerator, properties);
    }

    public void generateService(TypeScriptServiceModel model) throws IOException {
        if (generatedClasses.contains(model.getClassName())) {
            return;
        }

        generatedClasses.add(model.getClassName());

        var packageName = properties.getPackageNameForClass(model.getClassName());
        var imports = resolveImports(packageName, model, properties.getHttpHeaderCustomizer(), properties.getTestContext(),
                new TypeScriptDependency("url", utilityPackage() + "/playwright-service-commons.ts"),
                new TypeScriptDependency("mapJsonResponse", utilityPackage() + "/playwright-service-commons.ts"),
                new TypeScriptDependency("mapVoidResponse", utilityPackage() + "/playwright-service-commons.ts"),
                new TypeScriptDependency("mapStringResponse", utilityPackage() + "/playwright-service-commons.ts"),
                new TypeScriptDependency("mapStreamingResponse", utilityPackage() + "/playwright-service-commons.ts")
        );

        processTemplate("playwright-service-template.ts.ftl", packageName, model.getName(), Map.of(
                "model", model,
                "imports", imports,
                "testContextIdentifier", properties.getTestContext() == null ? "" : properties.getTestContext().getIdentifier(),
                "prepareHeadersIdentifier", properties.getHttpHeaderCustomizer() == null ? "" : properties.getHttpHeaderCustomizer().getIdentifier()
        ));
    }

    public void generateUtilities() throws IOException {
        var packageName = utilityPackage().replace('/', '.');
        if (!generatedClasses.contains(packageName + "." + SERVICE_COMMONS)) {
            generatedClasses.add(packageName + "." + SERVICE_COMMONS);
            processTemplate("playwright-service-commons.ts.ftl", packageName, SERVICE_COMMONS, Map.of());
        }
    }

    public void generateServiceInfo(List<TypeScriptServiceModel> services) throws IOException {
        var packageName = utilityPackage().replace('/', '.');
        if (!generatedClasses.contains(packageName + "." + SERVICE_INFO)) {
            generatedClasses.add(packageName + "." + SERVICE_INFO);
            processTemplate("playwright-service-index.ts.ftl", packageName, SERVICE_INFO, Map.of(
                    "services", services.stream().map(service -> this.mapToServiceInfo(packageName, service)).toList()
            ));
        }
    }

    private PlaywrightServiceInfoModel mapToServiceInfo(String currentPackage, TypeScriptServiceModel model) {
        var targetPackage = properties.getPackageNameForClass(model.getClassName());

        var name = model.getName();
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

        var fileName = fileGenerator.fileName(model.getName());
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        var info = new PlaywrightServiceInfoModel();
        info.setType(model.getName());
        info.setPath(fileGenerator.importPath(currentPackage, targetPackage) + "/" + fileName);
        info.setName(name);

        return info;
    }

    private String utilityPackage() {
        return properties.getDefaultPackageAlias() == null ? "commons" : properties.getDefaultPackageAlias();
    }

    private Collection<TypeScriptImportModel> resolveImports(String currentPackage, TypeScriptServiceModel model, TypeScriptDependency... dependencies) {
        var imports = new HashMap<String, TypeScriptImportModel>();
        resolveImports(currentPackage, dependencies).forEach(i -> addImport(i, imports));
        model.getMethods()
                .stream()
                .map(method -> importModelsForMethod(currentPackage, method, model.getClassName()))
                .flatMap(List::stream)
                .forEach(i -> addImport(i, imports));
        return imports.values();
    }

    private List<TypeScriptImportModel> importModelsForMethod(String currentPackage, TypeScriptServiceMethodModel model, String currentClassName) {
        return Stream.concat(
                model.getParameters()
                        .stream()
                        .map(TypeScriptServiceMethodParameterModel::getType)
                        .map(type -> importModelsForType(currentPackage, type, currentClassName))
                        .flatMap(List::stream),
                importModelsForType(currentPackage, model.getReturnType(), currentClassName).stream()
        ).toList();
    }
}
