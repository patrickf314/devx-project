package de.devx.project.commons.processor.spring.mapper;

import de.devx.project.commons.processor.spring.SpringAnnotations;
import de.devx.project.commons.processor.spring.data.RequestMappingAnnotation;
import de.devx.project.commons.processor.utils.AnnotationMirrorUtils;

import javax.lang.model.element.AnnotationMirror;
import java.util.Collections;
import java.util.List;

import static de.devx.project.commons.processor.utils.AnnotationMirrorUtils.extractFieldsFromAnnotationMirror;

public final class RequestMappingAnnotationMapper {

    private RequestMappingAnnotationMapper() {
        // No instances
    }

    public static RequestMappingAnnotation mapAnnotationMirrorToRequestMapping(AnnotationMirror annotationMirror) {
        var values = extractFieldsFromAnnotationMirror(annotationMirror);
        var requestMapping = new RequestMappingAnnotation();

        if (values.containsKey("name")) {
            requestMapping.setName(AnnotationValueMapper.mapAnnotationValueToString(values.get("name")));
        }

        if (values.containsKey("value")) {
            requestMapping.setPaths(AnnotationValueMapper.mapAnnotationValueToStringArray(values.get("value")));
        } else if (values.containsKey("path")) {
            requestMapping.setPaths(AnnotationValueMapper.mapAnnotationValueToStringArray(values.get("value")));
        } else {
            requestMapping.setPaths(Collections.emptyList());
        }

        var annotationName = AnnotationMirrorUtils.getAnnotationName(annotationMirror);
        if (SpringAnnotations.GET_MAPPING.equals(annotationName)) {
            requestMapping.setRequestMethods(List.of("GET"));
        } else if (SpringAnnotations.POST_MAPPING.equals(annotationName)) {
            requestMapping.setRequestMethods(List.of("POST"));
        } else if (SpringAnnotations.PUT_MAPPING.equals(annotationName)) {
            requestMapping.setRequestMethods(List.of("PUT"));
        } else if (SpringAnnotations.DELETE_MAPPING.equals(annotationName)) {
            requestMapping.setRequestMethods(List.of("DELETE"));
        } else if (values.containsKey("method")) {
            requestMapping.setRequestMethods(AnnotationValueMapper.mapAnnotationValueToStringArray(values.get("method")));
        }

        return requestMapping;
    }
}
