package api.maven.plugin.processor.entity.data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EntityFieldType {

    private final Kind kind;
    private final String primaryName;
    private final String packageName;
    private final String className;
    private final List<EntityFieldType> generics;

    public EntityFieldType(Kind kind, String primaryName, String packageName, String className, List<EntityFieldType> generics) {
        this.kind = kind;
        this.primaryName = primaryName;
        this.packageName = packageName;
        this.className = className;
        this.generics = generics;
    }

    public static EntityFieldType primaryType(String primaryName, String className) {
        return new EntityFieldType(Kind.PRIMARY, primaryName, "java.lang", className, Collections.emptyList());
    }

    public static EntityFieldType arrayType(EntityFieldType componentType) {
        return new EntityFieldType(Kind.ARRAY, componentType.getPrimaryName(), componentType.getPackageName(), componentType.getClassName(), componentType.getGenerics());
    }

    public static EntityFieldType objectType(String packageName, String className, List<EntityFieldType> generics) {
        return new EntityFieldType(Kind.OBJECT, null, packageName, className, generics);
    }

    public static EntityFieldType genericType(String name) {
        return new EntityFieldType(Kind.GENERIC, null, null, name, Collections.emptyList());
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
                                    .map(EntityFieldType::getFullType)
                                    .collect(Collectors.joining(", "))
                    )
                    .append(">");
        }
        return builder.toString();
    }

    public List<EntityFieldType> getGenerics() {
        return Collections.unmodifiableList(generics);
    }

    public enum Kind {
        PRIMARY, ARRAY, OBJECT, GENERIC
    }
}
