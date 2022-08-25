package de.badminton.neubiberg.service.test.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestDTO extends BriefTestDTO {

    private Map<String, TestRecordDTO> map;
    private List<String> list;
    @NotNull
    private String str;
    @NotNull
    private TestDTO child;

}
