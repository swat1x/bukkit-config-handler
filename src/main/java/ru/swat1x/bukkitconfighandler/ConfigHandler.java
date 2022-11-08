package ru.swat1x.bukkitconfighandler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.swat1x.bukkitconfighandler.annotation.ConfigPath;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j(topic = "swat1x | BukkitConfigHandler")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigHandler {

    private static final ConfigHandler handler = new ConfigHandler();

    public static ConfigHandler get() {
        return handler;
    }

    public CreationResult reloadConfig(Config config, File file) {
        config.setSourceFile(file);
        config.setHandler(this);
        return fillConfiguration(config, file);
    }

    private CreationResult fillConfiguration(Config config, File file) {
        boolean existsBeforeLoad = file.exists();
        return fillConfiguration(config, existsBeforeLoad, YamlConfiguration.loadConfiguration(file));
    }

    private CreationResult fillConfiguration(Config config, boolean existsBeforeLoad, YamlConfiguration handle) {
        try {
            File file = config.getSourceFile();
            for (Field field : Arrays.stream(config.getClass().getFields())
                    .filter(f -> f.getAnnotation(ConfigPath.class) != null)
                    .collect(Collectors.toList())) {

                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    throw new IllegalStateException(String.format("Static field `%s` can't contains @ConfigPath annotation", field.getName()));
                } else if (Modifier.isFinal(modifiers)) {
                    throw new IllegalStateException(String.format("Field `%s` can't be final", field.getName()));
                } else if (field.get(config) == null) {
                    throw new IllegalStateException(String.format("Field `%s` can't null", field.getName()));
                }

                ConfigPath pathAnnotation = field.getAnnotation(ConfigPath.class);
                String configPath = pathAnnotation.value();
                Object object = handle.get(configPath);
                if (object == null) {
                    insertFieldToFile(config, field, configPath, handle);
                    log.info("Inserted field {}({}) into {}", field.getName(), configPath, file.getName());
                } else {
                    updateField(config, field, configPath, handle);
                    log.info("Updated value of path {} from {}", configPath, file.getName());
                }
            }
            return new CreationResult(existsBeforeLoad ? ResultType.LOADED : ResultType.CREATED, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new CreationResult(ResultType.ERROR, null);
        }
    }

    @SneakyThrows
    private void updateField(Config config, Field field, String path, YamlConfiguration handle) {
        Object configObject = Objects.requireNonNull(mapPathValue(field, handle, path),
                String.format("This config path `%s` is null", path));
        field.set(configObject, config);
    }

    @SneakyThrows
    private void insertFieldToFile(Config config, Field field, String path, YamlConfiguration handle) {
        Class<?> tClass = field.getType();
        Object value = field.get(config);
        if (tClass == Map.class) {
            Map<String, ?> map = (Map<String, ?>) value;
            handle.set(path, map);
        } else {
            handle.set(path, value);
        }
        handle.save(config.getSourceFile());
    }

    @SneakyThrows
    private Object mapPathValue(Field field, YamlConfiguration handle, String path) {
        Class<?> tClass = field.getType();
        if (tClass == Map.class) {
            Map<String, Object> map = new HashMap<>();
            for (String key : handle.getConfigurationSection(path).getKeys(false)) {
                String valuePath = path + "." + key;
                Object value = handle.get(valuePath);
                map.put(key, value);
            }
            return map;
        } else {
            return handle.get(path);
        }

//        if (tClass == String.class) {
//            return handle.getString(path);
//        } else if (tClass == Integer.class || tClass == int.class) {
//            return handle.getInt(path);
//        } else if (tClass == Double.class || tClass == double.class) {
//            return handle.getDouble(path);
//        } else if (tClass == Float.class || tClass == float.class) {
//            return (float) handle.getDouble(path);
//        } else if (tClass == Boolean.class || tClass == boolean.class) {
//            return handle.getBoolean(path);
//        } else if (tClass == ItemStack.class) {
//            return handle.getItemStack(path);
//        } else if (tClass == List.class) {
//            return handle.getList(path);
//        } else if (tClass == Map.class) {
//
//        }
    }

    @Value
    public static class CreationResult {

        ResultType result;
        Config config;

    }

    public enum ResultType {

        CREATED,
        LOADED,
        ERROR

    }

}
