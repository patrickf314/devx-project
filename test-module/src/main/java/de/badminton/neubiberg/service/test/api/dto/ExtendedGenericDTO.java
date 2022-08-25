package de.badminton.neubiberg.service.test.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedGenericDTO<T> extends GenericDTO<T, List<String>> {

    private int id;

}
