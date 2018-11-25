package com.forgerock.edu.contactlist.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 *
 * @author vrg
 */
public class NullSafeJsonObjectBuilder implements JsonObjectBuilder {

    private final JsonObjectBuilder origBuilder;
    private boolean addNulls;

    public NullSafeJsonObjectBuilder() {
        this(Json.createObjectBuilder());
    }

    public NullSafeJsonObjectBuilder(JsonObjectBuilder origBuilder) {
        this.origBuilder = origBuilder;
    }
    
    public JsonObjectBuilder withNulls() {
        addNulls = true;
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, JsonValue value) {
        if (value != null) {
            origBuilder.add(name, value);
        }
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, String value) {
        if (value != null) {
            origBuilder.add(name, value);
        } else if (addNulls) {
            addNull(name);
        }
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, BigInteger value) {
        if (value != null) {
            origBuilder.add(name, value);
        } else if (addNulls) {
            addNull(name);
        }
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, BigDecimal value) {
        if (value != null) {
            origBuilder.add(name, value);
        } else if (addNulls) {
            addNull(name);
        }
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, int value) {
        origBuilder.add(name, value);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, long value) {
        origBuilder.add(name, value);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, double value) {
        origBuilder.add(name, value);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, boolean value) {
        origBuilder.add(name, value);
        return this;
    }

    @Override
    public JsonObjectBuilder addNull(String name) {
        origBuilder.addNull(name);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, JsonObjectBuilder builder) {
        origBuilder.add(name, builder);
        return this;

    }

    @Override
    public JsonObjectBuilder add(String name, JsonArrayBuilder builder) {
        origBuilder.add(name, builder);
        return this;
    }

    @Override
    public JsonObject build() {
        return origBuilder.build();
    }

}
