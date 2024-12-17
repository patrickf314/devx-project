package de.devx.project.spring.webmvc.test.generator;

import de.devx.project.commons.test.io.TestSourceFileGenerator;
import de.devx.project.spring.webmvc.test.generator.data.SpringWebMvcMethodModel;
import de.devx.project.spring.webmvc.test.generator.data.SpringWebMvcParameterModel;
import de.devx.project.spring.webmvc.test.generator.data.SpringWebMvcPathModel;
import de.devx.project.spring.webmvc.test.generator.data.SpringWebMvcTestModel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static de.devx.project.spring.webmvc.test.generator.data.SpringWebMvcTypeModel.fromClass;
import static de.devx.project.spring.webmvc.test.generator.data.SpringWebMvcTypeModel.primitive;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class SpringWebMvcTestGeneratorTest {

    private final TestSourceFileGenerator sourceFileGenerator = new TestSourceFileGenerator();
    private final SpringWebMvcTestGenerator generator = new SpringWebMvcTestGenerator(sourceFileGenerator);

    @Test
    void test() throws IOException {
        var method = new SpringWebMvcMethodModel();
        method.setName("getTestIds");
        method.setPath(new SpringWebMvcPathModel("/test-ids", Collections.emptyList()));
        method.setReturnType(fromClass("java.utils.List", List.of(fromClass(Integer.class))));
        method.setHttpMethod("GET");
        method.setParameters(List.of(new SpringWebMvcParameterModel("id", "idHttp", SpringWebMvcParameterModel.Type.QUERY, primitive("int"))));

        var test = new SpringWebMvcTestModel();
        test.setName("TestControllerWebMvcTestBase");
        test.setPackageName("de.test.spring.webmvc");
        test.setController(fromClass("de.test.spring.webmvc.rest.TestController"));
        test.setService(fromClass("de.test.spring.webmvc.service.TestService"));
        test.setActiveProfile(List.of("test"));
        test.setMethods(List.of(method));

        generator.generate(test);
        var generatedTestBase = sourceFileGenerator.getFileContent(test.getPackageName(), "TestControllerWebMvcTestBase");
        assertThat(generatedTestBase.isPresent(), is(true));
        assertThat(generatedTestBase.get(), is("""
                package de.test.spring.webmvc;
                
                import com.fasterxml.jackson.databind.ObjectMapper;
                import org.junit.jupiter.api.BeforeEach;
                import org.jeasy.random.EasyRandom;
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
                import org.springframework.test.context.bean.override.mockito.MockitoBean;
                import org.springframework.http.HttpMethod;
                import org.springframework.test.context.ActiveProfiles;
                import org.springframework.test.context.ContextConfiguration;
                import org.springframework.test.web.servlet.MockMvc;
                import org.springframework.test.web.servlet.setup.MockMvcBuilders;
                import org.springframework.test.web.servlet.ResultActions;
                import org.springframework.web.context.WebApplicationContext;
                import de.test.spring.webmvc.service.TestService;
                import de.test.spring.webmvc.rest.TestController;
                import java.utils.List;
                
                import static org.mockito.Mockito.when;
                import static org.mockito.Mockito.verify;
                import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
                import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
                import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
                
                @WebMvcTest
                @ActiveProfiles({"test"})
                @ContextConfiguration(classes = {
                    TestController.class
                })
                public class TestControllerWebMvcTestBase {
                
                    protected final EasyRandom random;
                
                    @Autowired
                    protected ObjectMapper objectMapper;
                
                    @Autowired
                    protected WebApplicationContext webApplicationContext;
                
                    @MockitoBean
                    protected TestService service;
                
                    protected MockMvc mvc;
                
                    protected TestControllerWebMvcTestBase(EasyRandom random) {
                        this.random = random;
                    }
                
                    @BeforeEach
                    void init() {
                        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
                    }
                
                    /**
                     * Autogenerated test layout method for {@link TestController#getTestIds}
                     *
                     * @return the result of the {@link MockMvc} request
                     * @throws Exception if an exceptions occurs
                     */
                    protected ResultActions getTestIds() throws Exception {
                        var getTestIdsResult = nextList();
                        var id = random.nextInt();
                
                        return getTestIds(getTestIdsResult, id);
                    }
                
                    /**
                     * Autogenerated test layout method for {@link TestController#getTestIds}
                     *
                     * @param getTestIdsResult an object which should be returned by the service mock
                     * @param id an object which should be passed to the controller by the request
                     * @return the result of the {@link MockMvc} request
                     * @throws Exception if an exceptions occurs
                     */
                    protected ResultActions getTestIds(List<Integer> getTestIdsResult, int id) throws Exception {
                        mockGetTestIdsServiceCalls(getTestIdsResult, id);
                
                        var result = performGetTestIdsRequest(id)
                                .andExpect(status().is(200))
                                .andExpect(content().json(objectMapper.writeValueAsString(getTestIdsResult)));
                
                        verifyGetTestIdsServiceCalls(id);
                        return result;
                    }
                
                    protected ResultActions performGetTestIdsRequest(int id) throws Exception {
                        return mvc.perform(request(HttpMethod.GET, "/test-ids")
                                .param("idHttp", String.valueOf(id)));
                    }
                
                    protected void mockGetTestIdsServiceCalls(List<Integer> getTestIdsResult, int id) throws Exception {
                        throw new UnsupportedOperationException("Implement mockGetTestIdsServiceCall");
                    }
                
                    protected void verifyGetTestIdsServiceCalls(int id) throws Exception {
                        throw new UnsupportedOperationException("Implement verifyGetTestIdsServiceCall");
                    }
                
                    protected List<Integer> nextList() {
                        throw new UnsupportedOperationException("Implement nextList");
                    }
                }
                """
        ));
    }
}