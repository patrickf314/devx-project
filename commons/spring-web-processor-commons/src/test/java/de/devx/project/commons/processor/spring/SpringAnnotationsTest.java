package de.devx.project.commons.processor.spring;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link SpringAnnotations}
 */
class SpringAnnotationsTest {

    private static Stream<Arguments> testCorrectSpringAnnotations() {
        return Stream.of(
                Arguments.of(SpringAnnotations.REQUEST_MAPPING, RequestMapping.class),
                Arguments.of(SpringAnnotations.GET_MAPPING, GetMapping.class),
                Arguments.of(SpringAnnotations.POST_MAPPING, PostMapping.class),
                Arguments.of(SpringAnnotations.PUT_MAPPING, PutMapping.class),
                Arguments.of(SpringAnnotations.DELETE_MAPPING, DeleteMapping.class),

                Arguments.of(SpringAnnotations.REQUEST_PARAM, RequestParam.class),
                Arguments.of(SpringAnnotations.PATH_VARIABLE, PathVariable.class),
                Arguments.of(SpringAnnotations.REQUEST_HEADER, RequestHeader.class),
                Arguments.of(SpringAnnotations.REQUEST_BODY, RequestBody.class)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testCorrectSpringAnnotations(String actual, Class<?> expected) {
        assertThat(actual).isEqualTo(expected.getName());
    }
}