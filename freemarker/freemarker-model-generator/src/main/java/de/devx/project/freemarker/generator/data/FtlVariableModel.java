package de.devx.project.freemarker.generator.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class FtlVariableModel {

    private final String name;

    /**
     * The plain java type, e.g. List<String>
     */
    private final String simpleType;

    private final List<String> imports;
}
