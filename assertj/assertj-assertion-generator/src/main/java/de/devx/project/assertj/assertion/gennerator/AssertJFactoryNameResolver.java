package de.devx.project.assertj.assertion.gennerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the names of the {@code InstanceOfAssertFactory} fields of a generated assertion class.
 * <p>
 * Unique names are kept. Names clashing with each other (e.g. {@code SECTION_DTO} for {@code a.club.SectionDTO}
 * and {@code a.badminton.SectionDTO}) are prefixed with either the configured prefix or the first package segment,
 * from right to left, which distinguishes the asserted type from all others of the clash
 * (e.g. {@code CLUB_SECTION_DTO} and {@code BADMINTON_SECTION_DTO}).
 */
public final class AssertJFactoryNameResolver {

    /**
     * @param name        the name of the factory without a prefix
     * @param packageName the package of the asserted type
     * @param prefix      the configured prefix, used in case of a clash
     */
    public record Factory(String name, String packageName, Optional<String> prefix) {
    }

    private AssertJFactoryNameResolver() {
    }

    /**
     * @param factories the factories of the generated class
     * @return the resolved names, in the order of the given factories
     * @throws IllegalStateException if a clash can not be resolved
     */
    public static List<String> resolve(List<Factory> factories) {
        var factoriesByName = factories.stream()
                .collect(Collectors.groupingBy(Factory::name));

        var names = new ArrayList<String>();
        for (var factory : factories) {
            var clashing = factoriesByName.get(factory.name());
            if (clashing.size() == 1) {
                names.add(factory.name());
            } else {
                var prefix = factory.prefix()
                        .or(() -> derivePrefix(factory, clashing))
                        .orElseThrow(() -> new IllegalStateException("Cannot resolve the clashing InstanceOfAssertFactory name "
                                + factory.name() + " for a type in package " + factory.packageName()
                                + ", configure a prefix via the factoryPrefixes parameter."));
                names.add(prefix.toUpperCase(Locale.ROOT) + "_" + factory.name());
            }
        }

        var duplicates = names.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
        if (!duplicates.isEmpty()) {
            throw new IllegalStateException("Clashing InstanceOfAssertFactory names " + duplicates
                    + ", configure distinct prefixes via the factoryPrefixes parameter.");
        }

        return names;
    }

    private static Optional<String> derivePrefix(Factory factory, List<Factory> clashing) {
        var segments = reversedSegments(factory.packageName());
        var others = clashing.stream()
                .filter(other -> other != factory)
                .map(other -> reversedSegments(other.packageName()))
                .toList();

        for (var depth = 0; depth < segments.size(); depth++) {
            var segment = segments.get(depth);
            var index = depth;
            if (others.stream().noneMatch(other -> index < other.size() && segment.equals(other.get(index)))) {
                return Optional.of(segment);
            }
        }

        return Optional.empty();
    }

    private static List<String> reversedSegments(String packageName) {
        if (packageName.isEmpty()) {
            return List.of();
        }

        var segments = new ArrayList<>(Arrays.asList(packageName.split("\\.")));
        Collections.reverse(segments);
        return segments;
    }
}
