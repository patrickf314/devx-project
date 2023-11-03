package api.maven.plugin.processor.entity;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes(value = {
        "jakarta.persistence.Entity"
})
@SupportedSourceVersion(SourceVersion.RELEASE_20)
public class EntityMatcherAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        var generator = new EntityMatcherGenerator(processingEnv.getMessager(), processingEnv.getFiler());
        for (var annotation : annotations) {
            generator.generateMatchers(roundEnv.getElementsAnnotatedWith(annotation));
        }
        return true;
    }
}
