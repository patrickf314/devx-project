package api.maven.plugin.core.io;

import api.maven.plugin.core.model.ApiModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class ApiModelReader implements AutoCloseable {

    private final ObjectMapper mapper;
    private final InputStream stream;

    public ApiModelReader(File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    public ApiModelReader(InputStream stream)  {
        this.mapper = new ObjectMapper();
        this.stream = stream;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    public ApiModel read() throws IOException {
        return mapper.readValue(stream, ApiModel.class);
    }
}
