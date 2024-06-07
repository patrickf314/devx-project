package de.devx.project.spring.webmvc.test.generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class SpringWebMvcTypeModel {

    public static final SpringWebMvcTypeModel VOID = primitive("void");

    private String name;
    private String packageName;
    private List<SpringWebMvcTypeModel> generics;

    public static SpringWebMvcTypeModel generic(String name) {
        return new SpringWebMvcTypeModel(name, null, Collections.emptyList());
    }

    public static SpringWebMvcTypeModel primitive(String name) {
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

    public static SpringWebMvcTypeModel array(SpringWebMvcTypeModel component) {
        return new SpringWebMvcTypeModel(null, null, List.of(component));
    }

    public boolean isArray() {
        return name == null && packageName == null;
    }

    public String getMockName() {
        return isMultipartFile() ? "MockMultipartFile" : name;
    }

    public String getMockPackageName() {
        return isMultipartFile() ? "org.springframework.mock.web" : packageName;
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
        if (isArray()) {
            return generics.get(0).getFullName() + "[]";
        }

        if (generics.isEmpty()) {
            return getMockName();
        }

        return getMockName() + "<" + generics.stream().map(SpringWebMvcTypeModel::getFullName).collect(Collectors.joining(", ")) + ">";
    }

    public boolean isMultipartFile() {
        return isClass(MultipartFile.class);
    }

    public boolean isClass(Class<?> clazz) {
        if(isArray()) {
            return clazz.isArray() && generics.get(0).isClass(clazz.getComponentType());
        }

        if(packageName == null) {
            return clazz.isPrimitive() && clazz.getName().equals(name);
        }

        return clazz.getName().equals(packageName + "." + name);
    }
}
