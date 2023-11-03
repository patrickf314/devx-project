package api.maven.plugin.processor.hamcrest.matcher.data;

public class ClassField {

    private final ClassFieldType type;
    private final String name;
    private final String getter;

    public ClassField(ClassFieldType type, String name, String getter) {
        this.type = type;
        this.name = name;
        this.getter = getter;
    }

    public ClassFieldType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getGetter() {
        return getter;
    }

    public String getMatcherType() {
        if (type.getKind() == ClassFieldType.Kind.PRIMARY) {
            return "Matcher<" + type.getClassName() + ">";
        }

        return "Matcher<" + type.getFullType() + ">";
    }
}
