package de.devx.project.spring.webmvc.test.generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Data
@AllArgsConstructor
public class SpringWebMvcParameterModel {

    private String name;
    private Type in;
    private SpringWebMvcTypeModel type;

    public String getStringConversion() {
        if (Objects.equals(type, SpringWebMvcTypeModel.fromClass(String.class))) {
            return name;
        }

        return "String.valueOf(" + name + ")";
    }

    public boolean isMultipartFile() {
        return type.isMultipartFile();
    }

    public enum Type {
        PATH,
        QUERY,
        HEADER,
        BODY
    }
}
