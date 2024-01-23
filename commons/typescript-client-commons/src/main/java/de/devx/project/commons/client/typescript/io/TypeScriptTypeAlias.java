package de.devx.project.commons.client.typescript.io;

import lombok.Data;

@Data
public class TypeScriptTypeAlias {

    private String className;
    private String tsFile;
    private String tsType;
    private String tsPath;
    private String annotation;

}
