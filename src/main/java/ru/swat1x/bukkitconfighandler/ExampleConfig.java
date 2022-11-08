package ru.swat1x.bukkitconfighandler;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.swat1x.bukkitconfighandler.annotation.ConfigPath;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExampleConfig extends Config {

    @ConfigPath("prefix")
    String messagePrefix = "&6&lPREFIX:&f ";
    // prefix: "&6&lPREFIX:&f "
    // On Loading colors translatins with ChatColor

    @ConfigPath("list")
    List<String> list = Lists.newArrayList("test", "list");
    // list:
    //   - "test"
    //   - "list"

    @ConfigPath("data.list")
    List<String> listInList = Lists.newArrayList("123", "345");
    // data:
    //   list:
    //     - "123"
    //     - "345"

}
