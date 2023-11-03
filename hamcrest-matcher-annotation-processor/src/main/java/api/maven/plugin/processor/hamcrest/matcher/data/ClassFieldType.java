package api.maven.plugin.processor.hamcrest.matcher.data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClassFieldType {

    private final Kind kind;
    private final String primaryName;
    private final String packageName;
    private final String className;
    private final List<ClassFieldType> generics;

    public ClassFieldType(Kind kind, String primaryName, String packageName, String className, List<ClassFieldType> generics) {
        this.kind = kind;
        this.primaryName = primaryName;
        this.packageName = packageName;
        this.className = className;
        this.generics = generics;
    }

    public static ClassFieldType primaryType(String primaryName, String className) {
        return new ClassFieldType(Kind.PRIMARY, primaryName, "java.lang", className, Collections.emptyList());
    }

    public static ClassFieldType arrayType(ClassFieldType componentType) {
        return new ClassFieldType(Kind.ARRAY, componentType.getPrimaryName(), componentType.getPackageName(), componentType.getClassName(), componentType.getGenerics());
    }

    public static ClassFieldType objectType(String packageName, String className, List<ClassFieldType> generics) {
        return new ClassFieldType(Kind.OBJECT, null, packageName, className, generics);
    }

    public static ClassFieldType genericType(String name) {
        return new ClassFieldType(Kind.GENERIC, null, null, name, Collections.emptyList());
    }

    public Kind getKind() {
        return kind;
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getFullType() {
        if (kind == Kind.PRIMARY) {
            return primaryName;
        }

        var builder = new StringBuilder(className);
        if (!generics.isEmpty()) {
            builder.append("<")
                    .append(
                            generics.stream()
                                    .map(ClassFieldType::getFullType)
                                    .collect(Collectors.joining(", "))
                    )
                    .append(">");
        }
        return builder.toString();
    }

    public List<ClassFieldType> getGenerics() {
        return Collections.unmodifiableList(generics);
    }

    public enum Kind {
        PRIMARY, ARRAY, OBJECT, GENERIC
    }
}
