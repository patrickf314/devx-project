package de.devx.project.assertj.assertion.gennerator;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertFieldModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertionModel;
import de.devx.project.assertj.assertion.gennerator.mapper.AssertJAssertMapper;
import de.devx.project.commons.generator.model.JavaTypeArgumentModel;
import de.devx.project.commons.generator.model.JavaTypeModel;
import de.devx.project.commons.test.io.TestSourceFileGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static de.devx.project.commons.generator.model.JavaTypeModel.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link AssertJAssertionGenerator}
 */
class AssertJAssertionGeneratorTest {

    public static final String PACKAGE_NAME = "de.devx.project.assertj.assertion.test";

    private final TestSourceFileGenerator sourceFileGenerator = new TestSourceFileGenerator();

    private final AssertJAssertionGenerator sut = new AssertJAssertionGenerator(sourceFileGenerator);

    @BeforeEach
    void setUp() {
        sourceFileGenerator.reset();
    }

    @Test
    void testAssertionsGeneration() throws IOException {
        var model = new AssertJAssertionModel();
        model.setPackageName("de.devx.project.assertj.assertion");
        model.setName("EntityAssertions");
        model.setAsserts(List.of(assertModel("TestEntity"), assertModel("AnotherTestEntity")));

        sut.generateAssertions(model);

        assertThat(sourceFileGenerator.getCreatedSourceFileNames())
                .containsExactlyInAnyOrder(
                        "de.devx.project.assertj.assertion.EntityAssertions",
                        PACKAGE_NAME + ".AbstractTestEntityAssert",
                        PACKAGE_NAME + ".TestEntityAssert",
                        PACKAGE_NAME + ".AbstractAnotherTestEntityAssert",
                        PACKAGE_NAME + ".AnotherTestEntityAssert"
                );

        assertThat(sourceFileGenerator.getFileContent("de.devx.project.assertj.assertion", "EntityAssertions"))
                .isPresent()
                .get()
                .isEqualTo("""
                        package de.devx.project.assertj.assertion;
                        
                        import de.devx.project.assertj.assertion.test.AnotherTestEntity;
                        import de.devx.project.assertj.assertion.test.AnotherTestEntityAssert;
                        import de.devx.project.assertj.assertion.test.TestEntity;
                        import de.devx.project.assertj.assertion.test.TestEntityAssert;
                        import org.assertj.core.api.Assertions;
                        
                        /**
                         * Autogenerated extension of the assertJ {@link Assertions}
                         * allowing for typed asserts of objects
                         */
                        public class EntityAssertions extends Assertions {
                        
                            /**
                             * Factory method for an assert of a {@link TestEntity}.
                             *
                             * @param actual the actual object
                             * @return the typed assert
                             */
                            public static TestEntityAssert assertThat(TestEntity actual) {
                                return new TestEntityAssert(actual);
                            }
                        
                            /**
                             * Factory method for an assert of a {@link AnotherTestEntity}.
                             *
                             * @param actual the actual object
                             * @return the typed assert
                             */
                            public static AnotherTestEntityAssert assertThat(AnotherTestEntity actual) {
                                return new AnotherTestEntityAssert(actual);
                            }
                        }""");
    }

    @Test
    void testAssertionsGenerationWithGenericAssert() throws IOException {
        var model = new AssertJAssertionModel();
        model.setPackageName("de.devx.project.assertj.assertion");
        model.setName("EntityAssertions");
        model.setAsserts(List.of(genericAssertModel()));

        sut.generateAssertions(model);

        assertThat(sourceFileGenerator.getFileContent("de.devx.project.assertj.assertion", "EntityAssertions"))
                .isPresent()
                .get()
                .isEqualTo("""
                        package de.devx.project.assertj.assertion;
                        
                        import de.devx.project.assertj.assertion.test.TestEntity;
                        import de.devx.project.assertj.assertion.test.TestEntityAssert;
                        import org.assertj.core.api.Assertions;
                        
                        /**
                         * Autogenerated extension of the assertJ {@link Assertions}
                         * allowing for typed asserts of objects
                         */
                        public class EntityAssertions extends Assertions {
                        
                            /**
                             * Factory method for an assert of a {@link TestEntity}.
                             *
                             * @param actual the actual object
                             * @return the typed assert
                             */
                            public static <T> TestEntityAssert<T> assertThat(TestEntity<T> actual) {
                                return new TestEntityAssert<>(actual);
                            }
                        }""");
    }

    @Test
    void testAssertGeneration() throws IOException {
        var model = assertModel("TestEntity");

        sut.generateAssert(model);

        assertThat(sourceFileGenerator.getFileContent(PACKAGE_NAME, "TestEntityAssert"))
                .isPresent()
                .get()
                .isEqualTo("""
                        package de.devx.project.assertj.assertion.test;
                        
                        /**
                         * Autogenerated assert of the {@link TestEntity}.
                         *
                         * @see AbstractTestEntityAssert for extending this assert
                         */
                        public final class TestEntityAssert extends AbstractTestEntityAssert<TestEntityAssert, TestEntity> {
                        
                            /**
                             * Autogenerated constructor
                             *
                             * @param actual the actual value
                             */
                            public TestEntityAssert(TestEntity actual) {
                                super(actual, TestEntityAssert.class);
                            }
                        }"""
                );
    }

    @Test
    void testAssertGenerationOfGenericModel() throws IOException {
        var model = genericAssertModel();

        sut.generateAssert(model);

        assertThat(sourceFileGenerator.getFileContent(PACKAGE_NAME, "TestEntityAssert"))
                .isPresent()
                .get()
                .isEqualTo("""
                        package de.devx.project.assertj.assertion.test;
                        
                        /**
                         * Autogenerated assert of the {@link TestEntity}.
                         *
                         * @see AbstractTestEntityAssert for extending this assert
                         */
                        public final class TestEntityAssert<T> extends AbstractTestEntityAssert<T, TestEntityAssert<T>, TestEntity<T>> {
                        
                            /**
                             * Autogenerated constructor
                             *
                             * @param actual the actual value
                             */
                            public TestEntityAssert(TestEntity<T> actual) {
                                super(actual, TestEntityAssert.class);
                            }
                        }"""
                );
    }

    @Test
    void testAbstractAssertGeneration() throws IOException {
        var model = assertModel("TestEntity");

        sut.generateAbstractAssert(emptyMap(), model);

        assertThat(sourceFileGenerator.getFileContent("de.devx.project.assertj.assertion.test", "AbstractTestEntityAssert"))
                .isPresent()
                .get()
                .isEqualTo("""
                        package de.devx.project.assertj.assertion.test;
                        
                        import java.util.List;
                        import java.util.Objects;
                        import java.util.function.Consumer;
                        import java.utils.List;
                        import org.assertj.core.api.AbstractAssert;
                        import org.assertj.core.description.TextDescription;
                        import org.assertj.core.error.MultipleAssertionsError;
                        
                        /**
                         * Autogenerated abstract assert of the {@link TestEntity}.
                         * The abstract assert is used for extensions.
                         * Use {@link TestEntityAssert} as implementation.
                         */
                        public class AbstractTestEntityAssert<SELF extends AbstractTestEntityAssert<SELF, ACTUAl>, ACTUAl extends TestEntity>
                            extends AbstractAssert<SELF, ACTUAl> {
                        
                            /**
                             * Autogenerated constructor
                             *
                             * @param actual the actual value
                             * @param selfType the class of the implementation of this abstract assert
                             */
                            protected AbstractTestEntityAssert(ACTUAl actual, Class<?> selfType) {
                                super(actual, selfType);
                            }
                        
                            /**
                             * Checks if id of the actual value is equal to the given value.
                             *
                             * @param expected the expected value of field
                             * @return this
                             */
                            public SELF hasId(int expected) {
                                isNotNull();
                                if (!Objects.equals(actual.getId(), expected)) {
                                    failWithActualExpectedAndMessage(actual.getId(), expected, "TestEntity.id");
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if id of the actual value satisfies
                             * all the assertions of the given consumer.
                             *
                             * @param consumer the consumer performing assertions
                             * @return this
                             */
                            public SELF hasIdSatisfying(Consumer<? super Integer> consumer) {
                                isNotNull();
                                try {
                                    consumer.accept(actual.getId());
                                } catch (AssertionError e) {
                                    throw new MultipleAssertionsError(new TextDescription("TestEntity.id"), List.of(e));
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if version of the actual value is equal to the given value.
                             *
                             * @param expected the expected value of field
                             * @return this
                             */
                            public SELF hasVersion(int expected) {
                                isNotNull();
                                if (!Objects.equals(actual.getVersion(), expected)) {
                                    failWithActualExpectedAndMessage(actual.getVersion(), expected, "TestEntity.version");
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if version of the actual value satisfies
                             * all the assertions of the given consumer.
                             *
                             * @param consumer the consumer performing assertions
                             * @return this
                             */
                            public SELF hasVersionSatisfying(Consumer<? super Integer> consumer) {
                                isNotNull();
                                try {
                                    consumer.accept(actual.getVersion());
                                } catch (AssertionError e) {
                                    throw new MultipleAssertionsError(new TextDescription("TestEntity.version"), List.of(e));
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if name of the actual value is equal to the given value.
                             *
                             * @param expected the expected value of field
                             * @return this
                             */
                            public SELF hasName(String expected) {
                                isNotNull();
                                if (!Objects.equals(actual.getName(), expected)) {
                                    failWithActualExpectedAndMessage(actual.getName(), expected, "TestEntity.name");
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if name of the actual value satisfies
                             * all the assertions of the given consumer.
                             *
                             * @param consumer the consumer performing assertions
                             * @return this
                             */
                            public SELF hasNameSatisfying(Consumer<? super String> consumer) {
                                isNotNull();
                                try {
                                    consumer.accept(actual.getName());
                                } catch (AssertionError e) {
                                    throw new MultipleAssertionsError(new TextDescription("TestEntity.name"), List.of(e));
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if tests of the actual value is equal to the given value.
                             *
                             * @param expected the expected value of field
                             * @return this
                             */
                            public SELF hasTests(List<String> expected) {
                                isNotNull();
                                if (!Objects.equals(actual.getTests(), expected)) {
                                    failWithActualExpectedAndMessage(actual.getTests(), expected, "TestEntity.tests");
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if tests of the actual value satisfies
                             * all the assertions of the given consumer.
                             *
                             * @param consumer the consumer performing assertions
                             * @return this
                             */
                            public SELF hasTestsSatisfying(Consumer<? super List<String>> consumer) {
                                isNotNull();
                                try {
                                    consumer.accept(actual.getTests());
                                } catch (AssertionError e) {
                                    throw new MultipleAssertionsError(new TextDescription("TestEntity.tests"), List.of(e));
                                }
                                return this.myself;
                            }
                        }""");
    }

    @Test
    void testAbstractAssertGenerationOfGenericModel() throws IOException {
        var model = genericAssertModel();

        sut.generateAbstractAssert(emptyMap(), model);

        assertThat(sourceFileGenerator.getFileContent("de.devx.project.assertj.assertion.test", "AbstractTestEntityAssert"))
                .isPresent()
                .get()
                .isEqualTo("""
                        package de.devx.project.assertj.assertion.test;
                        
                        import java.util.List;
                        import java.util.Objects;
                        import java.util.function.Consumer;
                        import org.assertj.core.api.AbstractAssert;
                        import org.assertj.core.description.TextDescription;
                        import org.assertj.core.error.MultipleAssertionsError;
                        
                        /**
                         * Autogenerated abstract assert of the {@link TestEntity}.
                         * The abstract assert is used for extensions.
                         * Use {@link TestEntityAssert} as implementation.
                         */
                        public class AbstractTestEntityAssert<T, SELF extends AbstractTestEntityAssert<T, SELF, ACTUAl>, ACTUAl extends TestEntity<T>>
                            extends AbstractAssert<SELF, ACTUAl> {
                        
                            /**
                             * Autogenerated constructor
                             *
                             * @param actual the actual value
                             * @param selfType the class of the implementation of this abstract assert
                             */
                            protected AbstractTestEntityAssert(ACTUAl actual, Class<?> selfType) {
                                super(actual, selfType);
                            }
                        
                            /**
                             * Checks if item of the actual value is equal to the given value.
                             *
                             * @param expected the expected value of field
                             * @return this
                             */
                            public SELF hasItem(T expected) {
                                isNotNull();
                                if (!Objects.equals(actual.getItem(), expected)) {
                                    failWithActualExpectedAndMessage(actual.getItem(), expected, "TestEntity.item");
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if item of the actual value satisfies
                             * all the assertions of the given consumer.
                             *
                             * @param consumer the consumer performing assertions
                             * @return this
                             */
                            public SELF hasItemSatisfying(Consumer<? super T> consumer) {
                                isNotNull();
                                try {
                                    consumer.accept(actual.getItem());
                                } catch (AssertionError e) {
                                    throw new MultipleAssertionsError(new TextDescription("TestEntity.item"), List.of(e));
                                }
                                return this.myself;
                            }
                        }""");
    }

    @Test
    void testAbstractAssertGenerationOfRecord() throws IOException {
        var model = assertModel("TestEntity");
        model.setJavaRecord(true);

        sut.generateAbstractAssert(emptyMap(), model);

        assertThat(sourceFileGenerator.getFileContent("de.devx.project.assertj.assertion.test", "AbstractTestEntityAssert"))
                .isPresent()
                .get()
                .isEqualTo("""
                        package de.devx.project.assertj.assertion.test;
                        
                        import java.util.List;
                        import java.util.Objects;
                        import java.util.function.Consumer;
                        import java.utils.List;
                        import org.assertj.core.api.AbstractAssert;
                        import org.assertj.core.description.TextDescription;
                        import org.assertj.core.error.MultipleAssertionsError;
                        
                        /**
                         * Autogenerated abstract assert of the {@link TestEntity}.
                         * The abstract assert is used for extensions.
                         * Use {@link TestEntityAssert} as implementation.
                         */
                        public class AbstractTestEntityAssert<SELF extends AbstractTestEntityAssert<SELF, ACTUAl>, ACTUAl extends TestEntity>
                            extends AbstractAssert<SELF, ACTUAl> {
                        
                            /**
                             * Autogenerated constructor
                             *
                             * @param actual the actual value
                             * @param selfType the class of the implementation of this abstract assert
                             */
                            protected AbstractTestEntityAssert(ACTUAl actual, Class<?> selfType) {
                                super(actual, selfType);
                            }
                        
                            /**
                             * Checks if id of the actual value is equal to the given value.
                             *
                             * @param expected the expected value of field
                             * @return this
                             */
                            public SELF hasId(int expected) {
                                isNotNull();
                                if (!Objects.equals(actual.id(), expected)) {
                                    failWithActualExpectedAndMessage(actual.id(), expected, "TestEntity.id");
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if id of the actual value satisfies
                             * all the assertions of the given consumer.
                             *
                             * @param consumer the consumer performing assertions
                             * @return this
                             */
                            public SELF hasIdSatisfying(Consumer<? super Integer> consumer) {
                                isNotNull();
                                try {
                                    consumer.accept(actual.id());
                                } catch (AssertionError e) {
                                    throw new MultipleAssertionsError(new TextDescription("TestEntity.id"), List.of(e));
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if version of the actual value is equal to the given value.
                             *
                             * @param expected the expected value of field
                             * @return this
                             */
                            public SELF hasVersion(int expected) {
                                isNotNull();
                                if (!Objects.equals(actual.version(), expected)) {
                                    failWithActualExpectedAndMessage(actual.version(), expected, "TestEntity.version");
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if version of the actual value satisfies
                             * all the assertions of the given consumer.
                             *
                             * @param consumer the consumer performing assertions
                             * @return this
                             */
                            public SELF hasVersionSatisfying(Consumer<? super Integer> consumer) {
                                isNotNull();
                                try {
                                    consumer.accept(actual.version());
                                } catch (AssertionError e) {
                                    throw new MultipleAssertionsError(new TextDescription("TestEntity.version"), List.of(e));
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if name of the actual value is equal to the given value.
                             *
                             * @param expected the expected value of field
                             * @return this
                             */
                            public SELF hasName(String expected) {
                                isNotNull();
                                if (!Objects.equals(actual.name(), expected)) {
                                    failWithActualExpectedAndMessage(actual.name(), expected, "TestEntity.name");
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if name of the actual value satisfies
                             * all the assertions of the given consumer.
                             *
                             * @param consumer the consumer performing assertions
                             * @return this
                             */
                            public SELF hasNameSatisfying(Consumer<? super String> consumer) {
                                isNotNull();
                                try {
                                    consumer.accept(actual.name());
                                } catch (AssertionError e) {
                                    throw new MultipleAssertionsError(new TextDescription("TestEntity.name"), List.of(e));
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if tests of the actual value is equal to the given value.
                             *
                             * @param expected the expected value of field
                             * @return this
                             */
                            public SELF hasTests(List<String> expected) {
                                isNotNull();
                                if (!Objects.equals(actual.tests(), expected)) {
                                    failWithActualExpectedAndMessage(actual.tests(), expected, "TestEntity.tests");
                                }
                                return this.myself;
                            }
                        
                            /**
                             * Checks if tests of the actual value satisfies
                             * all the assertions of the given consumer.
                             *
                             * @param consumer the consumer performing assertions
                             * @return this
                             */
                            public SELF hasTestsSatisfying(Consumer<? super List<String>> consumer) {
                                isNotNull();
                                try {
                                    consumer.accept(actual.tests());
                                } catch (AssertionError e) {
                                    throw new MultipleAssertionsError(new TextDescription("TestEntity.tests"), List.of(e));
                                }
                                return this.myself;
                            }
                        }""");
    }

    private AssertJAssertModel genericAssertModel() {
        return assertModel("TestEntity", List.of(
                fieldModel("item", genericTemplateType("T"))
        ), List.of(new JavaTypeArgumentModel("T")));
    }

    private AssertJAssertModel assertModel(String name) {
        return assertModel(name, List.of(
                fieldModel("id", primitiveType("int", "Integer")),
                fieldModel("version", primitiveType("int", "Integer")),
                fieldModel("name", objectType("java.lang", "String")),
                fieldModel("tests", objectType("java.utils", "List", List.of(
                        objectType("java.lang", "String")
                )))
        ), emptyList());
    }

    private AssertJAssertModel assertModel(String TestEntity, List<AssertJAssertFieldModel> fields, List<JavaTypeArgumentModel> typeParameters) {
        var model = new AssertJAssertModel();
        model.setPackageName(PACKAGE_NAME);
        model.setName(TestEntity);
        model.setFields(fields);
        model.setTypeArguments(typeParameters);
        model.setExtendedAbstractAssertModel(AssertJAssertMapper.ABSTRACT_ASSERT);
        return model;
    }

    private AssertJAssertFieldModel fieldModel(String name, JavaTypeModel type) {
        var model = new AssertJAssertFieldModel();
        model.setName(name);
        model.setType(type);
        return model;
    }
}