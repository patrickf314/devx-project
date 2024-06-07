package de.devx.project.spring.webmvc.test.processor;

import de.devx.project.commons.generator.logging.Logger;
import de.devx.project.commons.processor.io.ProcessorJavaFileGenerator;
import de.devx.project.commons.processor.logging.JavaProcessorLogger;
import de.devx.project.commons.processor.spring.SpringAnnotations;
import de.devx.project.commons.processor.utils.TypeElementUtils;
import de.devx.project.spring.webmvc.test.generator.SpringWebMvcTestGenerator;
import de.devx.project.spring.webmvc.test.generator.data.*;
import de.devx.project.spring.webmvc.test.processor.data.SpringWebMvcTestAnnotation;
import org.springframework.http.HttpMessage;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.devx.project.commons.processor.spring.SpringAnnotations.MAPPING_ANNOTATIONS;
import static de.devx.project.commons.processor.spring.mapper.RequestMappingAnnotationMapper.mapAnnotationMirrorToRequestMapping;
import static de.devx.project.commons.processor.utils.AnnotationMirrorUtils.*;
import static de.devx.project.commons.processor.utils.ExecutableElementUtils.containsMethod;

@SupportedAnnotationTypes(value = {
        "de.devx.project.annotations.SpringWebMvcTest"
})
@SupportedSourceVersion(SourceVersion.RELEASE_20)
public class SpringWebMvcTestAnnotationProcessor extends AbstractProcessor {

    private Logger logger;
    private SpringWebMvcTestGenerator generator;

    public Logger getLogger() {
        if (logger == null) {
            logger = new JavaProcessorLogger(processingEnv.getMessager());
        }

        return logger;
    }

    public SpringWebMvcTestGenerator getGenerator() {
        if (generator == null) {
            generator = new SpringWebMvcTestGenerator(new ProcessorJavaFileGenerator(processingEnv.getFiler()));
        }

        return generator;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        for (var annotation : annotations) {
            generateWebMvcTests(annotation, roundEnv.getElementsAnnotatedWith(annotation));
        }

        return true;
    }

    private void generateWebMvcTests(TypeElement annotation, Collection<? extends Element> elements) {
        elements.forEach(element -> generateWebMvcTest(annotation, element));
    }

    private void generateWebMvcTest(TypeElement annotationElement, Element element) {
        if (!(element instanceof TypeElement typeElement)) {
            getLogger().error("SpringWebMvcTest annotation is not supported here.", element);
            return;
        }

        var className = typeElement.getQualifiedName().toString();
        var annotationMirror = findAnnotationMirror(element, annotationElement).orElseThrow(IllegalArgumentException::new);
        var annotation = new SpringWebMvcTestAnnotation(annotationMirror);

        if (!(annotation.controller().asElement() instanceof TypeElement controllerType)) {
            getLogger().error("Failed to generate spring webmvc test for controller of type " + annotation.controller().asElement().getClass().getName(), element);
            return;
        }

        var basePath = getBasePath(annotation.controller());
        var model = createModelFromAnnotation(className, annotation);

        for (var method : controllerType.getEnclosedElements()) {
            createMethodModel(basePath, method, annotation.service()).ifPresent(model.getMethods()::add);
        }

        try {
            getGenerator().generate(model);
        } catch (IOException e) {
            getLogger().error("Failed to generate spring webmvc test: " + e.getMessage(), element);
        }
    }

    private Optional<SpringWebMvcMethodModel> createMethodModel(String basePath, Element element, DeclaredType service) {
        if (!(element instanceof ExecutableElement method)) {
            return Optional.empty();
        }

        var annotationMirror = findAnyAnnotationMirror(element, MAPPING_ANNOTATIONS).orElse(null);
        if (annotationMirror == null) {
            return Optional.empty();
        }

        var requestMapping = mapAnnotationMirrorToRequestMapping(annotationMirror);

        var model = new SpringWebMvcMethodModel();
        model.setName(method.getSimpleName().toString());
        model.setPath(new SpringWebMvcPathModel(concatPathPattern(basePath, requestMapping.getPaths()), Collections.emptyList()));
        model.setReturnType(mapType(method.getReturnType()));
        model.setHttpMethod(requestMapping.getRequestMethods().get(0));

        var parameters = method.getParameters().stream().filter(this::isNotExcludedParameter).toList();

        model.setParameters(parameters.stream().map(this::createMethodParameterModel).toList());
        model.setDefaultServiceCall(containsMethod(service, method.getReturnType(), method.getSimpleName(), parameters.stream().map(VariableElement::asType).toList()));

        return Optional.of(model);
    }

    private boolean isNotExcludedParameter(VariableElement variableElement) {
        return !TypeElementUtils.isImplementationOf(variableElement.asType(), HttpMessage.class)
               && !TypeElementUtils.isImplementationOf(variableElement.asType(), "jakarta.servlet.ServletResponse")
               && !TypeElementUtils.isImplementationOf(variableElement.asType(), "jakarta.servlet.http.HttpServletResponse");
    }

    private String concatPathPattern(String basePath, List<String> paths) {
        var pattern = basePath.startsWith("/") ? basePath : "/" + basePath;

        if (paths.isEmpty()) {
            return pattern;
        }

        var path = paths.get(0);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return pattern + "/" + path;
    }

    private SpringWebMvcParameterModel createMethodParameterModel(VariableElement element) {
        var in = SpringWebMvcParameterModel.Type.BODY;
        if (findAnnotationMirror(element, SpringAnnotations.PATH_VARIABLE).isPresent()) {
            in = SpringWebMvcParameterModel.Type.PATH;
        } else if (findAnnotationMirror(element, SpringAnnotations.REQUEST_PARAM).isPresent()) {
            in = SpringWebMvcParameterModel.Type.QUERY;
        } else if (findAnnotationMirror(element, SpringAnnotations.REQUEST_HEADER).isPresent()) {
            in = SpringWebMvcParameterModel.Type.HEADER;
        }

        return new SpringWebMvcParameterModel(element.getSimpleName().toString(), in, mapType(element.asType()));
    }

    private String getBasePath(DeclaredType type) {
        var requestMapping = findAnnotationMirror(type.asElement(), SpringAnnotations.REQUEST_MAPPING).orElse(null);
        if (requestMapping == null) {
            return "";
        }

        var values = extractFieldsFromAnnotationMirror(requestMapping).get("value").getValue();
        if (values instanceof List<?> list) {
            return list.stream().map(AnnotationValue.class::cast).map(AnnotationValue::getValue).map(String.class::cast).findFirst().orElseThrow();
        }

        throw new IllegalArgumentException("Unexpected request mapping annotation");
    }

    private SpringWebMvcTestModel createModelFromAnnotation(String className, SpringWebMvcTestAnnotation annotation) {
        var i = className.lastIndexOf('.');
        var packageName = className.substring(0, i);
        var name = className.substring(i + 1);

        var model = new SpringWebMvcTestModel();

        model.setName(name + "Base");
        model.setPackageName(packageName);
        model.setService(mapType(annotation.service()));
        model.setController(mapType(annotation.controller()));
        model.setActiveProfile(annotation.activeProfiles());
        model.setContext(annotation.context().stream().map(this::mapType).toList());

        return model;
    }

    private SpringWebMvcTypeModel mapType(TypeMirror type) {
        if (type instanceof DeclaredType declaredType && declaredType.asElement() instanceof TypeElement element) {
            return SpringWebMvcTypeModel.fromClass(element.getQualifiedName().toString(), declaredType.getTypeArguments().stream().map(this::mapType).toList());
        }

        if (type instanceof NoType) {
            return SpringWebMvcTypeModel.VOID;
        }

        if (type instanceof PrimitiveType) {
            return SpringWebMvcTypeModel.primitive(type.toString());
        }

        if (type instanceof ArrayType arrayType) {
            return SpringWebMvcTypeModel.array(mapType(arrayType.getComponentType()));
        }

        throw new IllegalArgumentException("Cannot map value " + type.getClass().getName() + " to a type model.");
    }
}
