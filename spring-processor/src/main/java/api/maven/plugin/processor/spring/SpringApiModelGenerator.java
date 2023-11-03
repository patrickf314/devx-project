package api.maven.plugin.processor.spring;

import api.maven.plugin.annotations.Readonly;
import api.maven.plugin.common.processor.utils.AnnotationMirrorUtils;
import api.maven.plugin.common.processor.utils.TypeElementUtils;
import api.maven.plugin.core.model.*;
import api.maven.plugin.core.type.ApiMethodParameterType;
import api.maven.plugin.core.type.ApiMethodResponseType;
import api.maven.plugin.core.type.ApiTypeType;
import api.maven.plugin.processor.spring.mapper.ParameterAnnotationMapper;
import org.springframework.http.HttpMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.servlet.ServletResponse;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;

import static api.maven.plugin.common.processor.utils.AnnotationMirrorUtils.findAnnotationMirror;
import static api.maven.plugin.processor.spring.mapper.RequestMappingAnnotationMapper.mapAnnotationMirrorToRequestMapping;

public class SpringApiModelGenerator {

    private final ApiModel model = new ApiModel();
    private final Messager messager;

    public SpringApiModelGenerator(Messager messager) {
        this.messager = messager;
    }

    public ApiModel getModel() {
        return model;
    }

    public void process(TypeElement annotation, Set<? extends Element> annotatedElements) {
        for (var element : annotatedElements) {
            this.process(annotation, element);
        }
    }

    private void process(TypeElement annotation, Element element) {
        switch (element.getKind()) {
            case INTERFACE, CLASS -> processClass(annotation, (TypeElement) element);
            case METHOD -> processMethod(annotation, (ExecutableElement) element);
            default ->
                    messager.printMessage(Diagnostic.Kind.ERROR, "Element of kind " + element.getKind() + " is not supported", element);
        }
    }

    private void processClass(TypeElement annotation, TypeElement element) {
        var className = element.getQualifiedName().toString();
        var endpointModel = requireEndpointModel(className, element.getSimpleName().toString());

        var annotationMirror = findAnnotationMirror(element, annotation).orElseThrow(IllegalArgumentException::new);
        var requestMapping = mapAnnotationMirrorToRequestMapping(annotationMirror);

        endpointModel.getBasePaths().addAll(requestMapping.getPaths());
    }

    private void processMethod(TypeElement annotation, ExecutableElement element) {
        var annotationMirror = findAnnotationMirror(element, annotation).orElseThrow(IllegalArgumentException::new);
        var requestMapping = mapAnnotationMirrorToRequestMapping(annotationMirror);
        if (!(element.getEnclosingElement() instanceof TypeElement enclosingElement)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected enclosing element for " + element.getSimpleName(), element);
            return;
        }

        var className = enclosingElement.getQualifiedName().toString();
        var endpointModel = requireEndpointModel(className, enclosingElement.getSimpleName().toString());
        var methodName = element.getSimpleName().toString();
        var methodModel = new ApiMethodModel(methodName);

        setReturnType(methodModel, element.getReturnType(), element.getAnnotationMirrors());

        methodModel.setParameters(mapMethodParameters(element.getParameters()));
        methodModel.getHttpMethods().addAll(requestMapping.getRequestMethods());
        methodModel.getPaths().addAll(requestMapping.getPaths());

        endpointModel.addMethod(methodModel);
    }

    private void setReturnType(ApiMethodModel model, TypeMirror typeMirror, List<? extends AnnotationMirror> annotations) {
        if (!(typeMirror instanceof DeclaredType declaredType)) {
            model.setReturnType(mapTypeMirror(typeMirror, true, annotations));
            return;
        }

        if (!(declaredType.asElement() instanceof TypeElement element)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected type mirror " + declaredType.getClass().getName() + ", asElement() does not return a TypeElement");
            return;
        }

        if (TypeElementUtils.isClass(element, ResponseEntity.class)) {
            setReturnType(model, declaredType.getTypeArguments().get(0), annotations);
        } else if (TypeElementUtils.isImplementationOf(declaredType, StreamingResponseBody.class)) {
            model.setReturnType(ApiTypeModel.UNKNOWN);
            model.setResponseType(ApiMethodResponseType.STREAM);
        } else if (TypeElementUtils.isExtensionOf(element, ResponseBodyEmitter.class)) {
            model.setReturnType(ApiTypeModel.UNKNOWN);
            model.setResponseType(ApiMethodResponseType.SERVER_SEND_EVENT);
        } else {
            model.setReturnType(mapDeclaredTypeMirror(declaredType, true, annotations));
        }
    }

    private List<ApiMethodParameterModel> mapMethodParameters(List<? extends VariableElement> parameters) {
        return parameters.stream()
                .filter(this::isRelevantParameter)
                .map(this::mapMethodParameter)
                .toList();
    }

    private boolean isRelevantParameter(VariableElement element) {
        var type = element.asType();
        if (type.getKind() != TypeKind.DECLARED) {
            return true;
        }

        if (!(((DeclaredType) type).asElement() instanceof TypeElement typeElement)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected type mirror " + type.getClass().getName() + ", asElement() does not return a TypeElement");
            return true;
        }

        return !TypeElementUtils.isImplementationOf(typeElement, HttpMessage.class)
                && !TypeElementUtils.isImplementationOf(typeElement, ServletResponse.class)
                && !TypeElementUtils.isImplementationOf(typeElement, "jakarta.servlet.http.HttpServletResponse");
    }

    private ApiMethodParameterModel mapMethodParameter(VariableElement element) {
        var parameterModel = new ApiMethodParameterModel(element.getSimpleName().toString());
        var parameterAnnotations = element.getAnnotationMirrors()
                .stream()
                .filter(mirror -> SpringAnnotations.PARAMETER_ANNOTATIONS.contains(AnnotationMirrorUtils.getAnnotationName(mirror)))
                .map(ParameterAnnotationMapper::mapAnnotationMirrorToParameterAnnotation)
                .toList();

        if (parameterAnnotations.size() > 1) {
            throw new IllegalArgumentException("Multiple parameter annotations is not supported");
        }

        if (parameterAnnotations.isEmpty()) {
            parameterModel.setType(mapTypeMirror(element.asType(), true, element.getAnnotationMirrors()));
            parameterModel.setIn(ApiMethodParameterType.BODY);
        } else {
            var annotation = parameterAnnotations.get(0);
            parameterModel.setType(mapTypeMirror(element.asType(), annotation.getDefaultValue() == null && annotation.isRequired(), element.getAnnotationMirrors()));
            parameterModel.setIn(annotation.getType());
            parameterModel.setDefaultValue(annotation.getDefaultValue());
            parameterModel.setParameterName(annotation.getName() == null ? parameterModel.getName() : annotation.getName());
        }

        return parameterModel;
    }

    private ApiTypeModel mapTypeMirror(TypeMirror typeMirror, boolean required, List<? extends AnnotationMirror> annotations) {
        return switch (typeMirror.getKind()) {
            case INT, DOUBLE, FLOAT, BYTE, LONG, SHORT, BOOLEAN, VOID -> new ApiTypeModel(
                    typeMirror.getKind().name().toLowerCase(Locale.ROOT),
                    ApiTypeType.JAVA_TYPE, null, true,
                    Collections.emptyList(), Collections.emptyList(), mapAnnotations(annotations));
            case ARRAY -> mapArrayTypeMirror((ArrayType) typeMirror, annotations);
            case DECLARED -> mapDeclaredTypeMirror((DeclaredType) typeMirror, required, annotations);
            case TYPEVAR -> new ApiTypeModel(typeMirror.toString(), ApiTypeType.GENERIC_TYPE, null, required);
            default ->
                    throw new IllegalArgumentException("Unexpected type mirror " + typeMirror + "(" + typeMirror.getClass().getName() + ")");
        };
    }

    private ApiTypeModel mapArrayTypeMirror(ArrayType typeMirror, List<? extends AnnotationMirror> annotations) {
        var typeModel = new ApiTypeModel("array", ApiTypeType.JAVA_TYPE, null, true);
        typeModel.setTypeArguments(List.of(mapTypeMirror(typeMirror.getComponentType(), true, Collections.emptyList())));
        typeModel.setAnnotations(mapAnnotations(annotations));
        return typeModel;
    }

    private ApiTypeModel mapDeclaredTypeMirror(DeclaredType typeMirror, boolean required, List<? extends AnnotationMirror> annotations) {
        if (!(typeMirror.asElement() instanceof TypeElement element)) {
            throw new IllegalArgumentException("Unexpected type mirror " + typeMirror.getClass().getName() + ", asElement() does not return a TypeElement");
        }

        var className = element.getQualifiedName().toString();
        if (className.equals(String.class.getName())) {
            return new ApiTypeModel("string", ApiTypeType.JAVA_TYPE, String.class.getName(), required, Collections.emptyList(), Collections.emptyList(), mapAnnotations(annotations));
        }

        if (TypeElementUtils.isExtensionOf(element, Number.class)) {
            return new ApiTypeModel("number", ApiTypeType.JAVA_TYPE, element.getQualifiedName().toString(), required, Collections.emptyList(), Collections.emptyList(), mapAnnotations(annotations));
        }

        var collection = TypeElementUtils.getInterfaceTypeMirror(typeMirror, Collection.class);
        if (collection.isPresent()) {
            var typeArgs = TypeElementUtils.getTypeArgumentsOfInterface(typeMirror, Collection.class)
                    .stream()
                    .map(t -> this.mapTypeMirror(t, true, Collections.emptyList()))
                    .toList();
            return new ApiTypeModel("collection", ApiTypeType.JAVA_TYPE, null, required, typeArgs, Collections.emptyList(), mapAnnotations(annotations));
        }

        var map = TypeElementUtils.getInterfaceTypeMirror(element, Map.class);
        if (map.isPresent()) {
            var typeArgs = TypeElementUtils.getTypeArgumentsOfInterface(typeMirror, Map.class)
                    .stream()
                    .map(t -> this.mapTypeMirror(t, true, Collections.emptyList()))
                    .toList();
            return new ApiTypeModel("map", ApiTypeType.JAVA_TYPE, null, required, typeArgs, Collections.emptyList(), mapAnnotations(annotations));
        }

        if (element.getKind() == ElementKind.ENUM) {
            var enumModel = createEnumModel(element, className);
            return new ApiTypeModel(enumModel.getName(), ApiTypeType.ENUM, enumModel.getClassName(), required, Collections.emptyList(), Collections.emptyList(), mapAnnotations(annotations));
        }

        var typeArguments = typeMirror.getTypeArguments()
                .stream()
                .map(t -> this.mapTypeMirror(t, true, Collections.emptyList()))
                .toList();

        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.RECORD) {
            var dtoModel = createDTOModel(element, className);
            var nesting = new ArrayList<String>();
            if (dtoModel.getEnclosingDTO() != null) {
                nesting.addAll(dtoModel.getEnclosingDTO().getNesting());
                nesting.add(dtoModel.getEnclosingDTO().getName());
            }
            return new ApiTypeModel(dtoModel.getName(), ApiTypeType.DTO, dtoModel.getClassName(), required, typeArguments, nesting, mapAnnotations(annotations));
        }

        return new ApiTypeModel("unknown", ApiTypeType.UNKNOWN, className, required, Collections.emptyList(), Collections.emptyList(), mapAnnotations(annotations));
    }

    private ApiDTOModel createDTOModel(TypeElement element, String className) {
        if (model.getDtos().containsKey(className)) {
            return model.getDtos().get(className);
        }

        var dtoModel = new ApiDTOModel(className, element.getSimpleName().toString(), element.getKind() == ElementKind.RECORD);

        if (element.getEnclosingElement() instanceof TypeElement enclosingElement) {
            var enclosingDTO = createDTOModel(enclosingElement, enclosingElement.getQualifiedName().toString());
            var nesting = new ArrayList<String>();
            if (enclosingDTO.getEnclosingDTO() != null) {
                nesting.addAll(enclosingDTO.getEnclosingDTO().getNesting());
                nesting.add(enclosingDTO.getEnclosingDTO().getName());
            }
            dtoModel.setEnclosingDTO(new ApiEnclosingDTOModel(enclosingDTO.getClassName(), enclosingDTO.getName(), nesting));
        }

        var superClass = element.getSuperclass();

        if (superClass.getKind() == TypeKind.DECLARED) {
            var superElement = ((DeclaredType) superClass).asElement();
            if (superElement.getKind() == ElementKind.CLASS && superElement instanceof TypeElement superTypeElement && !TypeElementUtils.isClass(superTypeElement, Record.class) && !TypeElementUtils.isClass(superTypeElement, Object.class)) {
                var superClassName = superTypeElement.getQualifiedName().toString();
                createDTOModel(superTypeElement, superClassName);
                dtoModel.setExtendedDTO(mapDeclaredTypeMirror((DeclaredType) superClass, true, Collections.emptyList()));
            }
        }

        dtoModel.setTypeArguments(element.getTypeParameters().stream().map(Object::toString).toList());

        model.addDTO(dtoModel);

        dtoModel.setFields(TypeElementUtils.getNonStaticFields(element)
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getSimpleName().toString(),
                        e -> mapTypeMirror(e.asType(), isRequiredDTOField(e), e.getAnnotationMirrors())
                ))
        );

        return dtoModel;
    }

    private boolean isRequiredDTOField(VariableElement element) {
        if (TypeElementUtils.isAnnotationPresent(element, "javax.validation.constraints.NotNull")
                || TypeElementUtils.isAnnotationPresent(element, "jakarta.validation.constraints.NotNull")) {
            return true;
        }

        var type = element.asType();
        if (type instanceof DeclaredType declaredType && declaredType.asElement().getKind() == ElementKind.ENUM) {
            return true;
        }

        if (TypeElementUtils.getInterfaceTypeMirror(type, Collection.class).isPresent() || type instanceof ArrayType) {
            return true;
        }

        return false;
    }

    private ApiEnumModel createEnumModel(TypeElement element, String className) {
        if (model.getEnums().containsKey(className)) {
            return model.getEnums().get(className);
        }

        var enumModel = new ApiEnumModel(className, element.getSimpleName().toString());

        enumModel.setValues(TypeElementUtils.getEnumValues(element).stream().map(VariableElement::getSimpleName).map(Name::toString).toList());

        model.addEnum(enumModel);

        return enumModel;
    }

    private ApiServiceEndpointModel requireEndpointModel(String className, String name) {
        var endpointModel = model.getEndpoints().get(className);
        if (endpointModel == null) {
            endpointModel = new ApiServiceEndpointModel(className, name);
            model.addEndpoint(endpointModel);
        }
        return endpointModel;
    }

    private List<String> mapAnnotations(List<? extends AnnotationMirror> annotations) {
        return annotations.stream().map(this::mapAnnotation).toList();
    }

    private String mapAnnotation(AnnotationMirror annotation) {
        return AnnotationMirrorUtils.getAnnotationName(annotation);
    }
}
