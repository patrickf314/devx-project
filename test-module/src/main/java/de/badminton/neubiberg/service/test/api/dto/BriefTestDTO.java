package de.badminton.neubiberg.service.test.api.dto;

import de.badminton.neubiberg.service.test.api.type.TestType;
import lombok.Data;

@Data
public class BriefTestDTO {

    private int id;
    private TestType type;

}
