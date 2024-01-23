package de.devx.project.hamcrest.matcher.processor;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

@SupportedAnnotationTypes(value = {
        "de.devx.project.annotations.HamcrestMatcher"
})
@SupportedSourceVersion(SourceVersion.RELEASE_20)
public class HamcrestMatcherAnnotationProcessor extends HamcrestMatcherProcessor {
}
