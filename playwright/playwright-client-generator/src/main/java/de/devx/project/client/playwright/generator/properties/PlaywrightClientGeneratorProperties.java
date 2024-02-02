package de.devx.project.client.playwright.generator.properties;

import de.devx.project.commons.client.typescript.properties.TypeScriptClientGeneratorProperties;
import de.devx.project.commons.client.typescript.properties.TypeScriptDependency;
import de.devx.project.commons.client.typescript.properties.TypeScriptPackageAlias;
import de.devx.project.commons.client.typescript.properties.TypeScriptTypeAlias;
import lombok.Getter;

import java.util.List;

@Getter
public class PlaywrightClientGeneratorProperties extends TypeScriptClientGeneratorProperties {

    private final TypeScriptDependency httpHeaderCustomizer;
    private final TypeScriptDependency testContext;

    public PlaywrightClientGeneratorProperties(List<TypeScriptTypeAlias> typeAliases, List<TypeScriptPackageAlias> packageAliases, String defaultPackageAlias, TypeScriptDependency httpHeaderCustomizer, TypeScriptDependency testContext) {
        super(typeAliases, packageAliases, defaultPackageAlias);

        this.httpHeaderCustomizer = httpHeaderCustomizer;
        this.testContext = testContext;
    }
}
