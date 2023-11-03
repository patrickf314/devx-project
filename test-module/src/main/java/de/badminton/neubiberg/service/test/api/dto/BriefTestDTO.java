package de.badminton.neubiberg.service.test.api.dto;

import api.maven.plugin.annotations.Readonly;
import de.badminton.neubiberg.service.test.api.type.TestType;
import lombok.Data;

@Data
public class BriefTestDTO {

    @Readonly
    private int id;
    private TestType type;

    private NestedDTO nestedDTO;

    public int getId() {
        return id;
    }

    @Data
    public static class NestedDTO {

        private DoubleNestedDTO nestedDTO;

        @Data
        public static class DoubleNestedDTO {
            private int id;
        }

    }
}
