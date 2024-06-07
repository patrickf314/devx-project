package de.devx.project.spring.webmvc.test.generator.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static java.util.function.Predicate.not;

@Data
public class SpringWebMvcTestModel {

    private String name;
    private String packageName;

    private SpringWebMvcTypeModel service;
    private SpringWebMvcTypeModel controller;

    private List<String> activeProfile = new ArrayList<>();
    private List<SpringWebMvcTypeModel> context = new ArrayList<>();

    private List<SpringWebMvcMethodModel> methods = new ArrayList<>();

    public boolean containsMultipartRequest() {
        return methods.stream().anyMatch(SpringWebMvcMethodModel::isMultipartRequest);
    }

    public boolean containsStandardRequest() {
        return methods.stream().anyMatch(not(SpringWebMvcMethodModel::isMultipartRequest));
    }

    public boolean containsAsyncRequest() {
        return methods.stream().anyMatch(SpringWebMvcMethodModel::isAsync);
    }
}
