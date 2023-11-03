package api.maven.plugin.processor.hamcrest.matcher;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes(value = {
        "api.maven.plugin.annotations.HamcrestMatcher"
})
@SupportedSourceVersion(SourceVersion.RELEASE_20)
public class HamcrestMatcherAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        var generator = new HamcrestMatcherGenerator(processingEnv.getMessager(), processingEnv.getFiler());
        for (var annotation : annotations) {
            generator.generateMatchers(roundEnv.getElementsAnnotatedWith(annotation));
        }
        return true;
    }
}
