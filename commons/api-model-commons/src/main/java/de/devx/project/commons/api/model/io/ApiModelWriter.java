package de.devx.project.commons.api.model.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.devx.project.commons.api.model.data.ApiModel;

import java.io.IOException;
import java.io.Writer;

public class ApiModelWriter implements AutoCloseable {

    private final ObjectMapper mapper;
    private final Writer delegate;

    public ApiModelWriter(Writer delegate)  {
        this.mapper = new ObjectMapper();
        this.delegate = delegate;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public void write(ApiModel model) throws IOException {
        mapper.writeValue(delegate, model);
    }
}
