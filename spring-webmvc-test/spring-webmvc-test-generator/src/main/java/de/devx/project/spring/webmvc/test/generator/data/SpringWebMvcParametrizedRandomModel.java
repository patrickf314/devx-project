package de.devx.project.spring.webmvc.test.generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class SpringWebMvcParametrizedRandomModel {

    private String name;
    private SpringWebMvcTypeModel type;

}
