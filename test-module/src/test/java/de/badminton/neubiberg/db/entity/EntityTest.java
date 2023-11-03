package de.badminton.neubiberg.db.entity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static de.badminton.neubiberg.db.entity.TestEntityMatcher.testEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class EntityTest {

    @Test
    void test() {
        var entity = new TestEntity();
        entity.setId(1);
        entity.setVersion(2);
        entity.setMap(Map.of("key", "value"));
        entity.setChildren(List.of());

        assertThat(entity, is(testEntity()
                .withId(1)
                .withVersion(2)
                .withMap(Map.of("key", "value"))
                .withChildren(List.of())
        ));
    }
}