package api.maven.plugin.processor.spring;

import api.maven.plugin.core.model.*;
import api.maven.plugin.core.type.ApiMethodParameterType;
import api.maven.plugin.core.type.ApiMethodResponseType;
import api.maven.plugin.core.type.ApiTypeType;
import api.maven.plugin.processor.spring.mapper.ParameterAnnotationMapper;
import api.maven.plugin.processor.spring.utils.TypeElementUtils;
import api.maven.plugin.processor.spring.utils.AnnotationMirrorUtils;
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
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static api.maven.plugin.processor.spring.mapper.RequestMappingAnnotationMapper.mapAnnotationMirrorToRequestMapping;
import static api.maven.plugin.processor.spring.utils.AnnotationMirrorUtils.findAnnotationMirror;

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

        setReturnType(methodModel, element.getReturnType());

        methodModel.setParameters(mapMethodParameters(element.getParameters()));
        methodModel.getHttpMethods().addAll(requestMapping.getRequestMethods());
        methodModel.getPaths().addAll(requestMapping.getPaths());

        endpointModel.addMethod(methodModel);
    }

    private void setReturnType(ApiMethodModel model, TypeMirror typeMirror) {
        if (!(typeMirror instanceof DeclaredType declaredType)) {
            model.setReturnType(mapTypeMirror(typeMirror, true));
            return;
        }

        if (!(declaredType.asElement() instanceof TypeElement element)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected type mirror " + declaredType.getClass().getName() + ", asElement() does not return a TypeElement");
            return;
        }

        if (TypeElementUtils.isClass(element, ResponseEntity.class)) {
            setReturnType(model, declaredType.getTypeArguments().get(0));
        } else if (TypeElementUtils.isImplementationOf(declaredType, StreamingResponseBody.class)) {
            model.setReturnType(ApiTypeModel.UNKNOWN);
            model.setResponseType(ApiMethodResponseType.STREAM);
        } else if (TypeElementUtils.isExtensionOf(element, ResponseBodyEmitter.class)) {
            model.setReturnType(ApiTypeModel.UNKNOWN);
            model.setResponseType(ApiMethodResponseType.SERVER_SEND_EVENT);
        } else {
            model.setReturnType(mapDeclaredTypeMirror(declaredType, true));
        }
    }

    private List<ApiMethodParameterModel> mapMethodParameters(List<? extends VariableElement> parameters) {
        return parameters.stream()
                .filter(this::isRelevantParameter)
                .map(this::mapMethodParameter)
                .collect(Collectors.toList());
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

        return !TypeElementUtils.isImplementationOf(typeElement, HttpMessage.class) && !TypeElementUtils.isImplementationOf(typeElement, ServletResponse.class);
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
            parameterModel.setType(mapTypeMirror(element.asType(), true));
            parameterModel.setIn(ApiMethodParameterType.BODY);
        } else {
            var annotation = parameterAnnotations.get(0);
            parameterModel.setType(mapTypeMirror(element.asType(), annotation.getDefaultValue() == null && annotation.isRequired()));
            parameterModel.setIn(annotation.getType());
            parameterModel.setDefaultValue(annotation.getDefaultValue());
            parameterModel.setParameterName(annotation.getName() == null ? parameterModel.getName() : annotation.getName());
        }

        return parameterModel;
    }

    private ApiTypeModel mapTypeMirror(TypeMirror typeMirror, boolean required) {
        return switch (typeMirror.getKind()) {
            case INT, DOUBLE, FLOAT, BYTE, LONG, SHORT, BOOLEAN, VOID ->
                    new ApiTypeModel(typeMirror.toString(), ApiTypeType.JAVA_TYPE, true);
            case ARRAY -> mapArrayTypeMirror((ArrayType) typeMirror);
            case DECLARED -> mapDeclaredTypeMirror((DeclaredType) typeMirror, required);
            case TYPEVAR -> new ApiTypeModel(typeMirror.toString(), ApiTypeType.GENERIC_TYPE, required);
            default -> throw new IllegalArgumentException("Unexpected type mirror " + typeMirror.getClass().getName());
        };
    }

    private ApiTypeModel mapArrayTypeMirror(ArrayType typeMirror) {
        var typeModel = new ApiTypeModel("array", ApiTypeType.JAVA_TYPE);
        typeModel.setTypeArguments(List.of(mapTypeMirror(typeMirror.getComponentType(), true)));
        typeModel.setRequired(true);
        return typeModel;
    }

    private ApiTypeModel mapDeclaredTypeMirror(DeclaredType typeMirror, boolean required) {
        if (!(typeMirror.asElement() instanceof TypeElement element)) {
            throw new IllegalArgumentException("Unexpected type mirror " + typeMirror.getClass().getName() + ", asElement() does not return a TypeElement");
        }

        var className = element.getQualifiedName().toString();
        if (className.equals(String.class.getName())) {
            return new ApiTypeModel("string", ApiTypeType.JAVA_TYPE, required);
        }

        if (TypeElementUtils.isExtensionOf(element, Number.class)) {
            return new ApiTypeModel("number", ApiTypeType.JAVA_TYPE, required);
        }

        var collection = TypeElementUtils.getInterfaceTypeMirror(typeMirror, Collection.class);
        if (collection.isPresent()) {
            var typeArgs = TypeElementUtils.getTypeArgumentsOfInterface(typeMirror, Collection.class)
                    .stream()
                    .map(t -> this.mapTypeMirror(t, true))
                    .toList();
            return new ApiTypeModel("collection", ApiTypeType.JAVA_TYPE, required, typeArgs);
        }

        var map = TypeElementUtils.getInterfaceTypeMirror(element, Map.class);
        if (map.isPresent()) {
            var typeArgs = TypeElementUtils.getTypeArgumentsOfInterface(typeMirror, Map.class)
                    .stream()
                    .map(t -> this.mapTypeMirror(t, true))
                    .toList();;
            return new ApiTypeModel("map", ApiTypeType.JAVA_TYPE, required, typeArgs);
        }

        if (element.getKind() == ElementKind.ENUM) {
            var enumModel = createEnumModel(element, className);
            return new ApiTypeModel(enumModel.getName(), ApiTypeType.ENUM, enumModel.getClassName(), required);
        }

        var typeArguments = typeMirror.getTypeArguments()
                .stream()
                .map(t -> this.mapTypeMirror(t, true))
                .toList();

        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.RECORD) {
            var dtoModel = createDTOModel(element, className);
            return new ApiTypeModel(dtoModel.getName(), ApiTypeType.DTO, dtoModel.getClassName(), required, typeArguments);
        }

        return new ApiTypeModel("unknown", ApiTypeType.UNKNOWN, className, required);
    }

    private ApiDTOModel createDTOModel(TypeElement element, String className) {
        if (model.getDtos().containsKey(className)) {
            return model.getDtos().get(className);
        }

        var dtoModel = new ApiDTOModel(className, element.getSimpleName().toString());
        var superClass = element.getSuperclass();

        if (superClass.getKind() == TypeKind.DECLARED) {
            var superElement = ((DeclaredType) superClass).asElement();
            if (superElement.getKind() == ElementKind.CLASS && superElement instanceof TypeElement superTypeElement && !TypeElementUtils.isClass(superTypeElement, Record.class) && !TypeElementUtils.isClass(superTypeElement, Object.class)) {
                var superClassName = superTypeElement.getQualifiedName().toString();
                createDTOModel(superTypeElement, superClassName);
                dtoModel.setExtendedDTO(mapDeclaredTypeMirror((DeclaredType) superClass, true));
            }
        }

        dtoModel.setTypeArguments(element.getTypeParameters().stream().map(Object::toString).toList());

        model.addDTO(dtoModel);

        dtoModel.setFields(TypeElementUtils.getFields(element)
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getSimpleName().toString(),
                        e -> mapTypeMirror(e.asType(), isRequiredDTOField(e))
                ))
        );

        return dtoModel;
    }

    private boolean isRequiredDTOField(VariableElement element) {
        if (TypeElementUtils.isAnnotationPresent(element, NotNull.class)) {
            return true;
        }

        var type = element.asType();
        if (type instanceof DeclaredType declaredType) {
            return declaredType.asElement().getKind() == ElementKind.ENUM;
        }

        return false;
    }

    private ApiEnumModel createEnumModel(TypeElement element, String className) {
        if (model.getEnums().containsKey(className)) {
            return model.getEnums().get(className);
        }

        var enumModel = new ApiEnumModel(className, element.getSimpleName().toString());

        enumModel.setValues(TypeElementUtils.getFields(element).stream().map(VariableElement::getSimpleName).map(Name::toString).toList());

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
}
