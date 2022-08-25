package api.maven.plugin.angular.client.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TypeScriptType {

    public static final TypeScriptType DOWNLOAD_INFO = new TypeScriptType("DownloadInfo");
    public static final TypeScriptType SERVER_SEND_EVENT = new TypeScriptType("DownloadInfo");

    private String name;
    private boolean optional = false;

    private TypeScriptType(String name) {
        this.name = name;
    }

}
