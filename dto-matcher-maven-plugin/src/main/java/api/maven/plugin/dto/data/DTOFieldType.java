package api.maven.plugin.dto.data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DTOFieldType {

    private final Kind kind;
    private final String primaryName;
    private final String packageName;
    private final String className;
    private final List<DTOFieldType> generics;

    public DTOFieldType(Kind kind, String primaryName, String packageName, String className, List<DTOFieldType> generics) {
        this.kind = kind;
        this.primaryName = primaryName;
        this.packageName = packageName;
        this.className = className;
        this.generics = generics;
    }

    public static DTOFieldType primaryType(String primaryName, String className) {
        return new DTOFieldType(Kind.PRIMARY, primaryName, "java.lang", className, Collections.emptyList());
    }

    public static DTOFieldType arrayType(DTOFieldType componentType) {
        return new DTOFieldType(Kind.ARRAY, componentType.getPrimaryName(), componentType.getPackageName(), componentType.getClassName(), componentType.getGenerics());
    }

    public static DTOFieldType objectType(String packageName, String className, List<DTOFieldType> generics) {
        return new DTOFieldType(Kind.OBJECT, null, packageName, className, generics);
    }

    public static DTOFieldType genericType(String name) {
        return new DTOFieldType(Kind.GENERIC, null, null, name, Collections.emptyList());
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
                                    .map(DTOFieldType::getFullType)
                                    .map(type -> "? extends " + type)
                                    .collect(Collectors.joining(", "))
                    )
                    .append(">");
        }
        return builder.toString();
    }

    public List<DTOFieldType> getGenerics() {
        return Collections.unmodifiableList(generics);
    }

    public enum Kind {
        PRIMARY, ARRAY, OBJECT, GENERIC
    }
}
