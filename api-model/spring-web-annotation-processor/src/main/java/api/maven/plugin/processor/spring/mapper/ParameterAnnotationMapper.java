package api.maven.plugin.processor.spring.mapper;

import api.maven.plugin.processor.spring.data.ParameterAnnotation;
import de.devx.project.commons.api.model.type.ApiMethodParameterType;
import de.devx.project.commons.processor.utils.AnnotationMirrorUtils;
import org.springframework.web.bind.annotation.ValueConstants;

import javax.lang.model.element.AnnotationMirror;

import static api.maven.plugin.processor.spring.SpringAnnotations.*;
import static api.maven.plugin.processor.spring.mapper.AnnotationValueMapper.mapAnnotationValueToBoolean;
import static api.maven.plugin.processor.spring.mapper.AnnotationValueMapper.mapAnnotationValueToString;
import static de.devx.project.commons.processor.utils.AnnotationMirrorUtils.extractFieldsFromAnnotationMirror;

public final class ParameterAnnotationMapper {

    private ParameterAnnotationMapper() {
        // No instances
    }

    public static ParameterAnnotation mapAnnotationMirrorToParameterAnnotation(AnnotationMirror annotationMirror) {
        var values = extractFieldsFromAnnotationMirror(annotationMirror);
        var parameter = new ParameterAnnotation();

        if (values.containsKey("name")) {
            parameter.setName(mapAnnotationValueToString(values.get("name")));
        } else if (values.containsKey("value")) {
            parameter.setName(mapAnnotationValueToString(values.get("value")));
        }

        if (values.containsKey("required")) {
            parameter.setRequired(mapAnnotationValueToBoolean(values.get("required")));
        }

        if (values.containsKey("defaultValue")) {
            parameter.setDefaultValue(mapAnnotationValueToString(values.get("defaultValue")));
            if (ValueConstants.DEFAULT_NONE.equals(parameter.getDefaultValue())) {
                parameter.setDefaultValue(null);
            }
        } else {
            parameter.setDefaultValue(null);
        }

        var annotationName = AnnotationMirrorUtils.getAnnotationName(annotationMirror);
        if (REQUEST_PARAM.equals(annotationName)) {
            parameter.setType(ApiMethodParameterType.QUERY);
        } else if (PATH_VARIABLE.equals(annotationName)) {
            parameter.setType(ApiMethodParameterType.PATH);
        } else if (REQUEST_HEADER.equals(annotationName)) {
            parameter.setType(ApiMethodParameterType.HEADER);
        } else {
            parameter.setType(ApiMethodParameterType.BODY);
        }

        return parameter;
    }
}
