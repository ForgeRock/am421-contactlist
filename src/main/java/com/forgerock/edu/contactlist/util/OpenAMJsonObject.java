package com.forgerock.edu.contactlist.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Helper class to parse OpenAM structures easier. In many cases OpenAM returns
 * a JSON object, where all of the properties are arrays, regardless that most
 * of these properties have only a single value. (i.e. user profile properties
 * like uid or givenName). If you wrap a normal {@link JsonObject} into this
 * class, then the wrapper arrays are hidden.
 * <p>
 * For example given this JSON object: </p>
 * <code>{
 *  "uid" : ["john"],
 *  "givenName" : ["John"]
 * }</code>
 * <p>
 * You can easily get the {@code uid} attribute value as a String by simply
 * calling the {@link #getString(java.lang.String) } method which will return
 * with the String {@code "john"}.</p>
 *
 *
 * @author vrg
 */
public class OpenAMJsonObject implements JsonObject {

    private final JsonObject object;

    public OpenAMJsonObject(JsonObject object) {
        this.object = object;
    }

    @Override
    public JsonArray getJsonArray(String name) {
        return object.getJsonArray(name);
    }

    @Override
    public JsonObject getJsonObject(String name) {
        return object.getJsonObject(name);
    }

    @Override
    public JsonNumber getJsonNumber(String name) {
        return object.getJsonNumber(name);
    }

    @Override
    public JsonString getJsonString(String name) {
        return object.getJsonString(name);
    }

    @Override
    public String getString(String name) {
        return getString(name, null);
    }

    @Override
    public String getString(String name, String defaultValue) {
        JsonString value = getCasted(name);
        if (value == null) {
            return defaultValue;
        } else {
            return value.getString();
        }
    }

    @Override
    public int getInt(String name) {
        return ((JsonNumber) get(name)).intValue();
    }

    @Override
    public int getInt(String name, int defaultValue) {
        JsonNumber value = getCasted(name);
        if (value == null) {
            return defaultValue;
        } else {
            return value.intValue();
        }
    }

    @Override
    public boolean getBoolean(String name) {
        return get(name).getValueType() == ValueType.TRUE;
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        JsonValue value = get(name);
        if (value == null) {
            return defaultValue;
        }
        return value.getValueType() == ValueType.TRUE;
    }

    @Override
    public boolean isNull(String name) {
        return get(name) == null;
    }

    @Override
    public ValueType getValueType() {
        return object.getValueType();
    }

    @Override
    public int size() {
        return object.size();
    }

    @Override
    public boolean isEmpty() {
        return object.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return object.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return object.containsValue(value);
    }

    public <T extends JsonValue> T getCasted(Object key) {
        T value = (T) get(key);
        return value;
    }

    @Override
    public JsonValue get(Object key) {
        JsonValue value = object.get(key);
        if (value == null) {
            return null;
        }
        if (value.getValueType() == ValueType.ARRAY) {
            JsonArray array = (JsonArray) value;
            if (array.isEmpty()) {
                return null;
            }
            return array.get(0);
        } else {
            return value;
        }
    }

    @Override
    public JsonValue put(String key, JsonValue value) {
        return object.put(key, value);
    }

    @Override
    public JsonValue remove(Object key) {
        return object.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends JsonValue> m) {
        object.putAll(m);
    }

    @Override
    public void clear() {
        object.clear();
    }

    @Override
    public Set<String> keySet() {
        return object.keySet();
    }

    @Override
    public Collection<JsonValue> values() {
        return object.values();
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return object.entrySet();
    }

    @Override
    public JsonValue getOrDefault(Object key, JsonValue defaultValue) {
        return object.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super JsonValue> action) {
        object.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super JsonValue, ? extends JsonValue> function) {
        object.replaceAll(function);
    }

    @Override
    public JsonValue putIfAbsent(String key, JsonValue value) {
        return object.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return object.remove(key, value);
    }

    @Override
    public boolean replace(String key, JsonValue oldValue, JsonValue newValue) {
        return object.replace(key, oldValue, newValue);
    }

    @Override
    public JsonValue replace(String key, JsonValue value) {
        return object.replace(key, value);
    }

    @Override
    public JsonValue computeIfAbsent(String key, Function<? super String, ? extends JsonValue> mappingFunction) {
        return object.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public JsonValue computeIfPresent(String key, BiFunction<? super String, ? super JsonValue, ? extends JsonValue> remappingFunction) {
        return object.computeIfPresent(key, remappingFunction);
    }

    @Override
    public JsonValue compute(String key, BiFunction<? super String, ? super JsonValue, ? extends JsonValue> remappingFunction) {
        return object.compute(key, remappingFunction);
    }

    @Override
    public JsonValue merge(String key, JsonValue value, BiFunction<? super JsonValue, ? super JsonValue, ? extends JsonValue> remappingFunction) {
        return object.merge(key, value, remappingFunction);
    }

}
