# React Client Api Maven Plugin

A maven plugin used to generate react (using redux) client in typescript based on an API model definition.

## Configuration

```xml

<build>
    <plugins>
        <!-- generate sources -->
        <plugin>
            <groupId>de.devx.project</groupId>
            <artifactId>react-client-api-maven-plugin</artifactId>
            <version>1.0</version>
            <executions>
                <execution>
                    <id>generate-react-client</id>
                    <goals>
                        <goal>generate-react-client</goal>
                    </goals>
                    <!-- This is the default phase in which the goal generate-react-client is executed -->
                    <phase>generate-sources</phase>

                    <configuration>
                        <outputDirectory>${project.basedir}/gen/service</outputDirectory>
                        <apiModelJson>api-model.json</apiModelJson>

                        <typeAliases>
                            <typeAlias>
                                <className>org.springframework.web.multipart.MultipartFile</className>
                                <type>File</type>
                            </typeAlias>
                            <typeScriptTypeAlias>
                                <className>java.time.LocalDate</className>
                                <type>string</type>
                            </typeScriptTypeAlias>
                            <typeScriptTypeAlias>
                                <className>de.devx.project.example.common.CustomData</className>
                                <type>CustomData</type>
                                <path>commons/custom-data</path>
                            </typeScriptTypeAlias>
                        </typeAliases>

                        <packageAliases>
                            <packageAlias>
                                <prefix>de.devx.project.example.api</prefix>
                            </packageAlias>
                            <packageAlias>
                                <prefix>de.devx.project.example.commons</prefix>
                                <alias>commons</alias>
                            </packageAlias>
                        </packageAliases>
                        <defaultPackageAlias>commons</defaultPackageAlias>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

| Property              | Required | Default value                | Description                                                                                                                                                                   |
|-----------------------|----------|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `outputDirectory`     | `false`  | `${project.build.directory}` | A path to a directory, in which the react client sources should be generated                                                                                                  |
| `apiModelJson`        | `false`  | -                            | The path to the `api-model.json` file. If not specified, then the plugin will search for the file in the dependencies. If none is found, then the plugin execution will fail. |
| `typeAliases`         | `false`  | -                            | A list of type aliases (see [Type Aliases](#type-aliases)).                                                                                                                   |
| `packageAliases`      | `false`  | -                            | A list of package aliases (see [Package Aliases](#package-aliases)).                                                                                                          |
| `defaultPackageAlias` | `false`  | -                            | A default package alias, which is applied to all packages except to ones with a specified package alias.                                                                      |

### Type Aliases

A type alias is used to specify an alternative type for a specific java class.
This is usefully, if one wants to change to typescript type for certain java classes.

```xml

<typeScriptTypeAlias>
    <className>de.devx.project.example.common.CustomData</className>
    <type>CustomData</type>
    <path>commons/custom-data</path>
</typeScriptTypeAlias>
```

| Property    | Required | Default value | Description                                                                                                                              |
|-------------|----------|---------------|------------------------------------------------------------------------------------------------------------------------------------------|
| `className` | `true`   | -             | The full java class name.                                                                                                                |
| `type`      | `true`   | -             | The typescript type. This value will be used as type in the generated typescript files                                                   |
| `path`      | `false`  | -             | A path to a typescript file, from which the type is imported. If not specified, then no import will be generated for the typescript type |

### Package Aliases

Package aliases are used to replace the long java package names to shorter typescript paths.
For example, without package alias DTOs in the java package `de.devx.project.example.api.test.dto` will be generated in
the output directory under
`de/devx/project/example/api/test/dto`.
This works, but is not common practice in typescript projects.

Thus, one can specify a package alias for all package starting with `de.devx.project.example.api` (prefix).
The prefix is then replaces by the specified alias or removed if not alias is given. For example, if we use

```xml

<packageAlias>
    <prefix>de.devx.project.example.api</prefix>
</packageAlias>
```

then the DTOs in the java package `de.devx.project.example.api.test.dto` will be generated in `test/dto`.
If we add the alias `alias.example`, then the DTOs will be generated in `alias/example/test.de`.

```xml

<packageAlias>
    <prefix>de.devx.project.example.api.test.dto</prefix>
    <alias>alias.example</alias>
</packageAlias>
```

| Property | Required | Default value | Description                     |
|----------|----------|---------------|---------------------------------|
| `prefix` | `true`   | -             | The package prefix.             |
| `alias`  | `false`  | -             | An alias for the package prefix |
