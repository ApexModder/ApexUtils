package xyz.apex.utils.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @see ConfigSerializer
 */
public interface ConfigSerializers
{
    /**
     * Used for String ConfigValue serialization
     */
    ConfigSerializer<String> STRING = primitive(
            (defaultValue, json) -> json.isString() ? json.getAsString() : defaultValue,
            JsonPrimitive::new
    );

    /**
     * Used for Integer ConfigValue serialization
     */
    ConfigSerializer<Integer> INTEGER = numeric(JsonPrimitive::getAsInt);

    /**
     * Used for Double ConfigValue serialization
     */
    ConfigSerializer<Double> DOUBLE = numeric(JsonPrimitive::getAsDouble);

    /**
     * Used for Float ConfigValue serialization
     */
    ConfigSerializer<Float> FLOAT = numeric(JsonPrimitive::getAsFloat);

    /**
     * Used for Long ConfigValue serialization
     */
    ConfigSerializer<Long> LONG = numeric(JsonPrimitive::getAsLong);

    /**
     * Used for Boolean ConfigValue serialization
     */
    ConfigSerializer<Boolean> BOOLEAN = primitive(
            (defaultValue, json) -> json.isBoolean() ? json.getAsBoolean() : defaultValue,
            JsonPrimitive::new
    );

    /**
     * Used to serialize a List of String ConfigValues
     */
    ConfigSerializer<List<String>> STRING_LIST = collection(STRING, Lists::newArrayList, List::add);

    /**
     * Used to serialize a List of Integer ConfigValues
     */
    ConfigSerializer<List<Integer>> INTEGER_LIST = collection(INTEGER, Lists::newArrayList, List::add);

    /**
     * Used to serialize a List of Double ConfigValues
     */
    ConfigSerializer<List<Double>> DOUBLE_LIST = collection(DOUBLE, Lists::newArrayList, List::add);

    /**
     * Used to serialize a List of Float ConfigValues
     */
    ConfigSerializer<List<Float>> FLOAT_LIST = collection(FLOAT, Lists::newArrayList, List::add);

    /**
     * Used to serialize a List of Long ConfigValues
     */
    ConfigSerializer<List<Long>> LONG_LIST = collection(LONG, Lists::newArrayList, List::add);

    /**
     * Used to serialize a List of Boolean ConfigValues
     */
    ConfigSerializer<List<Boolean>> BOOLEAN_LIST = collection(BOOLEAN, Lists::newArrayList, List::add);

    private static <T> ConfigSerializer<T> primitive(BiFunction<T, JsonPrimitive, T> deserializer, Function<T, JsonPrimitive> serializer)
    {
        return new ConfigSerializerImpl<>(
                (defaultValue, json) -> json instanceof JsonPrimitive prim ? deserializer.apply(defaultValue, prim) : defaultValue,
                serializer::apply
        );
    }

    private static <N extends Number> ConfigSerializer<N> numeric(Function<JsonPrimitive, N> deserializer)
    {
        return primitive(
                (defaultValue, json) -> json.isNumber() ? deserializer.apply(json) : defaultValue,
                JsonPrimitive::new
        );
    }

    @SuppressWarnings("DataFlowIssue")
    private static <T, C extends Collection<T>> ConfigSerializer<C> collection(ConfigSerializer<T> serializer, Supplier<C> collectionFactory, BiConsumer<C, T> collectionAdder)
    {
        return new ConfigSerializerImpl<>(
                (defaultValue, json) -> {
                    if(!json.isJsonArray()) return defaultValue;
                    var collection = collectionFactory.get();
                    json.getAsJsonArray().asList().stream().map(element -> serializer.deserialize(null, element)).forEach(deserialized -> collectionAdder.accept(collection, deserialized));
                    return collection;
                },
                value -> {
                    if(value.isEmpty()) return JsonNull.INSTANCE;
                    var json = new JsonArray();
                    value.stream().map(serializer::serialize).forEach(json::add);
                    return json;
                }
        );
    }

    @ApiStatus.Internal
    static void bootstrap() {}
}
