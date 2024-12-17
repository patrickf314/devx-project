package de.devx.project.assertj.assertion.maven;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertFieldModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertThatMethodModel;
import de.devx.project.commons.api.model.data.ApiDTOModel;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.generator.model.JavaTypeArgumentModel;
import de.devx.project.commons.generator.model.JavaTypeModel;
import de.devx.project.commons.maven.parser.MavenApiModelParser;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static de.devx.project.commons.generator.model.JavaTypeModel.genericTemplateType;
import static de.devx.project.commons.generator.model.JavaTypeModel.objectType;
import static de.devx.project.commons.generator.utils.ClassUtils.toBoxedType;
import static de.devx.project.commons.maven.parser.MavenApiModelParser.extractPackageName;
import static de.devx.project.commons.maven.parser.MavenApiModelParser.extractSimpleClassName;

@Mojo(
        name = "generate-assertj-api-model-assertions",
        defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true
)
public class AssertJApiModelAssertionsMojo extends AbstractAssertJAssertionsMojo {

    @Parameter(property = "apiModelAssertions.apiModelJson")
    private File apiModelJson;

    @Override
    protected List<AssertJAssertModel> createAssertModels() throws MojoExecutionException {
        var parser = new MavenApiModelParser(getLog(), apiModelJson, project);
        return parser.requireApiModels()
                .stream()
                .flatMap(model -> model.getDtos().values().stream())
                .map(this::mapToModel)
                .toList();
    }

    @Override
    protected List<AssertJAssertThatMethodModel> createAssertThatMethodModels() {
        return Collections.emptyList();
    }

    private AssertJAssertModel mapToModel(ApiDTOModel apiModel) {
        var model = new AssertJAssertModel();
        model.setName(extractSimpleClassName(apiModel));
        model.setPackageName(extractPackageName(apiModel));
        model.setFields(apiModel.getFields()
                .entrySet()
                .stream()
                .map(entry -> mapToModel(entry.getKey(), entry.getValue()))
                .toList()
        );
        model.setTypeArguments(apiModel.getTypeArguments().stream()
                .map(JavaTypeArgumentModel::new).toList());
        model.setJavaRecord(apiModel.isJavaRecord());
        return model;
    }

    private AssertJAssertFieldModel mapToModel(String name, ApiTypeModel type) {
        var model = new AssertJAssertFieldModel();
        model.setName(name);
        model.setType(mapToModel(type));
        return model;
    }

    private JavaTypeModel mapToModel(ApiTypeModel apiModel) {
        return switch (apiModel.getType()) {
            case UNKNOWN -> objectType("java.lang", "Object");
            case GENERIC_TYPE -> genericTemplateType(apiModel.getName());
            case ENUM -> objectType(
                    extractPackageName(apiModel),
                    extractSimpleClassName(apiModel)
            );
            case DTO -> objectType(
                    extractPackageName(apiModel),
                    extractSimpleClassName(apiModel),
                    apiModel.getTypeArguments().stream().map(this::mapToModel).toList()
            );
            case JAVA_TYPE -> {
                var className = apiModel.getClassName();
                if (className == null) {
                    var boxedType = toBoxedType(apiModel.getName());
                    yield JavaTypeModel.primitiveType(apiModel.getName(), boxedType.getSimpleName());
                }

                var generics = apiModel.getTypeArguments().stream().map(this::mapToModel).toList();
                if ("collection".equals(apiModel.getName())) {
                    yield objectType("java.util", "Collection", generics);
                } else if ("map".equals(apiModel.getName())) {
                    yield objectType("java.util", "Map", generics);
                }

                yield objectType(
                        extractPackageName(apiModel),
                        extractSimpleClassName(apiModel),
                        generics
                );
            }
        };
    }
}
