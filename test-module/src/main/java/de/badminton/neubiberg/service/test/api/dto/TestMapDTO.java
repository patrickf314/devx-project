package de.badminton.neubiberg.service.test.api.dto;

import java.util.AbstractMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO Document this class
 */
public class TestMapDTO<T extends BriefTestDTO> extends AbstractMap<Integer, T> {

    private final Set<T> dtos;

    public TestMapDTO(Set<T> dtos) {
        this.dtos = dtos;
    }

    @Override
    public Set<Entry<Integer, T>> entrySet() {
        return dtos.stream()
                .map(dto -> new SimpleEntry<>(dto.getId(), dto))
                .collect(Collectors.toSet());
    }
}
