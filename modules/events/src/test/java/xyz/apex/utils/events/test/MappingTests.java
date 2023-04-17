package xyz.apex.utils.events.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public final class MappingTests
{
    @BeforeEach
    void setup()
    {
        TestEvent.EVENT_TYPE.addListener(someDummyListenerToActuallyPostTheEvent -> {});
    }

    @Test
    void map()
    {
        var result = TestEvent.EVENT_TYPE.post();
        var mapped = result.map(e -> "Hello").orElse("Invalid Result!");
        Assertions.assertEquals("Hello", mapped);
    }

    @Test
    void flatMap()
    {
        var result = TestEvent.EVENT_TYPE.post();
        var mapped = result.flatMap(e -> Optional.of("Hello")).orElse("Invalid Result!");
        Assertions.assertEquals("Hello", mapped);
    }

    @Test
    void mapToInt()
    {
        var result = TestEvent.EVENT_TYPE.post();
        var mapped = result.mapToInt(e -> 1).orElse(0);
        Assertions.assertEquals(1, mapped);
    }

    @Test
    void mapToLong()
    {
        var result = TestEvent.EVENT_TYPE.post();
        var mapped = result.mapToLong(e -> 1L).orElse(0L);
        Assertions.assertEquals(1L, mapped);
    }

    @Test
    void mapToDouble()
    {
        var result = TestEvent.EVENT_TYPE.post();
        var mapped = result.mapToDouble(e -> 1D).orElse(0D);
        Assertions.assertEquals(1D, mapped);
    }
}
