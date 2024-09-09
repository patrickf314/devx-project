package de.devx.project.hamcrest.matcher.generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class HamcrestClassFieldTypeModel {

    private Kind kind;
    private String primaryName;
    private String packageName;
    private String className;
    private List<HamcrestClassFieldTypeModel> generics;

    public static HamcrestClassFieldTypeModel primaryType(String primaryName, String className) {
        return new HamcrestClassFieldTypeModel(Kind.PRIMARY, primaryName, "java.lang", className, Collections.emptyList());
    }

    public static HamcrestClassFieldTypeModel arrayType(HamcrestClassFieldTypeModel componentType) {
        return new HamcrestClassFieldTypeModel(Kind.ARRAY, componentType.getPrimaryName(), componentType.getPackageName(), componentType.getClassName(), componentType.getGenerics());
    }

    public static HamcrestClassFieldTypeModel objectType(String packageName, String className, List<HamcrestClassFieldTypeModel> generics) {
        return new HamcrestClassFieldTypeModel(Kind.OBJECT, null, packageName, className, generics);
    }

    public static HamcrestClassFieldTypeModel genericType(String name) {
        return new HamcrestClassFieldTypeModel(Kind.GENERIC, null, null, name, Collections.emptyList());
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
