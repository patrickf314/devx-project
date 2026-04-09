package de.devx.project.test;

import de.devx.project.annotations.BrandedType;

@BrandedType(type = String.class)
public class CorrelationId {

    private String prefix;
    private String suffix;
}
