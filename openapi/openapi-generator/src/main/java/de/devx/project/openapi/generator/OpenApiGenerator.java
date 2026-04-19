package de.devx.project.openapi.generator;

import de.devx.project.commons.api.model.data.*;
import de.devx.project.commons.api.model.type.ApiMethodParameterType;
import de.devx.project.commons.api.model.type.ApiMethodResponseType;
import de.devx.project.commons.api.model.type.ApiTypeType;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenApiGenerator {

    private final String title;
    private final String description;
    private final String version;
    private final String serverUrl;
    private final String serverDescription;
    private final File outputFile;

    public OpenApiGenerator(String title, String description, String version,
                            String serverUrl, String serverDescription, File outputFile) {
        this.title = title;
        this.description = description;
        this.version = version;
        this.serverUrl = serverUrl;
        this.serverDescription = serverDescription;
        this.outputFile = outputFile;
    }

    public void generate(List<ApiModel> apiModels) throws IOException {
        var openAPI = buildOpenAPI(apiModels);
        writeSpec(openAPI);
    }

    private OpenAPI buildOpenAPI(List<ApiModel> apiModels) {
        var openAPI = new OpenAPI()
                .openapi("3.1.0")
                .info(buildInfo());

        if (serverUrl != null && !serverUrl.isBlank()) {
            openAPI.addServersItem(buildServer());
        }

        var paths = new Paths();
        var components = new Components();

        for (var apiModel : apiModels) {
            buildPaths(apiModel, paths);
            buildComponents(apiModel, components);
        }

        if (!paths.isEmpty()) {
            openAPI.paths(paths);
        }

        var schemas = components.getSchemas();
        if (schemas != null && !schemas.isEmpty()) {
            openAPI.components(components);
        }

        return openAPI;
    }

    private Info buildInfo() {
        var info = new Info()
                .title(title)
                .version(version);
        if (description != null && !description.isBlank()) {
            info.description(description);
        }
        return info;
    }

    private Server buildServer() {
        var server = new Server().url(serverUrl);
        if (serverDescription != null && !serverDescription.isBlank()) {
            server.description(serverDescription);
        }
        return server;
    }

    private void buildPaths(ApiModel apiModel, Paths paths) {
        for (var service : apiModel.getEndpoints().values()) {
            for (var methods : service.getMethods().values()) {
                for (var method : methods) {
                    var fullPaths = buildFullPaths(service.getBasePaths(), method.getPaths());
                    for (var path : fullPaths) {
                        var pathItem = paths.computeIfAbsent(path, k -> new PathItem());
                        for (var httpMethod : method.getHttpMethods()) {
                            pathItem.operation(
                                    PathItem.HttpMethod.valueOf(httpMethod.toUpperCase()),
                                    buildOperation(method, service.getName())
                            );
                        }
                    }
                }
            }
        }
    }

    private List<String> buildFullPaths(List<String> basePaths, List<String> methodPaths) {
        var bases = (basePaths == null || basePaths.isEmpty()) ? List.of("") : basePaths;
        var paths = (methodPaths == null || methodPaths.isEmpty()) ? List.of("") : methodPaths;

        var result = new ArrayList<String>();
        for (var base : bases) {
            for (var path : paths) {
                var combined = combinePaths(base, path);
                result.add(combined.isEmpty() ? "/" : combined);
            }
        }
        return result;
    }

    private String combinePaths(String base, String path) {
        if (base.endsWith("/") && path.startsWith("/")) {
            return base + path.substring(1);
        }
        if (!base.endsWith("/") && !path.isEmpty() && !path.startsWith("/")) {
            return base + "/" + path;
        }
        return base + path;
    }

    private Operation buildOperation(ApiMethodModel method, String tag) {
        var operation = new Operation().operationId(method.getName());

        if (tag != null) {
            operation.addTagsItem(tag);
        }

        var parameters = buildParameters(method);
        if (!parameters.isEmpty()) {
            operation.parameters(parameters);
        }

        var requestBody = buildRequestBody(method);
        if (requestBody != null) {
            operation.requestBody(requestBody);
        }

        operation.responses(buildResponses(method));
        return operation;
    }

    private List<Parameter> buildParameters(ApiMethodModel method) {
        var parameters = new ArrayList<Parameter>();
        if (method.getParameters() == null) {
            return parameters;
        }

        for (var param : method.getParameters()) {
            if (param.getIn() == ApiMethodParameterType.BODY) {
                continue;
            }

            var paramName = param.getParameterName() != null ? param.getParameterName() : param.getName();
            var p = new Parameter()
                    .name(paramName)
                    .in(param.getIn().name().toLowerCase())
                    .schema(typeToSchema(param.getType()));

            if (param.getIn() == ApiMethodParameterType.PATH
                    || (param.getType() != null && param.getType().isRequired())) {
                p.required(true);
            }

            parameters.add(p);
        }
        return parameters;
    }

    private RequestBody buildRequestBody(ApiMethodModel method) {
        if (method.getParameters() == null) {
            return null;
        }

        var bodyParam = method.getParameters().stream()
                .filter(p -> p.getIn() == ApiMethodParameterType.BODY)
                .findFirst()
                .orElse(null);

        if (bodyParam == null) {
            return null;
        }

        var schema = typeToSchema(bodyParam.getType());
        if (schema == null) {
            return null;
        }

        var contentType = isMultipartType(bodyParam.getType()) ? "multipart/form-data" : "application/json";

        return new RequestBody()
                .required(true)
                .content(new Content().addMediaType(contentType, new MediaType().schema(schema)));
    }

    private boolean isMultipartType(ApiTypeModel type) {
        if (type == null) {
            return false;
        }
        return "org.springframework.web.multipart.MultipartFile".equals(type.getClassName())
                || "MultipartFile".equals(type.getName());
    }

    private ApiResponses buildResponses(ApiMethodModel method) {
        var responses = new ApiResponses();

        if (method.getResponseType() == ApiMethodResponseType.STREAM) {
            responses.addApiResponse("200", new ApiResponse()
                    .description("Binary stream")
                    .content(new Content().addMediaType("application/octet-stream",
                            new MediaType().schema(new BinarySchema()))));
            return responses;
        }

        if (method.getResponseType() == ApiMethodResponseType.SERVER_SEND_EVENT) {
            responses.addApiResponse("200", new ApiResponse()
                    .description("Server-sent events")
                    .content(new Content().addMediaType("text/event-stream",
                            new MediaType().schema(new StringSchema()))));
            return responses;
        }

        var returnType = method.getReturnType();
        if (returnType == null || isVoidType(returnType)) {
            responses.addApiResponse("204", new ApiResponse().description("No content"));
        } else {
            var schema = typeToSchema(returnType);
            var response = new ApiResponse().description("Success");
            if (schema != null) {
                response.content(new Content().addMediaType("application/json", new MediaType().schema(schema)));
            }
            responses.addApiResponse("200", response);
        }

        return responses;
    }

    private boolean isVoidType(ApiTypeModel type) {
        if (type.getType() != ApiTypeType.JAVA_TYPE) {
            return false;
        }
        return "void".equals(type.getName()) || "java.lang.Void".equals(type.getClassName());
    }

    private void buildComponents(ApiModel apiModel, Components components) {
        var dtoStream = apiModel.getDtos()
                .values()
                .stream()
                .map(model -> Map.entry(model.getName(), buildDtoSchema(model)));
        var enumStream = apiModel.getEnums()
                .values()
                .stream()
                .map(model -> Map.entry(model.getName(), buildEnumSchema(model)));
        var brandedTypeStream = apiModel.getBrandedTypes()
                .values()
                .stream()
                .map(model -> Map.entry(model.getName(), typeToSchema(model.getUnderlyingType())));

        components.setSchemas(
                Stream.concat(dtoStream, Stream.concat(enumStream, brandedTypeStream))
                        .sorted(Comparator.comparing(Map.Entry::getKey))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (_, y) -> y, LinkedHashMap::new))
        );
    }

    private Schema<?> buildDtoSchema(ApiDTOModel dto) {
        if (dto.getExtendedDTO() != null) {
            return new Schema<>()
                    .addAllOfItem(new Schema<>().$ref("#/components/schemas/" + dto.getExtendedDTO().getName()))
                    .addAllOfItem(buildObjectSchema(dto));
        }
        return buildObjectSchema(dto);
    }

    private ObjectSchema buildObjectSchema(ApiDTOModel dto) {
        var schema = new ObjectSchema();
        if (dto.getFields() == null || dto.getFields().isEmpty()) {
            return schema;
        }

        for (var entry : dto.getFields().entrySet()) {
            var fieldSchema = typeToSchema(entry.getValue());
            if (fieldSchema != null) {
                schema.addProperty(entry.getKey(), fieldSchema);
            }
            if (entry.getValue().isRequired()) {
                schema.addRequiredItem(entry.getKey());
            }
        }
        return schema;
    }

    private Schema<?> buildEnumSchema(ApiEnumModel enumModel) {
        var schema = new StringSchema();
        enumModel.getValues().forEach(schema::addEnumItem);
        return schema;
    }

    private Schema<?> typeToSchema(ApiTypeModel type) {
        if (type == null) {
            return null;
        }
        return switch (type.getType()) {
            case JAVA_TYPE -> javaTypeToSchema(type);
            case DTO, ENUM, BRANDED_TYPE -> new Schema<>().$ref("#/components/schemas/" + type.getName());
            case GENERIC_TYPE -> genericTypeToSchema(type);
            case UNKNOWN -> null;
        };
    }

    private Schema<?> javaTypeToSchema(ApiTypeModel type) {
        var className = type.getClassName() != null ? type.getClassName() : type.getName();
        var schema = schemaForClassName(className);
        if (schema != null) {
            return schema;
        }
        schema = schemaForClassName(type.getName());
        return schema != null ? schema : new StringSchema();
    }

    private Schema<?> schemaForClassName(String name) {
        if (name == null) {
            return null;
        }
        return switch (name) {
            case "void", "java.lang.Void" -> null;
            case "java.lang.String", "String", "char", "java.lang.Character" -> new StringSchema();
            case "int", "java.lang.Integer", "Integer", "short", "java.lang.Short" ->
                    new IntegerSchema().format("int32");
            case "long", "java.lang.Long", "Long" ->
                    new IntegerSchema().format("int64");
            case "float", "java.lang.Float" ->
                    new NumberSchema().format("float");
            case "double", "java.lang.Double" ->
                    new NumberSchema().format("double");
            case "boolean", "java.lang.Boolean", "Boolean" -> new BooleanSchema();
            case "java.math.BigDecimal", "BigDecimal" -> new NumberSchema();
            case "java.math.BigInteger", "BigInteger" -> new IntegerSchema();
            case "java.util.UUID", "UUID" -> new UUIDSchema();
            case "java.time.LocalDate", "LocalDate" -> new DateSchema();
            case "java.time.LocalDateTime", "LocalDateTime",
                 "java.time.OffsetDateTime", "OffsetDateTime",
                 "java.time.ZonedDateTime", "ZonedDateTime",
                 "java.time.Instant", "Instant" -> new DateTimeSchema();
            case "byte[]", "org.springframework.web.multipart.MultipartFile", "MultipartFile" -> new BinarySchema();
            case "java.lang.Object", "Object" -> new ObjectSchema();
            default -> null;
        };
    }

    private Schema<?> genericTypeToSchema(ApiTypeModel type) {
        var name = type.getName();
        if (name == null) {
            return null;
        }
        var args = type.getTypeArguments();

        if (name.contains("Optional") || name.contains("ResponseEntity")) {
            return (args != null && !args.isEmpty()) ? typeToSchema(args.get(0)) : null;
        }

        if (name.contains("List") || name.contains("Collection")
                || name.contains("Set") || name.contains("Queue") || name.contains("Deque")) {
            var arraySchema = new ArraySchema();
            if (args != null && !args.isEmpty()) {
                var items = typeToSchema(args.get(0));
                if (items != null) {
                    arraySchema.items(items);
                }
            }
            return arraySchema;
        }

        if (name.contains("Map")) {
            var mapSchema = new MapSchema();
            if (args != null && args.size() >= 2) {
                var valueSchema = typeToSchema(args.get(1));
                if (valueSchema != null) {
                    mapSchema.additionalProperties(valueSchema);
                }
            }
            return mapSchema;
        }

        return null;
    }

    private void writeSpec(OpenAPI openAPI) throws IOException {
        var parentDir = outputFile.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }

        Files.writeString(outputFile.toPath(), Yaml.pretty().writeValueAsString(openAPI), StandardCharsets.UTF_8);
    }
}
