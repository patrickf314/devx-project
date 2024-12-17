package de.devx.project.assertj.assertion.gennerator.mapper;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.commons.generator.model.JavaClassModel;
import org.mapstruct.Mapper;

@Mapper
public interface AssertJAssertMapper {

    AssertJAssertModel mapToAssert(JavaClassModel model);

}
