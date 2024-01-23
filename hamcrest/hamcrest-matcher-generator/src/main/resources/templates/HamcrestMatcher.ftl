<#-- @ftlvariable name="matcher" type="de.devx.project.hamcrest.matcher.generator.data.HamcrestMatcherModel" -->
<#-- @ftlvariable name="imports" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="matcherFactoryFunctionName" type="java.lang.String" -->
package ${matcher.packageName};

<#list imports as import>
import ${import};
</#list>
import org.hamcrest.Matcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsAnything;

import static org.hamcrest.core.Is.is;

public class ${matcher.className}Matcher${matcher.generics} extends TypeSafeMatcher<${matcher.className}${matcher.generics}> {

<#list matcher.fields as field>
    private final ${field.matcherType} ${field.name};
</#list>

    private ${matcher.className}Matcher() {
    <#list matcher.fields as field>
        this.${field.name} = new IsAnything<>();
    </#list>
    }

    private ${matcher.className}Matcher(${matcher.fields?map(field -> field.matcherType + " " + field.name)?join(", ")}) {
    <#list matcher.fields as field>
        this.${field.name} = ${field.name};
    </#list>
    }

    public static ${matcher.generics?has_content?then(matcher.generics + ' ', '')}${matcher.className}Matcher${matcher.generics} ${matcherFactoryFunctionName}() {
        return new ${matcher.className}Matcher${matcher.generics}();
    }

    @Override
    protected boolean matchesSafely(${matcher.className}${matcher.generics} item) {
        return ${matcher.fields?map(field -> "this." + field.name + ".matches(item." + field.getter + "())")?join("\n\t\t\t&& ")};
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(${"\"" + matcher.className + "(\""})
            ${matcher.fields?map(field -> ".appendText(\"" + field.name + "=\").appendDescriptionOf(this." + field.name + ")")?join(".appendText(\", \")\n\t\t\t")}
            .appendText(${"\")\""});
    }
<#list matcher.fields as field>

    public ${matcher.className}Matcher${matcher.generics} with${field.name?cap_first}(${field.type.fullType} ${field.name}) {
        return with${field.name?cap_first}(is(${field.name}));
    }

    public ${matcher.className}Matcher${matcher.generics} with${field.name?cap_first}(${field.matcherType} ${field.name}) {
        return new ${matcher.className}Matcher${matcher.className}(${matcher.fields?map(field -> field.name)?join(", ")});
    }
</#list>
}