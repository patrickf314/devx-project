package de.badminton.neubiberg.service.test.api.dto;

import de.badminton.neubiberg.service.test.api.type.TestType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static de.badminton.neubiberg.service.test.api.dto.TestDTOMatcher.testDTO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class TestDTOTest {

    @Test
    void test() {
        var dto = new TestDTO();
        dto.setId(1);
        dto.setType(TestType.A);
        dto.setMap(Map.of());
        dto.setList(List.of("item"));

        assertThat(dto, is(testDTO()
                .withId(1)
                .withType(TestType.A)
                .withMap(Map.of())
                .withList(List.of("item"))
        ));
    }


}
