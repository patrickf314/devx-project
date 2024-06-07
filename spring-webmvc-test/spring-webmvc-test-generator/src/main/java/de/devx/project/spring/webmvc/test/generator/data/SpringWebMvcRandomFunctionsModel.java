package de.devx.project.spring.webmvc.test.generator.data;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class SpringWebMvcRandomFunctionsModel {

    private final Map<SpringWebMvcTypeModel, SpringWebMvcParametrizedRandomModel> parametrizedRandom;

    public Collection<SpringWebMvcParametrizedRandomModel> getParametrizedRandomFunctions() {
        return parametrizedRandom.values();
    }

    public String getRandomFunctionName(SpringWebMvcParameterModel parameter) {
        if (parameter.isMultipartFile()) {
            return "new MockMultipartFile(\"" + parameter.getName() + "\", random.nextObject(String.class), \"text/plain\", random.nextObject(String.class).getBytes(StandardCharsets.UTF_8))";
        }

        return getRandomFunctionName(parameter.getType());
    }

    public String getRandomFunctionName(SpringWebMvcTypeModel type) {
        var parametrizedRandomModel = parametrizedRandom.get(type);
        if (parametrizedRandomModel != null) {
            return parametrizedRandomModel.getName() + "()";
        }

        return switch (type.getMockName()) {
            case "int" -> "random.nextInt()";
            case "double" -> "random.nextDouble()";
            case "float" -> "random.nextFloat()";
            case "boolean" -> "random.nextBoolean()";
            default -> "random.nextObject(" + type.getMockName() + ".class)";
        };
    }
}
