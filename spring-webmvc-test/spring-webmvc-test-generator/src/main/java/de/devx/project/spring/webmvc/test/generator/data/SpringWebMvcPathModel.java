package de.devx.project.spring.webmvc.test.generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class SpringWebMvcPathModel {

    private String pattern;
    private List<String> parameters;

}
