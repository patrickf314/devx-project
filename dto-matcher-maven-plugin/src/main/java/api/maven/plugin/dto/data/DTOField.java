package api.maven.plugin.dto.data;

public class DTOField {

    private final DTOFieldType type;
    private final String name;
    private final String getter;

    public DTOField(DTOFieldType type, String name, String getter) {
        this.type = type;
        this.name = name;
        this.getter = getter;
    }

    public DTOFieldType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getGetter() {
        return getter;
    }

    public String getMatcherType() {
        if (type.getKind() == DTOFieldType.Kind.PRIMARY) {
            return "Matcher<" + type.getClassName() + ">";
        }

        return "Matcher<? extends " + type.getFullType() + ">";
    }
}
