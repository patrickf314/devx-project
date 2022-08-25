package de.badminton.neubiberg.service.test.api.dto;

import lombok.Data;

@Data
public class GenericDTO<T, S> {

    private T value;
    private S s;

}
