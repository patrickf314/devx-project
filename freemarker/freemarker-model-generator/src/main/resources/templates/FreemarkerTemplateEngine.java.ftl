<#-- @ftlvariable name="packageName" type="java.lang.String" -->
package ${packageName};

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Generic FreeMarker template engine that processes a {@link FreemarkerTemplate} together with
 * the corresponding model instance.
 *
 * <p>Example setup:</p>
 * <pre>{@code
 * var engine = new FreemarkerTemplateEngine(MyApp.class.getClassLoader(), "templates");
 * try (var writer = ...) {
 *     engine.process(WelcomeEmailTemplate.TEMPLATE, model, writer);
 * }
 * }</pre>
 */
public class FreemarkerTemplateEngine {

    private final Configuration configuration;

    /**
     * @param classLoader       class loader used to locate templates on the classpath
     * @param templateDirectory classpath-relative directory that contains the {@code .ftl} files
     */
    public FreemarkerTemplateEngine(ClassLoader classLoader, String templateDirectory) {
        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(classLoader, templateDirectory);
    }

    /**
     * Processes {@code template} with {@code model} as the root data model and writes the result
     * to {@code writer}.
     *
     * @param template the template identifier returned by a generated {@code *Template} constant
     * @param model    the model instance whose properties are exposed to the template
     * @param writer   the target writer
     * @throws IOException if the template cannot be loaded or rendering fails
     */
    public <M> void process(FreemarkerTemplate<M> template, M model, Writer writer) throws IOException {
        try {
            configuration.getTemplate(template.getTemplatePath()).process(model, writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template: " + template.getTemplatePath(), e);
        }
    }
}
