package xyz.apex.utils.config.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.apex.utils.config.ConfigBuilder;

import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ConfigTests
{
    @Test
    void existence()
    {
        var config = ConfigBuilder.builder("none_existent_config").build();
        Assertions.assertFalse(() -> Files.exists(config.path()), () -> "Expected config '%s' to not exist, but it does.".formatted(config.path()));

        var config1 = ConfigBuilder.builder("test").build();
        Assertions.assertTrue(() -> Files.exists(config1.path()), () -> "Expected config '%s' to exist, but it does not".formatted(config1.path()));
    }

    @Test
    void configs()
    {
        var builder = ConfigBuilder.builder("test");

        var cfgHello = builder.defineString("hello", "world");
        var cfgFunny = builder.defineInteger("funny", 420);
        var cfgSomeConfig = builder.defineString("some_config", "some_other_value");
        var cfgIAmABool = builder.defineBoolean("i_am_a_bool", true);
        var cfgIAmADouble = builder.defineDouble("i_am_a_double", 1D);
        var cfgIAmAFloat = builder.defineFloat("i_am_a_float", 1F);

        var config = builder.build();
        config.load();

        Assertions.assertEquals(cfgHello.get(), "world");
        Assertions.assertEquals(cfgFunny.get(), 69);
        Assertions.assertEquals(cfgSomeConfig.get(), "some_value");
        Assertions.assertEquals(cfgIAmABool.get(), true);
        Assertions.assertEquals(cfgIAmADouble.get(), 1D);
        Assertions.assertEquals(cfgIAmAFloat.get(), 1F);
    }

    @Test
    void listConfigs()
    {
        var builder = ConfigBuilder.builder("list_test");

        var cfgSomeList = builder.defineStringList("some_list", List.of("entry_a", "entry_b"), Collections.emptyList());
        var cfgInvalid = builder.defineStringList("some_invalid_list", List.of("some_value"), List.of("some_default_value"), List.of(String.valueOf(new Random().nextLong())));

        var config = builder.build();
        config.load();

        Assertions.assertEquals(cfgSomeList.get(), List.of("entry_a", "entry_b"));
        Assertions.assertNotEquals(cfgSomeList.get(), List.of("entry_b", "entry_a"));
        Assertions.assertFalse(cfgInvalid.add("some_value"));
    }
}
