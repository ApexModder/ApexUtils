package xyz.apex.utils.config;

import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.stream.Collectors;

non-sealed class ConfigValueImpl<T> implements ConfigValue<T>
{
    protected final ConfigImpl config;
    protected final String key;
    private T value;
    protected final T defaultValue;
    protected boolean isDirty = false;
    protected final ConfigSerializer<T> serializer;

    protected ConfigValueImpl(Config config, String key, T initialValue,  T defaultValue, ConfigSerializer<T> serializer)
    {
        Validate.isInstanceOf(ConfigImpl.class, config);
        this.config = (ConfigImpl) config;
        this.key = key;
        value = initialValue;
        this.defaultValue = defaultValue;
        this.serializer = serializer;
    }

    @Override
    public final Config config()
    {
        return config;
    }

    @Override
    public final String key()
    {
        return key;
    }

    @Override
    public final T get()
    {
        return value;
    }

    @Override
    public final void set(T value)
    {
        if(this.value == value) return;
        this.value = value;
        if(config.canBeDirty) isDirty = true;
    }

    @Override
    public final T defaultValue()
    {
        return defaultValue;
    }

    @Override
    public final boolean isDefault()
    {
        return isDefault(value);
    }

    @Override
    public final boolean isDefault(T value)
    {
        return value == defaultValue;
    }

    @Override
    public final boolean isDirty()
    {
        return config.canBeDirty && isDirty;
    }

    @Override
    public final ConfigSerializer<T> serializer()
    {
        return serializer;
    }

    @Override
    public final boolean equals(Object obj)
    {
        if(this == obj) return true;
        if(!(obj instanceof ConfigValue<?> other)) return false;
        return config.equals(other.config()) && key.equals(other.key()) && value == other.get();
    }

    @Override
    public final int hashCode()
    {
        return key.hashCode();
    }

    @Override
    public final String toString()
    {
        return "ConfigValue[%s=%s]".formatted(key, value);
    }

    static final class BooleanImpl extends ConfigValueImpl<java.lang.Boolean> implements Boolean
    {
        BooleanImpl(Config config, String key, boolean initialValue, boolean defaultValue)
        {
            super(config, key, initialValue, defaultValue, ConfigSerializers.BOOLEAN);
        }
    }

    static non-sealed class NumericImpl<N extends Number> extends ConfigValueImpl<N> implements Numeric<N>
    {
        protected final N minValue;
        protected final N maxValue;

        protected NumericImpl(Config config, String key, N initialValue, N defaultValue, N minValue, N maxValue, ConfigSerializer<N> serializer)
        {
            super(config, key, initialValue, defaultValue, serializer);

            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public final N minValue()
        {
            return minValue;
        }

        @Override
        public final N maxValue()
        {
            return maxValue;
        }

        @Override
        public final int getAsInt()
        {
            return Numeric.super.getAsInt();
        }

        @Override
        public final double getAsDouble()
        {
            return Numeric.super.getAsDouble();
        }

        @Override
        public final float getAsFloat()
        {
            return Numeric.super.getAsFloat();
        }

        @Override
        public final long getAsLong()
        {
            return Numeric.super.getAsLong();
        }

        @Override
        public final boolean getAsBoolean()
        {
            return Numeric.super.getAsBoolean();
        }
    }

    static final class IntegerImpl extends NumericImpl<java.lang.Integer> implements Integer
    {
        IntegerImpl(Config config, String key, int initialValue, int defaultValue, int minValue, int maxValue)
        {
            super(config, key, initialValue, defaultValue, minValue, maxValue, ConfigSerializers.INTEGER);
        }
    }

    static final class DoubleImpl extends NumericImpl<java.lang.Double> implements Double
    {
        DoubleImpl(Config config, String key, double initialValue, double defaultValue, double minValue, double maxValue)
        {
            super(config, key, initialValue, defaultValue, minValue, maxValue, ConfigSerializers.DOUBLE);
        }
    }

    static final class FloatImpl extends NumericImpl<java.lang.Float> implements Float
    {
        FloatImpl(Config config, String key, float initialValue, float defaultValue, float minValue, float maxValue)
        {
            super(config, key, initialValue, defaultValue, minValue, maxValue, ConfigSerializers.FLOAT);
        }
    }

    static final class LongImpl extends NumericImpl<java.lang.Long> implements Long
    {
        LongImpl(Config config, String key, long initialValue, long defaultValue, long minValue, long maxValue)
        {
            super(config, key, initialValue, defaultValue, minValue, maxValue, ConfigSerializers.LONG);
        }
    }

    static final class ListImpl<T> implements ConfigValue<java.util.List<T>>, List<T>
    {
        private final ConfigImpl config;
        private final String key;
        private final java.util.List<T> defaultValues;
        private final java.util.List<T> possibleValues;
        private final java.util.List<T> values;
        private final java.util.List<T> view;
        private boolean isDirty = false;
        private final ConfigSerializer<java.util.List<T>> serializer;

        ListImpl(Config config, String key, java.util.List<T> initialValues, java.util.List<T> defaultValues, java.util.List<T> possibleValues, ConfigSerializer<java.util.List<T>> serializer)
        {
            Validate.isInstanceOf(ConfigImpl.class, config);
            this.config = (ConfigImpl) config;
            this.key = key;
            this.possibleValues = Collections.unmodifiableList(possibleValues);
            // filter out any invalid values
            // TODO: maybe we should throw exceptions instead of filtering?
            this.defaultValues = defaultValues.stream().filter(this::isValid).toList();
            values = initialValues.stream().filter(this::isValid).collect(Collectors.toList()); // mutable copy
            // immutable view
            // must use the list methods implemented here to mutate the list
            // as they update the config value & config respectively marking it as dirty
            view = Collections.unmodifiableList(values);
            this.serializer = serializer;
        }

        // region: ConfigValue
        @Override
        public Config config()
        {
            return config;
        }

        @Override
        public String key()
        {
            return key;
        }

        @Override
        public java.util.List<T> get()
        {
            return view;
        }

        @Override
        public void set(java.util.List<T> values)
        {
            var valid = values.stream().filter(this::isValid).toList();
            if(valid.isEmpty()) return;
            if(this.values.equals(valid)) return;
            this.values.clear();
            this.values.addAll(valid);
            isDirty = true;
        }

        @Override
        public java.util.List<T> defaultValue()
        {
            return defaultValues;
        }

        @Override
        public java.util.List<T> possibleValues()
        {
            return possibleValues;
        }

        @Override
        public boolean isDirty()
        {
            return config.canBeDirty && isDirty;
        }

        @Override
        public boolean isDefault()
        {
            return isDefault(view);
        }

        @Override
        public boolean isDefault(java.util.List<T> values)
        {
            return defaultValues.equals(values);
        }

        @Override
        public boolean isValid(T value)
        {
            // if possible values is empty, allow EVERYTHING
            // if possible values is not empty, only allow what 'possibleValues' contains
            return possibleValues.isEmpty() || possibleValues.contains(value);
        }

        @Override
        public ConfigSerializer<java.util.List<T>> serializer()
        {
            return serializer;
        }
        // endregion

        // region: List
        @Override
        public int size()
        {
            return values.size();
        }

        @Override
        public boolean isEmpty()
        {
            return values.isEmpty();
        }

        @Override
        public boolean contains(Object o)
        {
            return values.contains(o);
        }

        @Override
        public Iterator<T> iterator()
        {
            return values.iterator();
        }

        @Override
        public Object[] toArray()
        {
            return values.toArray();
        }

        @Override
        public <A> A[] toArray(A[] a)
        {
            return values.toArray(a);
        }

        @Override
        public boolean add(T t)
        {
            if(isValid(t) && values.add(t))
            {
                isDirty = true;
                return true;
            }

            return false;
        }

        @Override
        public boolean remove(Object o)
        {
            if(values.remove(o))
            {
                isDirty = true;
                return true;
            }

            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            // covers, shhhhhh this is fine :)
            // whos going to be calling ListConfigValue.containsAll() anyways
            return values.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c)
        {
            // only valid values
            var valid = c.stream().filter(this::isValid).toList();
            if(valid.isEmpty()) return false;

            if(values.addAll(valid))
            {
                isDirty = true;
                return true;
            }

            return false;
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c)
        {
            // only valid values
            var valid = c.stream().filter(this::isValid).toList();
            if(valid.isEmpty()) return false;

            if(values.addAll(index, valid))
            {
                isDirty = true;
                return true;
            }

            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            if(values.removeAll(c))
            {
                isDirty = true;
                return true;
            }

            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            // this::isValid is type bound to T
            // possibleValues::contains is not
            var valid = possibleValues.isEmpty() ? c : c.stream().filter(possibleValues::contains).toList();
            if(valid.isEmpty()) return false;

            if(values.retainAll(valid))
            {
                isDirty = true;
                return true;
            }

            return false;
        }

        @Override
        public void clear()
        {
            values.clear();
            isDirty = true;
        }

        @Override
        public T get(int index)
        {
            return values.get(index);
        }

        @Override
        public T set(int index, T element)
        {
            if(!isValid(element)) throw new IllegalArgumentException("Can not use #set(int, T) to set invalid elements");
            isDirty = true;
            return values.set(index, element);
        }

        @Override
        public void add(int index, T element)
        {
            if(!isValid(element)) return;
            isDirty = true;
            values.add(index, element);
        }

        @Override
        public T remove(int index)
        {
            isDirty = true;
            return values.remove(index);
        }

        @Override
        public int indexOf(Object o)
        {
            return values.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o)
        {
            return values.lastIndexOf(o);
        }

        @Override
        public ListIterator<T> listIterator()
        {
            return values.listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index)
        {
            return values.listIterator(index);
        }

        @Override
        public java.util.List<T> subList(int fromIndex, int toIndex)
        {
            return values.subList(fromIndex, toIndex);
        }
        // endregion
    }
}
