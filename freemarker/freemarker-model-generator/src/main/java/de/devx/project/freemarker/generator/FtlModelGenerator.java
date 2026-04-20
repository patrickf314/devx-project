package de.devx.project.freemarker.generator;

import de.devx.project.commons.generator.io.SourceFileGenerator;
import de.devx.project.freemarker.generator.data.FtlTemplateModel;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class FtlModelGenerator {

    private final SourceFileGenerator fileGenerator;
    private final Configuration configuration;

    public FtlModelGenerator(SourceFileGenerator fileGenerator) {
        this.fileGenerator = fileGenerator;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(FtlModelGenerator.class.getClassLoader(), "templates");
    }

    public void generateFreemarkerTemplate(String packageName) throws IOException {
        try (var writer = fileGenerator.createSourceFile(packageName, "FreemarkerTemplate")) {
            configuration.getTemplate("FreemarkerTemplate.java.ftl").process(Map.of("packageName", packageName), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to generate FreemarkerTemplate", e);
        }
    }

    public void generateTemplatesClass(String enginePackage, List<FtlTemplateModel> templates) throws IOException {
        var first = templates.getFirst();
        var groupClassName = first.getGroupClassName();
        var packageName = first.getPackageName();

        var imports = new TreeSet<String>();
        if (!enginePackage.equals(packageName)) {
            imports.add(enginePackage + ".FreemarkerTemplate");
        }

        try (var writer = fileGenerator.createSourceFile(packageName, groupClassName)) {
            configuration.getTemplate("TemplatesGroup.java.ftl").process(Map.of(
                    "groupClassName", groupClassName,
                    "packageName", packageName,
                    "imports", List.copyOf(imports),
                    "templates", templates
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to generate " + groupClassName, e);
        }
    }

    public void generateFreemarkerTemplateEngine(String packageName) throws IOException {
        try (var writer = fileGenerator.createSourceFile(packageName, "FreemarkerTemplateEngine")) {
            configuration.getTemplate("FreemarkerTemplateEngine.java.ftl").process(Map.of("packageName", packageName), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to generate FreemarkerTemplateEngine", e);
        }
    }

    public void generateModel(FtlTemplateModel model) throws IOException {
        var imports = collectImports(model);

        try (var writer = fileGenerator.createSourceFile(model.getPackageName(), model.getModelClassName())) {
            configuration.getTemplate("FtlModel.ftl").process(Map.of(
                    "model", model,
                    "imports", imports
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template for " + model.getTemplateName(), e);
        }
    }

    private List<String> collectImports(FtlTemplateModel model) {
        var imports = new TreeSet<String>();
        imports.add("lombok.AllArgsConstructor");
        imports.add("lombok.Data");
        imports.add("lombok.NoArgsConstructor");

        model.getVariables().stream()
                .flatMap(v -> v.getImports().stream())
                .filter(imp -> !imp.startsWith(model.getPackageName() + "."))
                .forEach(imports::add);

        return List.copyOf(imports);
    }
}
