package api.maven.plugin.processor.spring;

import de.devx.project.commons.api.model.ApiModelConstants;
import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.io.ApiModelWriter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Set;

import static de.devx.project.commons.processor.spring.SpringAnnotations.*;

@SupportedAnnotationTypes(value = {
        REQUEST_MAPPING,
        GET_MAPPING,
        POST_MAPPING,
        PUT_MAPPING,
        DELETE_MAPPING
})
@SupportedSourceVersion(SourceVersion.RELEASE_20)
public class SpringAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        var generator = new SpringApiModelGenerator(processingEnv.getMessager());
        for (var annotation : annotations) {
            generator.process(annotation, roundEnv.getElementsAnnotatedWith(annotation));
        }

        try {
            writeModel(generator.getModel());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write api-model.json: " + e.getMessage());
        }

        return true;
    }

    private void writeModel(ApiModel model) throws IOException {
        var file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", ApiModelConstants.FILE_NAME);
        try (var writer = new ApiModelWriter(file.openWriter())) {
            writer.write(model);
        }
    }
}
