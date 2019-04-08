package de.twoyang.telegram.bot.tb.helper.config;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Config {
    public static String getConfig(String key) throws IOException {
        return getConfig(key, System::getenv, System::getenv);
    }

    public static String getConfig(String key, Function<String, String> getenv, Supplier<Map<String, String>> getenvMap) throws IOException {
        if (getenvMap.get().containsKey(key))
            return getenv.apply(key);
        else if (getenvMap.get().containsKey(key + "_FILE"))
            return Files.readAllLines(new File(getenv.apply(key + "_FILE")).toPath()).get(0).trim();
        return null;
    }
}