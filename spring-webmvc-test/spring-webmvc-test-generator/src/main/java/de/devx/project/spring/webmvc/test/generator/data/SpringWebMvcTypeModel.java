package de.devx.project.spring.webmvc.test.generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class SpringWebMvcTypeModel {

    public static final SpringWebMvcTypeModel VOID = primary("void");

    private String name;
    private String packageName;
    private List<SpringWebMvcTypeModel> generics;

    public static SpringWebMvcTypeModel generic(String name) {
        return new SpringWebMvcTypeModel(name, null, Collections.emptyList());
    }

    public static SpringWebMvcTypeModel primary(String name) {
        return new SpringWebMvcTypeModel(name, null, Collections.emptyList());
    }

    public static SpringWebMvcTypeModel fromClass(Class<?> clazz) {
        return fromClass(clazz.getName());
    }

    public static SpringWebMvcTypeModel fromClass(String className) {
        return fromClass(className, Collections.emptyList());
    }

    public static SpringWebMvcTypeModel fromClass(String className, List<SpringWebMvcTypeModel> generics) {
        var i = className.lastIndexOf('.');
        var name = className.substring(i + 1);
        var packageName = className.substring(0, i);
        return new SpringWebMvcTypeModel(name, packageName, generics);
    }

    public SpringWebMvcTypeModel getNonPrimaryType() {
        return switch (name) {
            case "int" -> fromClass(Integer.class);
            case "double" -> fromClass(Double.class);
            case "float" -> fromClass(Float.class);
            case "byte" -> fromClass(Byte.class);
            case "short" -> fromClass(Short.class);
            case "boolean" -> fromClass(Boolean.class);
            default -> this;
        };
    }

    public String getFullName() {
        if (generics.isEmpty()) {
            return name;
        }

        return name + "<" + generics.stream().map(SpringWebMvcTypeModel::getFullName).collect(Collectors.joining(", ")) + ">";
    }
}
