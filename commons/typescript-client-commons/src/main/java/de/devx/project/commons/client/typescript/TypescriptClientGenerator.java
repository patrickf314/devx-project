package de.devx.project.commons.client.typescript;

import de.devx.project.commons.client.typescript.data.*;
import de.devx.project.commons.client.typescript.properties.TypeScriptClientGeneratorProperties;
import de.devx.project.commons.client.typescript.properties.TypeScriptDependency;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class TypescriptClientGenerator<P extends TypeScriptClientGeneratorProperties> {

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

        if (properties.isGenerateZodSchemas()) {
            processTemplate("enum-zod-schema-template.ts.ftl", packageName, model.getName() + "Schema", Map.of("model", model));
        }
    }

    public void generateBrandedType(TypeScriptBrandedTypeModel model) throws IOException {
        if (generatedClasses.contains(model.getClassName())) {
            return;
        }

        generatedClasses.add(model.getClassName());

        var packageName = properties.getPackageNameForClass(model.getClassName());

        var typeTemplateData = new HashMap<String, Object>();
        typeTemplateData.put("model", model);
        if (properties.isGenerateZodSchemas()) {
            var schemaFileName = fileGenerator.fileName(model.getName() + "Schema", false);
            typeTemplateData.put("schemaImportPath", fileGenerator.importPath(packageName, packageName) + "/" + schemaFileName);
        }
        processTemplate("branded-type-template.ts.ftl", packageName, model.getName(), typeTemplateData);

        if (properties.isGenerateZodSchemas()) {
            processTemplate("branded-type-zod-schema-template.ts.ftl", packageName, model.getName() + "Schema", Map.of("model", model));
        }
    }

    public void generateDTO(TypeScriptDTOModel model) throws IOException {
        if (generatedClasses.contains(model.getClassName())) {
            return;
        }

        generatedClasses.add(model.getClassName());

        var packageName = properties.getPackageNameForClass(model.getClassName());
        var imports = resolveImports(packageName, model);

        processTemplate("dto-template.ts.ftl", packageName, model.getName(), Map.of("model", model, "imports", imports));

        if (properties.isGenerateZodSchemas() && (model.getTypeArguments() == null || model.getTypeArguments().isEmpty())) {
            var zodImports = resolveZodImports(packageName, model);
            processTemplate("dto-zod-schema-template.ts.ftl", packageName, model.getName() + "Schema", Map.of("model", model, "imports", zodImports));
        }
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
                    if (dependency.getDependency() != null) {
                        return new TypeScriptImportModel(dependency.getDependency(), Set.of(dependency.getIdentifier()));
                    }

                    var path = dependency.getPath();
                    String fileName;
                    var i = path.lastIndexOf('/');
                    var j = dependency.getPath().lastIndexOf('.');
                    if (j == -1) {
                        throw new IllegalArgumentException("Path '" + path + "' is not a valid file path.");
                    }

                    if (i != -1) {
                        fileName = path.substring(i + 1, j);
                        path = path.substring(0, i);
                    } else {
                        fileName = path.substring(0, j);
                        path = "";
                    }

                    var targetPackage = path.replace('/', '.');
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
        var alias = properties.getTypeAliases().stream().filter(a -> a.getClassName().equals(className)).findAny().orElse(null);
        if (alias != null) {
            if (alias.getDependency() == null && alias.getPath() == null) {
                return null;
            }

            if (alias.getDependency() != null) {
                return new TypeScriptImportModel(alias.getDependency(), Set.of(alias.getType()));
            } else {
                return new TypeScriptImportModel(fileGenerator.importPath(currentPackage, alias.getPath()), Set.of(alias.getType()));
            }
        }

        var targetPackage = properties.getPackageNameForClass(className);
        var name = className.substring(className.lastIndexOf('.') + 1);

        var importPath = fileGenerator.importPath(currentPackage, targetPackage);
        var fileName = fileGenerator.fileName(name);
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

    private Collection<TypeScriptImportModel> resolveZodImports(String currentPackage, TypeScriptDTOModel model) {
        var imports = new HashMap<String, TypeScriptImportModel>();

        var allTypes = model.getFields().stream().map(TypeScriptDTOFieldModel::getType);
        if (model.getExtendedDTO() != null) {
            allTypes = Stream.concat(Stream.of(model.getExtendedDTO()), allTypes);
        }

        allTypes.map(type -> importZodModelsForType(currentPackage, type, model.getClassName()))
                .flatMap(List::stream)
                .forEach(i -> addImport(i, imports));

        return imports.values();
    }

    private List<TypeScriptImportModel> importZodModelsForType(String currentPackage, TypeScriptTypeModel typeModel, String currentClassName) {
        return typeModel.getDependentClassNames()
                .stream()
                .filter(not(currentClassName::equals))
                .map(className -> importZodModelForClassName(currentPackage, className))
                .filter(Objects::nonNull)
                .toList();
    }

    private TypeScriptImportModel importZodModelForClassName(String currentPackage, String className) {
        var alias = properties.getTypeAliases().stream().filter(a -> a.getClassName().equals(className)).findAny().orElse(null);
        if (alias != null) {
            if (alias.getZodSchema() == null) {
                // z.custom<Type>(_ => true) — the type itself must be imported for the generic
                if (alias.getDependency() != null) {
                    return new TypeScriptImportModel(alias.getDependency(), Set.of(alias.getType()));
                }
                if (alias.getPath() != null) {
                    return new TypeScriptImportModel(fileGenerator.importPath(currentPackage, alias.getPath()), Set.of(alias.getType()));
                }
                return null;
            }
            if (alias.getZodSchemaDependency() != null) {
                return new TypeScriptImportModel(alias.getZodSchemaDependency(), Set.of(alias.getZodSchema()));
            }
            if (alias.getZodSchemaPath() != null) {
                var i = alias.getZodSchemaPath().lastIndexOf('/');
                var targetPackage = i == -1 ? "" : alias.getZodSchemaPath().substring(0, i).replace('/', '.');
                var fileName = alias.getZodSchemaPath().substring(i + 1);
                return new TypeScriptImportModel(fileGenerator.importPath(currentPackage, targetPackage) + "/" + fileName, Set.of(alias.getZodSchema()));
            }
            return null;
        }

        var targetPackage = properties.getPackageNameForClass(className);
        var name = className.substring(className.lastIndexOf('.') + 1);
        var schemaName = name + "Schema";

        var importPath = fileGenerator.importPath(currentPackage, targetPackage);
        var schemaFileName = fileGenerator.fileName(schemaName);
        return new TypeScriptImportModel(importPath + "/" + schemaFileName, Set.of(schemaName));
    }

    protected Collection<TypeScriptImportModel> resolveZodImportsForService(String currentPackage, TypeScriptServiceModel model) {
        var imports = new HashMap<String, TypeScriptImportModel>();
        model.getMethods().stream()
                .map(TypeScriptServiceMethodModel::getReturnType)
                .filter(TypescriptClientGenerator::isZodSchemaReference)
                .map(type -> importZodModelsForType(currentPackage, type, model.getClassName()))
                .flatMap(List::stream)
                .forEach(i -> addImport(i, imports));
        return imports.values();
    }

    protected static boolean isZodSchemaReference(TypeScriptTypeModel type) {
        return type != null && type.getZodSchema() != null && !type.getZodSchema().startsWith("z.");
    }
}
