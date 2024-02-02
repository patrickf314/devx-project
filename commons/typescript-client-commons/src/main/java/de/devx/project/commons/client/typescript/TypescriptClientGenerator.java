package de.devx.project.commons.client.typescript;

import de.devx.project.commons.client.typescript.data.*;
import de.devx.project.commons.client.typescript.mapper.TypeScriptTypeMapper;
import de.devx.project.commons.client.typescript.properties.TypeScriptClientGeneratorProperties;
import de.devx.project.commons.client.typescript.properties.TypeScriptDependency;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

public class TypescriptClientGenerator<P extends TypeScriptClientGeneratorProperties> {

    private static final TypeScriptTypeMapper MAPPER = Mappers.getMapper(TypeScriptTypeMapper.class);

    protected final SourceFileGenerator fileGenerator;
    protected final P properties;
    protected final Configuration configuration;

    protected final Set<String> generatedClasses = new HashSet<>();

    public TypescriptClientGenerator(SourceFileGenerator fileGenerator, P properties) {
        this.fileGenerator = fileGenerator;
        this.properties = properties;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(TypescriptClientGenerator.class.getClassLoader(), "templates");
    }

    public void generateEnum(TypeScriptEnumModel model) throws IOException {
        if (generatedClasses.contains(model.getClassName())) {
            return;
        }

        generatedClasses.add(model.getClassName());

        var packageName = properties.getPackageNameForClass(model.getClassName());

        processTemplate("enum-template.ts.ftl", packageName, model.getName(), Map.of("model", model));
    }

    public void generateDTO(TypeScriptDTOModel model) throws IOException {
        if (generatedClasses.contains(model.getClassName())) {
            return;
        }

        generatedClasses.add(model.getClassName());

        var packageName = properties.getPackageNameForClass(model.getClassName());
        var imports = resolveImports(packageName, model);

        processTemplate("dto-template.ts.ftl", packageName, model.getName(), Map.of("model", model, "imports", imports));
    }

    protected void processTemplate(String template, String packageName, String className, Map<String, Object> dataModel) throws IOException {
        try (var writer = fileGenerator.createSourceFile(packageName, className)) {
            configuration.getTemplate(template).process(dataModel, writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    protected Collection<TypeScriptImportModel> resolveImports(String currentPackage, TypeScriptDependency... dependencies) {
        var imports = new HashMap<String, TypeScriptImportModel>();
        Arrays.stream(dependencies)
                .filter(Objects::nonNull)
                .map(dependency -> {
                    var i = dependency.getPath().lastIndexOf('/');
                    var targetPackage = dependency.getPath().substring(0, i).replace('/', '.');
                    var fileName = dependency.getPath().substring(i + 1, dependency.getPath().lastIndexOf('.'));

                    var importPath = fileGenerator.importPath(currentPackage, targetPackage);
                    return new TypeScriptImportModel(importPath + "/" + fileName, Set.of(dependency.getIdentifier()));
                })
                .forEach(i -> addImport(i, imports));
        return imports.values();
    }

    protected void addImport(TypeScriptImportModel newImport, HashMap<String, TypeScriptImportModel> imports) {
        var existingImport = imports.get(newImport.getPath());
        if (existingImport == null) {
            imports.put(newImport.getPath(), newImport);
        } else {
            var identifiers = new HashSet<>(existingImport.getIdentifiers());
            identifiers.addAll(newImport.getIdentifiers());
            existingImport.setIdentifiers(identifiers);
        }
    }

    protected List<TypeScriptImportModel> importModelsForType(String currentPackage, TypeScriptTypeModel typeModel, String currentClassName) {
        return typeModel.getDependentClassNames()
                .stream()
                .filter(not(currentClassName::equals))
                .map(className -> importModelForClassName(currentPackage, className))
                .filter(Objects::nonNull)
                .toList();
    }

    private TypeScriptImportModel importModelForClassName(String currentPackage, String className) {
        var alias = properties.getTypeAliases().stream().filter(a -> a.getClassName().equals(className)).findAny();
        if (alias.isPresent()) {
            if (alias.get().getPath() == null) {
                return null;
            }

            return new TypeScriptImportModel(alias.get().getPath(), Set.of(alias.get().getType()));
        }

        var targetPackage = properties.getPackageNameForClass(className);
        var name = className.substring(className.lastIndexOf('.') + 1);

        var importPath = fileGenerator.importPath(currentPackage, targetPackage);
        var fileName = fileGenerator.fileName(name);
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        return new TypeScriptImportModel(importPath + "/" + fileName, Set.of(name));
    }

    private Collection<TypeScriptImportModel> resolveImports(String currentPackage, TypeScriptDTOModel model) {
        var imports = new HashMap<String, TypeScriptImportModel>();
        if (model.getExtendedDTO() != null) {
            importModelsForType(currentPackage, model.getExtendedDTO(), model.getClassName())
                    .forEach(i -> addImport(i, imports));
        }

        model.getFields()
                .stream()
                .map(TypeScriptDTOFieldModel::getType)
                .map(type -> importModelsForType(currentPackage, type, model.getClassName()))
                .flatMap(List::stream)
                .forEach(i -> addImport(i, imports));

        return imports.values();
    }
}
