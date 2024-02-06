package de.devx.project.spring.webmvc.test.generator.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpringWebMvcTestModel {

    private String name;
    private String packageName;

    private SpringWebMvcTypeModel service;
    private SpringWebMvcTypeModel controller;

    private List<String> activeProfile = new ArrayList<>();
    private List<SpringWebMvcTypeModel> context = new ArrayList<>();

    private List<SpringWebMvcMethodModel> methods = new ArrayList<>();
}
