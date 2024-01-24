package de.devx.project.client.react.properties;

import de.devx.project.commons.client.typescript.properties.TypeScriptClientGeneratorProperties;
import de.devx.project.commons.client.typescript.properties.TypeScriptDependency;
import de.devx.project.commons.client.typescript.properties.TypeScriptPackageAlias;
import de.devx.project.commons.client.typescript.properties.TypeScriptTypeAlias;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ReactClientGeneratorProperties extends TypeScriptClientGeneratorProperties {

    private final TypeScriptDependency errorMapper;
    private final TypeScriptDependency errorSerializer;
    private final TypeScriptDependency reduxThunkConfig;
    private final TypeScriptDependency httpHeaderCustomizer;
    private final TypeScriptDependency backendUrlGetter;
    private final String backendUrl;

    public ReactClientGeneratorProperties(List<TypeScriptPackageAlias> packageAliases, String defaultPackageAlias, TypeScriptDependency errorMapper, TypeScriptDependency errorSerializer, TypeScriptDependency reduxThunkConfig, TypeScriptDependency httpHeaderCustomizer, TypeScriptDependency backendUrlGetter, String backendUrl) {
        super(packageAliases, defaultPackageAlias);

        this.errorMapper = errorMapper;
        this.errorSerializer = errorSerializer;
        this.reduxThunkConfig = reduxThunkConfig;
        this.httpHeaderCustomizer = httpHeaderCustomizer;
        this.backendUrlGetter = backendUrlGetter;
        this.backendUrl = backendUrl;
    }
}
