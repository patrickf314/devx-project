package de.devx.project.assertj.condition.generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class AssertjClassFieldTypeModel {

    private Kind kind;
    private String primaryName;
    private String packageName;
    private String className;
    private List<AssertjClassFieldTypeModel> generics;

    public static AssertjClassFieldTypeModel primaryType(String primaryName, String className) {
        return new AssertjClassFieldTypeModel(Kind.PRIMARY, primaryName, "java.lang", className, Collections.emptyList());
    }

    public static AssertjClassFieldTypeModel arrayType(AssertjClassFieldTypeModel componentType) {
        return new AssertjClassFieldTypeModel(Kind.ARRAY, componentType.getPrimaryName(), componentType.getPackageName(), componentType.getClassName(), componentType.getGenerics());
    }

    public static AssertjClassFieldTypeModel objectType(String packageName, String className, List<AssertjClassFieldTypeModel> generics) {
        return new AssertjClassFieldTypeModel(Kind.OBJECT, null, packageName, className, generics);
    }

    public static AssertjClassFieldTypeModel genericType(String name) {
        return new AssertjClassFieldTypeModel(Kind.GENERIC, null, null, name, Collections.emptyList());
    }

    public String getFullType() {
        return getFullType(true);
    }

    public String getFullType(boolean usePrimaryClassName) {
        if (kind == Kind.PRIMARY) {
            return usePrimaryClassName ? primaryName : className;
        }

        var builder = new StringBuilder(className);
        if (!generics.isEmpty()) {
            builder.append("<")
                    .append(
                            generics.stream()
                                    .map(type -> "? extends " + type.getFullType(false))
                                    .collect(Collectors.joining(", "))
                    )
                    .append(">");
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        if (kind == Kind.PRIMARY) {
            return primaryName;
        }

        return getFullType();
    }

    public enum Kind {
        PRIMARY, ARRAY, OBJECT, GENERIC
    }
}
