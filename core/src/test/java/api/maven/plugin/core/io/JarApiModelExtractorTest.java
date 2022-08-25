package api.maven.plugin.core.io;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class JarApiModelExtractorTest {

    @Test
    void testTestModuleExtraction() {
        var extractor = new JarApiModelExtractor();

        var model = extractor.extract(new File("../test-module/target/test-module-1.0.jar"));
        assertThat(model.isPresent(), is(true));
    }

}