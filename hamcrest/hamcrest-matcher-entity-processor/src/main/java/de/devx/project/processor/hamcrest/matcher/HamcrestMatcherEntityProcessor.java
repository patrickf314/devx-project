package de.devx.project.processor.hamcrest.matcher;

import de.devx.project.hamcrest.matcher.processor.HamcrestMatcherProcessor;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

@SupportedAnnotationTypes(value = {
        "jakarta.persistence.Entity"
})
@SupportedSourceVersion(SourceVersion.RELEASE_20)
public class HamcrestMatcherEntityProcessor extends HamcrestMatcherProcessor {
}
