package de.devx.project.spring.web.processor.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestMappingAnnotation {

    private String name;
    private List<String> paths;
    private List<String> requestMethods;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPaths() {
        return Collections.unmodifiableList(paths);
    }

    public void setPaths(List<String> paths) {
        this.paths = new ArrayList<>(paths);
    }

    public List<String> getRequestMethods() {
        return Collections.unmodifiableList(requestMethods);
    }

    public void setRequestMethods(List<String> requestMethods) {
        this.requestMethods = new ArrayList<>(requestMethods);
    }
}
