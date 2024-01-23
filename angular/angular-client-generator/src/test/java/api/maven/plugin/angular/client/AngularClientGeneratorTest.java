package api.maven.plugin.angular.client;

import api.maven.plugin.angular.client.data.TypeScriptTypeAlias;
import de.devx.project.commons.api.model.io.ApiModelReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

class AngularClientGeneratorTest {

    @Test
    void testTestModuleClientGeneration() throws IOException {
        var alias0 = new TypeScriptTypeAlias();
        alias0.setClassName("org.springframework.web.multipart.MultipartFile");
        alias0.setTsType("File");

        var alias1 = new TypeScriptTypeAlias();
        alias1.setClassName("de.badminton.neubiberg.service.test.api.dto.ScoreDTO");
        alias1.setTsType("Score");
        alias1.setTsFile("commons/game/score");

        var file = new File("../test-module/target/classes/api-model.json");
        try (var reader = new ApiModelReader(file)) {
            var model = reader.read();
            new AngularClientGenerator("target/out", List.of(alias0, alias1)).generate(model);
        }
    }

}