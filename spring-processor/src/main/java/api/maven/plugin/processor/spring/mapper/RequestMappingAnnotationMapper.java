package api.maven.plugin.processor.spring.mapper;

import api.maven.plugin.processor.spring.data.RequestMappingAnnotation;
import api.maven.plugin.common.processor.utils.AnnotationMirrorUtils;

import javax.lang.model.element.AnnotationMirror;

import java.util.Collections;
import java.util.List;

import static api.maven.plugin.processor.spring.SpringAnnotations.*;
import static api.maven.plugin.processor.spring.mapper.AnnotationValueMapper.*;
import static api.maven.plugin.processor.spring.mapper.AnnotationValueMapper.mapAnnotationValueToStringArray;
import static api.maven.plugin.common.processor.utils.AnnotationMirrorUtils.extractFieldsFromAnnotationMirror;

public final class RequestMappingAnnotationMapper {

    private RequestMappingAnnotationMapper() {
        // No instances
    }

    public static RequestMappingAnnotation mapAnnotationMirrorToRequestMapping(AnnotationMirror annotationMirror) {
        var values = extractFieldsFromAnnotationMirror(annotationMirror);
        var requestMapping = new RequestMappingAnnotation();

        if (values.containsKey("name")) {
            requestMapping.setName(mapAnnotationValueToString(values.get("name")));
        }

        if (values.containsKey("value")) {
            requestMapping.setPaths(mapAnnotationValueToStringArray(values.get("value")));
        } else if (values.containsKey("path")) {
            requestMapping.setPaths(mapAnnotationValueToStringArray(values.get("value")));
        } else{
            requestMapping.setPaths(Collections.emptyList());
        }

        var annotationName = AnnotationMirrorUtils.getAnnotationName(annotationMirror);
        if (GET_MAPPING.equals(annotationName)) {
            requestMapping.setRequestMethods(List.of("GET"));
        } else if (POST_MAPPING.equals(annotationName)) {
            requestMapping.setRequestMethods(List.of("POST"));
        } else if (PUT_MAPPING.equals(annotationName)) {
            requestMapping.setRequestMethods(List.of("PUT"));
        } else if (DELETE_MAPPING.equals(annotationName)) {
            requestMapping.setRequestMethods(List.of("DELETE"));
        } else if (values.containsKey("method")) {
            requestMapping.setRequestMethods(mapAnnotationValueToStringArray(values.get("method")));
        }

        return requestMapping;
    }
}
