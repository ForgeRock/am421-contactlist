package com.forgerock.edu.contactlist.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.forgerock.opendj.ldap.Attribute;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.Entry;
import org.forgerock.opendj.ldap.ErrorResultIOException;
import org.forgerock.opendj.ldap.SearchResultReferenceIOException;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;
import org.forgerock.opendj.ldif.ConnectionEntryReader;

/**
 *
 * @author vrg
 */
public class JsonUtil {

    public static JsonArray toArray(ConnectionEntryReader entryReader) throws ErrorResultIOException, SearchResultReferenceIOException {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        while (entryReader.hasNext()) {
            SearchResultEntry entry = entryReader.readEntry();
            builder.add(toObject(entry));
        }
        return builder.build();
    }

    public static JsonObject toObject(Entry entry) {
        JsonObjectBuilder obBuilder = Json.createObjectBuilder();

        for (Attribute attribute : entry.getAllAttributes()) {
            JsonArrayBuilder valueArrayBuilder = Json.createArrayBuilder();
            for (ByteString value : attribute) {
                valueArrayBuilder.add(value.toString());
            }
            obBuilder.add(attribute.getAttributeDescriptionAsString(), valueArrayBuilder);
        }
        return obBuilder.build();
    }

    public static Collector<JsonValue, JsonArrayBuilder, JsonArray> toJsonArray() {
        return new JsonArrayCollector<>(
                (JsonArrayBuilder builder, JsonValue value) -> builder.add(value));
    }

    public static Collector<String, JsonArrayBuilder, JsonArray> toJsonArrayOfStrings() {
        return new JsonArrayCollector<>(
                (JsonArrayBuilder builder, String value) -> builder.add(value));
    }

    public static Collector<Long, JsonArrayBuilder, JsonArray> toJsonArrayOfLongs() {
        return new JsonArrayCollector<>(
                (JsonArrayBuilder builder, Long value) -> builder.add(value));
    }

    public static <T> Collector<T, JsonArrayBuilder, JsonArray> toJsonArray(BiConsumer<JsonArrayBuilder, T> accumulator) {
        return new JsonArrayCollector<>(accumulator);
    }

    static class JsonArrayCollector<T> implements Collector<T, JsonArrayBuilder, JsonArray> {

        private final BiConsumer<JsonArrayBuilder, T> accumulator;

        public JsonArrayCollector(BiConsumer<JsonArrayBuilder, T> accumulator) {
            this.accumulator = accumulator;
        }

        @Override
        public Supplier<JsonArrayBuilder> supplier() {
            return () -> Json.createArrayBuilder();
        }

        @Override
        public BiConsumer<JsonArrayBuilder, T> accumulator() {
            return accumulator;
        }

        @Override
        public BinaryOperator<JsonArrayBuilder> combiner() {
            return (JsonArrayBuilder builder1, JsonArrayBuilder builder2)
                    -> builder1.add(builder2);
        }

        @Override
        public Function<JsonArrayBuilder, JsonArray> finisher() {
            return (JsonArrayBuilder builder) -> builder.build();
        }

        @Override
        public Set<Collector.Characteristics> characteristics() {
            return Collections.unmodifiableSet(EnumSet.noneOf(Collector.Characteristics.class));
        }
    }

    public static Function<JsonValue, String> jsonValueAsString() {
        return (jsonValue) -> {
            switch (jsonValue.getValueType()) {
                case STRING:
                    return ((JsonString) jsonValue).getString();
                case NULL:
                    return null;
                default:
                    return jsonValue.toString();
            }
        };
    }
}
