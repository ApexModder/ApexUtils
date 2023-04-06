package xyz.apex.utils.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

record ConfigSerializerImpl<T>(BiFunction<T, JsonElement, T> deserializer, Function<T, JsonElement> serializer) implements ConfigSerializer<T>
{
    @Override
    public T deserialize(T defaultValue, JsonElement json)
    {
        return deserializer.apply(defaultValue, json);
    }

    @Override
    public JsonElement serialize(@Nullable T value)
    {
        return value == null ? JsonNull.INSTANCE : serializer.apply(value);
    }
}
