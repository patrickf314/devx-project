package de.devx.project.commons.generator.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link StringUtils}
 */
class StringUtilsTest {

    @Test
    void testCapitalize() {
        assertThat(StringUtils.capitalize("test")).isEqualTo("Test");
    }

}