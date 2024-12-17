package de.devx.project.commons.generator.utils;

import org.junit.jupiter.api.Test;

import static de.devx.project.commons.generator.utils.ImportUtils.asJavaImport;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ImportUtils}
 */
class ImportUtilsTest {

    @Test
    void testAsJavaImportForJavaLangClass() {
        assertThat(asJavaImport("", "java.lang", "String")).isEmpty();
    }

    @Test
    void testAsJavaImportForSamePackage() {
        assertThat(asJavaImport("de.test", "de.test", "TestClass")).isEmpty();
    }

    @Test
    void testAsJavaImportForOtherPackage() {
        assertThat(asJavaImport("de.test.current", "de.test.db", "TestEntity")).isPresent()
                .get()
                .isEqualTo("de.test.db.TestEntity");
    }

}