package api.maven.plugin.processor.entity.data;

public class EntityField {

    private final EntityFieldType type;
    private final String name;
    private final String getter;

    public EntityField(EntityFieldType type, String name, String getter) {
        this.type = type;
        this.name = name;
        this.getter = getter;
    }

    public EntityFieldType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getGetter() {
        return getter;
    }

    public String getMatcherType() {
        if (type.getKind() == EntityFieldType.Kind.PRIMARY) {
            return "Matcher<" + type.getClassName() + ">";
        }

        return "Matcher<" + type.getFullType() + ">";
    }
}
