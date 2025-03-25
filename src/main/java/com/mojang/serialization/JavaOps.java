package com.mojang.serialization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Ops for pure Java types.
 * This class MUST NOT discard any information (other than exact compound types) - there should be no data loss between 'create' and 'get' pairs.
 */
public class JavaOps implements DynamicOps<Object> {
    public static final JavaOps INSTANCE = new JavaOps();

    private JavaOps() {
    }

    @Override
    public Object empty() {
        return null;
    }

    @Override
    public Object emptyMap() {
        return Map.of();
    }

    @Override
    public Object emptyList() {
        return List.of();
    }

    @Override
    public <U> U convertTo(final DynamicOps<U> outOps, final Object input) {
        if (input == null) {
            return outOps.empty();
        }
        if (input instanceof Map) {
            return convertMap(outOps, input);
        }
        if (input instanceof ByteList) {
            return outOps.createByteList(ByteBuffer.wrap(((ByteList) input).toByteArray()));
        }
        if (input instanceof IntList) {
            return outOps.createIntList(((IntList) input).intStream());
        }
        if (input instanceof LongList) {
            return outOps.createLongList(((LongList) input).longStream());
        }
        if (input instanceof List) {
            return convertList(outOps, input);
        }
        if (input instanceof String) {
            return outOps.createString(((String) input));
        }
        if (input instanceof Boolean) {
            return outOps.createBoolean(((Boolean) input));
        }
        if (input instanceof Byte) {
            return outOps.createByte(((Byte) input));
        }
        if (input instanceof Short) {
            return outOps.createShort(((Short) input));
        }
        if (input instanceof Integer) {
            return outOps.createInt(((Integer) input));
        }
        if (input instanceof Long) {
            return outOps.createLong(((Long) input));
        }
        if (input instanceof Float) {
            return outOps.createFloat(((Float) input));
        }
        if (input instanceof Double) {
            return outOps.createDouble(((Double) input));
        }
        if (input instanceof Number) {
            return outOps.createNumeric(((Number) input));
        }
        throw new IllegalStateException("Don't know how to convert " + input);
    }

    @Override
    public DataResult<Number> getNumberValue(final Object input) {
        if (input instanceof Number) {
            return DataResult.success((Number) input);
        }
        return DataResult.error(() -> "Not a number: " + input);
    }

    @Override
    public Object createNumeric(final Number value) {
        return value;
    }

    @Override
    public Object createByte(final byte value) {
        return value;
    }

    @Override
    public Object createShort(final short value) {
        return value;
    }

    @Override
    public Object createInt(final int value) {
        return value;
    }

    @Override
    public Object createLong(final long value) {
        return value;
    }

    @Override
    public Object createFloat(final float value) {
        return value;
    }

    @Override
    public Object createDouble(final double value) {
        return value;
    }

    @Override
    public DataResult<Boolean> getBooleanValue(final Object input) {
        if (input instanceof Boolean) {
            return DataResult.success(((Boolean) input));
        }
        return DataResult.error(() -> "Not a boolean: " + input);
    }

    @Override
    public Object createBoolean(final boolean value) {
        return value;
    }

    @Override
    public DataResult<String> getStringValue(final Object input) {
        if (input instanceof String) {
            return DataResult.success(((String) input));
        }
        return DataResult.error(() -> "Not a string: " + input);
    }

    @Override
    public Object createString(final String value) {
        return value;
    }

    @Override
    public DataResult<Object> mergeToList(final Object input, final Object value) {
        if (input == empty()) {
            return DataResult.success(List.of(value));
        }
        if (input instanceof List) {
            final List<?> list = (List<?>) input;
            if (list.isEmpty()) {
                return DataResult.success(List.of(value));
            }
            return DataResult.success(ImmutableList.builder().addAll(list).add(value).build());
        }
        return DataResult.error(() -> "Not a list: " + input);
    }

    @Override
    public DataResult<Object> mergeToList(final Object input, final List<Object> values) {
        if (input == empty()) {
            return DataResult.success(values);
        }
        if (input instanceof List) {
            final List<?> list = (List<?>) input;
            if (list.isEmpty()) {
                return DataResult.success(values);
            }
            return DataResult.success(ImmutableList.builder().addAll(list).addAll(values).build());
        }
        return DataResult.error(() -> "Not a list: " + input);
    }

    @Override
    public DataResult<Object> mergeToMap(final Object input, final Object key, final Object value) {
        if (input == empty()) {
            return DataResult.success(Map.of(key, value));
        }
        if (input instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) input;
            if (map.isEmpty()) {
                return DataResult.success(Map.of(key, value));
            }
            final ImmutableMap.Builder<Object, Object> result = ImmutableMap.builderWithExpectedSize(map.size() + 1);
            result.putAll(map);
            result.put(key, value);
            return DataResult.success(result.buildKeepingLast());
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    @Override
    public DataResult<Object> mergeToMap(final Object input, final Map<Object, Object> values) {
        if (input == empty()) {
            return DataResult.success(values);
        }
        if (input instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) input;
            if (map.isEmpty()) {
                return DataResult.success(values);
            }
            final ImmutableMap.Builder<Object, Object> result = ImmutableMap.builderWithExpectedSize(map.size() + values.size());
            result.putAll(map);
            result.putAll(values);
            return DataResult.success(result.buildKeepingLast());
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    private static Map<Object, Object> mapLikeToMap(final MapLike<Object> values) {
        return values.entries().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
    }

    @Override
    public DataResult<Object> mergeToMap(final Object input, final MapLike<Object> values) {
        if (input == empty()) {
            return DataResult.success(mapLikeToMap(values));
        }
        if (input instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) input;
            if (map.isEmpty()) {
                return DataResult.success(mapLikeToMap(values));
            }

            final ImmutableMap.Builder<Object, Object> result = ImmutableMap.builderWithExpectedSize(map.size());
            result.putAll(map);
            values.entries().forEach(e -> result.put(e.getFirst(), e.getSecond()));
            return DataResult.success(result.buildKeepingLast());
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    private static Stream<Pair<Object, Object>> getMapEntries(final Map<?, ?> input) {
        return input.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue()));
    }

    @Override
    public DataResult<Stream<Pair<Object, Object>>> getMapValues(final Object input) {
        if (input instanceof Map) {
            return DataResult.success(getMapEntries(((Map<?, ?>) input)));
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    @Override
    public DataResult<Consumer<BiConsumer<Object, Object>>> getMapEntries(final Object input) {
        if (input instanceof Map) {
            return DataResult.success(consumer -> ((Map<?, ?>) input).forEach(consumer));
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    @Override
    public Object createMap(final Stream<Pair<Object, Object>> map) {
        return map.collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
    }

    @Override
    public DataResult<MapLike<Object>> getMap(final Object input) {
        if (input instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) input;
            return DataResult.success(
                new MapLike<>() {
                    @Nullable
                    @Override
                    public Object get(final Object key) {
                        return map.get(key);
                    }

                    @Nullable
                    @Override
                    public Object get(final String key) {
                        return map.get(key);
                    }

                    @Override
                    public Stream<Pair<Object, Object>> entries() {
                        return getMapEntries(map);
                    }

                    @Override
                    public String toString() {
                        return "MapLike[" + map + "]";
                    }
                }
            );
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    @Override
    public Object createMap(final Map<Object, Object> map) {
        return map;
    }

    @Override
    public DataResult<Stream<Object>> getStream(final Object input) {
        if (input instanceof List<?>) {
            return DataResult.success(((List<?>) input).stream().map(o -> o));
        }
        return DataResult.error(() -> "Not an list: " + input);
    }

    @Override
    public DataResult<Consumer<Consumer<Object>>> getList(final Object input) {
        if (input instanceof List) {
            return DataResult.success(consumer -> ((List<?>) input).forEach(consumer));
        }
        return DataResult.error(() -> "Not an list: " + input);
    }

    @Override
    public Object createList(final Stream<Object> input) {
        return List.of(input.toArray());
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(final Object input) {
        if (input instanceof ByteList) {
            return DataResult.success(ByteBuffer.wrap(((ByteList) input).toByteArray()));
        }
        return DataResult.error(() -> "Not a byte list: " + input);
    }

    @Override
    public Object createByteList(final ByteBuffer input) {
        // Set .limit to .capacity to match default method
        final ByteBuffer wholeBuffer = input.duplicate().clear();
        final ByteArrayList result = new ByteArrayList();
        result.size(wholeBuffer.capacity());
        int position = wholeBuffer.position();
        try {
            wholeBuffer.position(0);
            wholeBuffer.get(result.elements(), 0, result.size());
        } finally {
            wholeBuffer.position(position);
        }
        return result;
    }

    @Override
    public DataResult<IntStream> getIntStream(final Object input) {
        if (input instanceof IntList) {
            return DataResult.success(((IntList) input).intStream());
        }
        return DataResult.error(() -> "Not an int list: " + input);
    }

    @Override
    public Object createIntList(final IntStream input) {
        return IntArrayList.toList(input);
    }

    @Override
    public DataResult<LongStream> getLongStream(final Object input) {
        if (input instanceof LongList) {
            return DataResult.success(((LongList) input).longStream());
        }
        return DataResult.error(() -> "Not a long list: " + input);
    }

    @Override
    public Object createLongList(final LongStream input) {
        return LongArrayList.toList(input);
    }

    @Override
    public Object remove(final Object input, final String key) {
        if (input instanceof Map) {
            final Map<Object, Object> result = new LinkedHashMap<>(((Map<?, ?>) input));
            result.remove(key);
            return Map.copyOf(result);
        }
        return input;
    }

    @Override
    public RecordBuilder<Object> mapBuilder() {
        return new FixedMapBuilder<>(this);
    }

    @Override
    public String toString() {
        return "Java";
    }

    private static final class FixedMapBuilder<T> extends RecordBuilder.AbstractUniversalBuilder<T, ImmutableMap.Builder<T, T>> {
        public FixedMapBuilder(final DynamicOps<T> ops) {
            super(ops);
        }

        @Override
        protected ImmutableMap.Builder<T, T> initBuilder() {
            return ImmutableMap.builder();
        }

        @Override
        protected ImmutableMap.Builder<T, T> append(final T key, final T value, final ImmutableMap.Builder<T, T> builder) {
            return builder.put(key, value);
        }

        @Override
        protected DataResult<T> build(final ImmutableMap.Builder<T, T> builder, final T prefix) {
            final ImmutableMap<T, T> result = builder.buildKeepingLast();
            return ops().mergeToMap(prefix, result);
        }
    }
}
