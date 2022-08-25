package api.maven.plugin.angular.client.utils;

public class TypeScriptServiceUtils {



    public static String getServiceByClassName(String className) {
        var i = className.indexOf(".service.");
        if(i == -1) {
            return "commons";
        }

        i += 9;
        var j = className.indexOf('.', i);
        if(j == -1) {
            return "commons";
        }

        return className.substring(i, j);
    }

}
