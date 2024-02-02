package de.devx.project.commons.client.typescript.properties;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TypeScriptClientGeneratorProperties {

    private final List<TypeScriptTypeAlias> typeAliases;
    private final List<TypeScriptPackageAlias> packageAliases;
    private final String defaultPackageAlias;

    public String getPackageNameForClass(String className) {
        var i = className.lastIndexOf('.');
        if (i == -1) {
            throw new IllegalArgumentException("Invalid class name " + className);
        }

        var packageName = className.substring(0, i);
        for (var packageAlias : packageAliases) {
            if (packageName.startsWith(packageAlias.getPrefix() + ".")) {
                return (packageAlias.getAlias() == null ? "" : packageAlias.getAlias() + ".") + packageName.substring(packageAlias.getPrefix().length() + 1);
            }
        }

        return defaultPackageAlias == null ? packageName : defaultPackageAlias;
    }
}

