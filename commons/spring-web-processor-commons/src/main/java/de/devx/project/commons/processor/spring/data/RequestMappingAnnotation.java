package de.devx.project.commons.processor.spring.data;

import lombok.Data;

import java.util.List;

@Data
public class RequestMappingAnnotation {

    private String name;
    private List<String> paths;
    private List<String> requestMethods;

}
