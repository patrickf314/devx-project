package api.maven.plugin.angular.client.mapper;

import api.maven.plugin.angular.client.data.TypeScriptPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public final class TypeScriptPathMapper {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([a-zA-Z]+)}");


    private TypeScriptPathMapper() {
        // No instances
    }

    public static TypeScriptPath mapPath(String pathStr) {
        if("".equals(pathStr)) {
            var tsPath = new TypeScriptPath();
            tsPath.setParams(Collections.emptyList());
            return tsPath;
        }

        if(!pathStr.startsWith("/")) {
            pathStr = "/" + pathStr;
        }

        var pathMatcher = PATH_VARIABLE_PATTERN.matcher(pathStr);
        var params = new ArrayList<String>();

        while(pathMatcher.find()) {
            params.add(pathMatcher.group(1));
        }

        var path = "'" + pathMatcher.replaceAll("' + $1 + '");
        if(path.endsWith(" + '")){
            path = path.substring(0, path.length() - 4);
        }else{
            path += "'";
        }

        if(path.equals("''")) {
            path = "";
        }

        var tsPath = new TypeScriptPath();
        tsPath.setPath(path);
        tsPath.setParams(params);
        return tsPath;
    }
}
