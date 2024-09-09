package de.devx.project.hamcrest.matcher.maven.dto.mapper;

import de.devx.project.commons.api.model.data.ApiDTOModel;
import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.api.model.type.ApiTypeType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldTypeModel.objectType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

class DTOHamcrestMatcherMapperTest {

    private static final DTOHamcrestMatcherMapper MAPPER = Mappers.getMapper(DTOHamcrestMatcherMapper.class);

    @Test
    void testMapFieldsWithGenericExtension() {
        var baseDto = new ApiDTOModel();
        baseDto.setClassName("de.devx.test.dto");
        baseDto.setName("BaseDTO");
        baseDto.setTypeArguments(List.of("T"));
        baseDto.setFields(Map.of(
                "field", new ApiTypeModel("collection", ApiTypeType.JAVA_TYPE, null, true, List.of(new ApiTypeModel("T", ApiTypeType.GENERIC_TYPE, true)))
        ));

        var testDto = new ApiDTOModel();
        testDto.setClassName("de.devx.test.dto");
        testDto.setName("TestDTO");
        testDto.setExtendedDTO(new ApiTypeModel("BaseDTO", ApiTypeType.DTO, "de.devx.test.dto", true, List.of(new ApiTypeModel("string", ApiTypeType.JAVA_TYPE, true))));

        var model = new ApiModel();
        model.addDTO(baseDto);

        var fields = MAPPER.mapFields(testDto, model);

        assertThat(fields, hasSize(1));
        assertThat(fields.get(0).getName(), is(equalTo("field")));
        assertThat(fields.get(0).getGetter(), is(equalTo("getField")));
        assertThat(fields.get(0).getType(), is(objectType("java.util", "Collection", List.of(objectType("java.lang", "String", Collections.emptyList())))));
    }

}