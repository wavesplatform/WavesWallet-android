package util;

import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Scanner;

public class SerializeUtils {

    public static <T> T serializeJsonFromAssets(String path, Type type) {
        String json = getStringAsset(path);
        return new GsonBuilder().create().fromJson(json, type);
    }

    public static String getStringAsset(String path) {
        InputStream stream = SerializeUtils.class.getResourceAsStream(path);
        return new Scanner(stream,"UTF-8").useDelimiter("\\A").next();
    }
}
