package de.devx.project.hamcrest.matcher.generator;

import de.devx.project.commons.test.io.TestSourceFileGenerator;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldModel;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldTypeModel;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestMatcherModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldTypeModel.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class HamcrestMatcherGeneratorTest {

    private static final HamcrestClassFieldTypeModel INT_TYPE = primaryType("int", "Integer");
    private static final HamcrestClassFieldTypeModel STRING_TYPE = objectType("java.lang", "String", emptyList());
    private static final HamcrestClassFieldTypeModel LIST_TYPE = objectType("java.util", "List", List.of(STRING_TYPE));
    private static final HamcrestClassFieldTypeModel MAP_TYPE = objectType("java.util", "Map", List.of(INT_TYPE, LIST_TYPE));
    private static final HamcrestClassFieldTypeModel SET_TYPE = objectType("java.util", "Set", List.of(INT_TYPE));
    private static final HamcrestClassFieldTypeModel OBJECT_TYPE = objectType("de.custom.app.data", "CustomObject", emptyList());

    private final TestSourceFileGenerator sourceFileGenerator = new TestSourceFileGenerator();
    private final HamcrestMatcherGenerator generator = new HamcrestMatcherGenerator(sourceFileGenerator);

    private static Stream<Arguments> types() {
        return Stream.of(
                Arguments.of(INT_TYPE, emptyList()),
                Arguments.of(STRING_TYPE, emptyList()),
                Arguments.of(LIST_TYPE, List.of("java.util.List")),
                Arguments.of(SET_TYPE, List.of("java.util.Set")),
                Arguments.of(MAP_TYPE, List.of("java.util.List", "java.util.Map")),
                Arguments.of(OBJECT_TYPE, List.of("de.custom.app.data.CustomObject")),
                Arguments.of(objectType("java.util", "List", List.of(OBJECT_TYPE)), List.of("de.custom.app.data.CustomObject", "java.util.List")),
                Arguments.of(objectType("de.test.matcher", "ObjectInSamePackage", emptyList()), List.of())
        );
    }

    @MethodSource("types")
    @ParameterizedTest
    void testSimpleMatcher(HamcrestClassFieldTypeModel fieldType, List<String> imports) throws IOException {
        var matcher = new HamcrestMatcherModel();
        matcher.setPackageName("de.test.matcher");
        matcher.setClassName("TestClass");
        matcher.getFields().add(new HamcrestClassFieldModel(fieldType, "testField", "getTestField"));

        generator.generate(matcher);

        var expectedImports = new StringBuilder();
        if (!imports.isEmpty()) {
            imports.forEach(i -> expectedImports.append("\nimport ").append(i).append(";"));
        }

        var generatedMatcher = sourceFileGenerator.getFileContent(matcher.getPackageName(), "TestClassMatcher");
        assertThat(generatedMatcher.isPresent(), is(true));
        assertThat(generatedMatcher.get(), is("""
                package de.test.matcher;
                %3$s
                import org.hamcrest.Matcher;
                import org.hamcrest.Description;
                import org.hamcrest.TypeSafeMatcher;
                import org.hamcrest.core.IsAnything;
                                
                import static org.hamcrest.core.Is.is;
                                
                public class TestClassMatcher extends TypeSafeMatcher<TestClass> {
                                
                    private final Matcher<%1$s> testField;
                                
                    private TestClassMatcher() {
                        this.testField = new IsAnything<>();
                    }
                                
                    private TestClassMatcher(Matcher<%1$s> testField) {
                        this.testField = testField;
                    }
                                
                    public static TestClassMatcher testClass() {
                        return new TestClassMatcher();
                    }
                                
                    @Override
                    protected boolean matchesSafely(TestClass item) {
                        return this.testField.matches(item.getTestField());
                    }
                                
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("TestClass(")
                            .appendText("testField=").appendDescriptionOf(this.testField)
                            .appendText(")");
                    }
                     
                    public TestClassMatcher withTestField(%2$s testField) {
                        return withTestField(is(testField));
                    }
                             
                    public TestClassMatcher withTestField(Matcher<%1$s> testField) {
                        return new TestClassMatcher(testField);
                    }
                }""".formatted(fieldType.getFullType(false), fieldType.getFullType(true), expectedImports)
        ));
    }

    @Test
    void testGenericMatcher() throws IOException {
        var matcher = new HamcrestMatcherModel();
        matcher.setPackageName("de.test.matcher");
        matcher.setClassName("TestClass");
        matcher.setGenerics("<T>");
        matcher.getFields().add(new HamcrestClassFieldModel(genericType("T"), "testField", "getTestField"));

        generator.generate(matcher);

        var generatedMatcher = sourceFileGenerator.getFileContent(matcher.getPackageName(), "TestClassMatcher");
        assertThat(generatedMatcher.isPresent(), is(true));
        assertThat(generatedMatcher.get(), is("""
                package de.test.matcher;
                                
                import org.hamcrest.Matcher;
                import org.hamcrest.Description;
                import org.hamcrest.TypeSafeMatcher;
                import org.hamcrest.core.IsAnything;
                                
                import static org.hamcrest.core.Is.is;
                                                                
                public class TestClassMatcher<T> extends TypeSafeMatcher<TestClass<T>> {
                                        
                    private final Matcher<T> testField;
                                
                    private TestClassMatcher() {
                        this.testField = new IsAnything<>();
                    }
                                
                    private TestClassMatcher(Matcher<T> testField) {
                        this.testField = testField;
                    }
                                
                    public static <T> TestClassMatcher<T> testClass() {
                        return new TestClassMatcher<T>();
                    }
                                
                    @Override
                    protected boolean matchesSafely(TestClass<T> item) {
                        return this.testField.matches(item.getTestField());
                    }
                                
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("TestClass(")
                            .appendText("testField=").appendDescriptionOf(this.testField)
                            .appendText(")");
                    }
                     
                    public TestClassMatcher<T> withTestField(T testField) {
                        return withTestField(is(testField));
                    }
                             
                    public TestClassMatcher<T> withTestField(Matcher<T> testField) {
                        return new TestClassMatcherTestClass(testField);
                    }
                }"""));
    }
}