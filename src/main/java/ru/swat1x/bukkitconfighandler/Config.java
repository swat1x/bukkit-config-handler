package ru.swat1x.bukkitconfighandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.File;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Config {

    File sourceFile;
    ConfigHandler handler;

    public Config(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Config(ConfigHandler handler) {
        this.handler = handler;
    }

    public void reload() {
        checkHandler();
        checkFile();

        handler.reloadConfig(this, sourceFile);
    }

    public void checkHandler() {
        if (handler == null) {
            throw new IllegalStateException("Can't reload config while handler is null");
        }
    }

    public void checkFile() {
        if (sourceFile == null) {
            throw new IllegalStateException("Can't reload config while file is null");
        }
    }

}
