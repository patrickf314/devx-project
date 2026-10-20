package de.devx.project.assertj.assertion.gennerator;

import de.devx.project.assertj.assertion.gennerator.AssertJFactoryNameResolver.Factory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link AssertJFactoryNameResolver}
 */
class AssertJFactoryNameResolverTest {

    @Test
    void testUniqueNamesAreKept() {
        assertThat(AssertJFactoryNameResolver.resolve(List.of(
                factory("SECTION_DTO", "a.dto"),
                factory("MEMBER_DTO", "a.dto")
        ))).containsExactly("SECTION_DTO", "MEMBER_DTO");
    }

    @Test
    void testFirstDistinguishingSegmentFromTheRight() {
        assertThat(AssertJFactoryNameResolver.resolve(List.of(
                factory("SECTION_DTO", "de.tsv.neubiberg.club.api.section.dto"),
                factory("SECTION_DTO", "de.tsv.neubiberg.badminton.api.section.dto")
        ))).containsExactly("CLUB_SECTION_DTO", "BADMINTON_SECTION_DTO");
    }

    @Test
    void testThreeClashingNames() {
        assertThat(AssertJFactoryNameResolver.resolve(List.of(
                factory("SECTION_DTO", "x.club.dto"),
                factory("SECTION_DTO", "x.badminton.dto"),
                factory("SECTION_DTO", "x.tennis.dto")
        ))).containsExactly("CLUB_SECTION_DTO", "BADMINTON_SECTION_DTO", "TENNIS_SECTION_DTO");
    }

    @Test
    void testNoSingleSegmentDistinguishesFromAllOthers() {
        assertThatThrownBy(() -> AssertJFactoryNameResolver.resolve(List.of(
                factory("SECTION_DTO", "x.club.dto"),
                factory("SECTION_DTO", "x.badminton.dto"),
                factory("SECTION_DTO", "y.badminton.v2")
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("x.badminton.dto");
    }

    @Test
    void testConfiguredPrefix() {
        assertThat(AssertJFactoryNameResolver.resolve(List.of(
                new Factory("SECTION_DTO", "a.dto", Optional.of("Club")),
                factory("SECTION_DTO", "b.a.dto")
        ))).containsExactly("CLUB_SECTION_DTO", "B_SECTION_DTO");
    }

    @Test
    void testNoDistinguishingSegment() {
        assertThatThrownBy(() -> AssertJFactoryNameResolver.resolve(List.of(
                factory("SECTION_DTO", "a.dto"),
                factory("SECTION_DTO", "b.a.dto")
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SECTION_DTO")
                .hasMessageContaining("a.dto");
    }

    @Test
    void testPrefixedNameClashesWithExistingName() {
        assertThatThrownBy(() -> AssertJFactoryNameResolver.resolve(List.of(
                factory("SECTION_DTO", "a.club"),
                factory("SECTION_DTO", "a.badminton"),
                factory("CLUB_SECTION_DTO", "a.dto")
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLUB_SECTION_DTO");
    }

    private Factory factory(String name, String packageName) {
        return new Factory(name, packageName, Optional.empty());
    }
}
