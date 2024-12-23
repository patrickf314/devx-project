package de.devx.project.commons.processor.spring;

import java.util.Set;

public final class SpringAnnotations {

    // We cannot use RequestMapping.class.getName() here, since we want to use the constant in annotations
    public static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    public static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    public static final String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    public static final String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    public static final String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";

    public static final String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    public static final String PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";
    public static final String REQUEST_HEADER = "org.springframework.web.bind.annotation.RequestHeader";
    public static final String REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";

    public static final Set<String> MAPPING_ANNOTATIONS = Set.of(REQUEST_MAPPING, GET_MAPPING, POST_MAPPING, PUT_MAPPING, DELETE_MAPPING);
    public static final Set<String> PARAMETER_ANNOTATIONS = Set.of(REQUEST_PARAM, PATH_VARIABLE, REQUEST_HEADER, REQUEST_BODY);

    private SpringAnnotations() {
        // No instances
    }
}
