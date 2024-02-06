package de.devx.project.commons.processor.spring.mapper;

import de.devx.project.commons.processor.spring.SpringAnnotations;
import de.devx.project.commons.processor.spring.data.ParameterAnnotation;
import de.devx.project.commons.processor.spring.type.ParameterType;
import de.devx.project.commons.processor.utils.AnnotationMirrorUtils;
import org.springframework.web.bind.annotation.ValueConstants;

import javax.lang.model.element.AnnotationMirror;

import static de.devx.project.commons.processor.utils.AnnotationMirrorUtils.extractFieldsFromAnnotationMirror;

public final class ParameterAnnotationMapper {

    private ParameterAnnotationMapper() {
        // No instances
    }

    public static ParameterAnnotation mapAnnotationMirrorToParameterAnnotation(AnnotationMirror annotationMirror) {
        var values = extractFieldsFromAnnotationMirror(annotationMirror);
        var parameter = new ParameterAnnotation();

        if (values.containsKey("name")) {
            parameter.setName(AnnotationValueMapper.mapAnnotationValueToString(values.get("name")));
        } else if (values.containsKey("value")) {
            parameter.setName(AnnotationValueMapper.mapAnnotationValueToString(values.get("value")));
        }

        if (values.containsKey("required")) {
            parameter.setRequired(AnnotationValueMapper.mapAnnotationValueToBoolean(values.get("required")));
        }

        if (values.containsKey("defaultValue")) {
            parameter.setDefaultValue(AnnotationValueMapper.mapAnnotationValueToString(values.get("defaultValue")));
            if (ValueConstants.DEFAULT_NONE.equals(parameter.getDefaultValue())) {
                parameter.setDefaultValue(null);
            }
        } else {
            parameter.setDefaultValue(null);
        }

        var annotationName = AnnotationMirrorUtils.getAnnotationName(annotationMirror);
        if (SpringAnnotations.REQUEST_PARAM.equals(annotationName)) {
            parameter.setType(ParameterType.QUERY);
        } else if (SpringAnnotations.PATH_VARIABLE.equals(annotationName)) {
            parameter.setType(ParameterType.PATH);
        } else if (SpringAnnotations.REQUEST_HEADER.equals(annotationName)) {
            parameter.setType(ParameterType.HEADER);
        } else {
            parameter.setType(ParameterType.BODY);
        }

        return parameter;
    }
}
