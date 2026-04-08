# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build all modules (skip tests)
mvn install -DskipTests

# Run all tests
mvn verify

# Run tests for a specific module
mvn verify -pl commons/api-model-commons

# Build a single module and its dependencies
mvn install -pl react/react-client-generator -am -DskipTests
```

CI uses Java 25 (Temurin). Match this locally.

## Architecture Overview

This is a **multi-module Maven project** (~27 modules) that generates TypeScript API clients and Java test utilities from Spring Web annotations at compile time.

### Data Flow

1. **Annotation Processor** (`spring-web/spring-web-annotation-processor`) scans Spring `@RestController`/`@RequestMapping` annotations at compile time and emits `api-model.json` — the intermediate representation of the API.
2. **Maven Plugins** (Mojos in `angular/`, `react/`, `typescript/`, `playwright/`) read `api-model.json` and invoke **Generators** to emit TypeScript client code using FreeMarker templates.
3. **Test Generation Plugins** (`hamcrest/`, `assertj/`, `spring-webmvc-test/`) read DTOs and emit Java matcher/assertion code.

### Module Groups

| Directory | Purpose |
|-----------|---------|
| `commons/api-model-commons` | Core data model: `ApiModel`, `ApiEndpointData`, `ApiDTOData`, `ApiEnumData` |
| `commons/processor-commons` | Annotation processor utilities (context, logging, reflection helpers) |
| `commons/generator-commons` | Base classes for file generation (`JavaClassModel`, `JavaFileGenerator`) |
| `commons/maven-plugin-commons` | Maven Mojo base classes, classpath scanning, `api-model.json` parsing |
| `commons/typescript-client-commons` | Shared TypeScript templates (FreeMarker) and mappers (MapStruct) |
| `commons/spring-web-processor-commons` | Spring annotation utilities shared by the processor and test generators |
| `spring-web/spring-web-annotation-processor` | `SpringAnnotationProcessor` — entry point for API extraction |
| `angular/`, `react/`, `typescript/`, `playwright/` | Client generator + Maven plugin pairs per framework |
| `hamcrest/`, `assertj/`, `spring-webmvc-test/` | Test utility annotation processor + generator + Maven plugin per framework |
| `bom/` | Bill of Materials for version alignment |

### Key Patterns

- **Annotation Processor → JSON → Maven Plugin** pipeline: processors and plugins are intentionally decoupled via `api-model.json`.
- **Abstract Mojo base classes** (e.g., `AbstractAssertJAssertionsMojo`) define the lifecycle; subclasses provide model-specific logic.
- **MapStruct mappers** transform `ApiModel` data into framework-specific models before FreeMarker renders them.
- **FreeMarker templates** live in `src/main/resources/templates/` of each generator module.
- Lombok is used for all POJOs; MapStruct and Lombok annotation processors are configured in every module's `pom.xml`.

### Key Versions (root pom.xml properties)

- Java 25, Maven 3.9.x
- Spring Framework 7.x, Jakarta EE 3.x
- MapStruct 1.6.x, Lombok 1.18.x, FreeMarker 2.3.x
- JUnit 5, AssertJ 3.x, Hamcrest 2.x, Mockito 5.x