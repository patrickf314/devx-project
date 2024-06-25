# DevX Project

The idea of this project is to provide annotation processor, maven plugins, etc.
which improve the development workflow in projects.
Initially, this project was developed in the context of a Spring Boot backend
combined with an angular frontend.
Later, corresponding modules for React were added.
If wanted, the project might be extended to other frameworks.

At the moment, there are two main features:
- The generation of frontend clients (in typescript) based on the java classes of the backend.
- The generation of test utilities to simplify testing of a project.

## API Model

A corresponding frontend client for a backend is an essential requirement for having a
interaction between backend and frontend. 
Usually, one only want to modify the backend API and then automatically generate
the client for a given framework in the frontend.
The common practice for that is using the OpenAPI specification for documenting the backend
API and then use one of the many available tool to create a frontend client.
In the context of a java backend and a typescript frontend, the generated client is restricted
to the information available in the OpenAPI documentation.
However, since typescript and java offer a rich type system, one might want to use an extended documentation.
Thus, the project provides a richer API model which for example supports generics in contrast to OpenAPI.

### API Model Generation

In the context of a spring boot API, there is the spring-web-annotation-processor, which processes the spring web annotations
of the RestAPI and generates a `api-model.json` in the classpath.
By adding the API module as a dependency and a client generator (e.g. react-client-api-maven-plugin) to the client, then
the client for the API is generated during a `mvn install`.

## Test Utils

As test utils, there are certain annotation processors for creating assertion matchers for different testing frameworks
such as Hamcrest or AssertJ.
In addition, there are maven plugins for creating matchers for DTOs using the API model.

## Ideas

I wouldn't the current state of the project as product ready. 
It works for my use-cases and is adjusted to my need.
However, I am willing to develop this project towards more general usages.
Thus, if you have a different use-case or just want to have some information about what is already supported, feel free
to ask.
In addition, the test coverage and code documentation is lacking.
This has to be improved, feel free to contribute.

One feature I have in mind is to close the gab to OpenAPI as this is still a standard of API documentation.
The idea would be to have for example a maven-plugin which can generate a JSON according to OpenAPI specification
based on the `api-model.json`.
This should be easily achievable by a corresponding mapper.

Other missing components are demo modules to show how the different modules are used.

## Limitations

The main idea is to improve the development experience.
If want wants an accurate documentation of a Sping API one needs to start the spring context in order to get only
active endpoints, authentication, etc...
However, starting the spring context is compared to an annotation processor a time-consuming task.
The aim is to require a minimal time from changing the API to using the frontend client.
Usually, the IDE supports annotation processors in their default build process.
Thus, the generation of the `api-model.json` does not require a maven build.

