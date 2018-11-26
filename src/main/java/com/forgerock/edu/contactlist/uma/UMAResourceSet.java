package com.forgerock.edu.contactlist.uma;

import com.forgerock.edu.contactlist.entity.JSONEntity;
import com.forgerock.edu.contactlist.util.JsonUtil;
import com.forgerock.edu.contactlist.util.NullSafeJsonObjectBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 *
 * @author vrg
 */
public class UMAResourceSet implements JSONEntity {

    private String id;
    private String name;
    private String iconURI;
    private Set<String> scopes = new TreeSet<>();
    private Set<String> unmodifiableScopes;
    private Set<String> labels = new TreeSet<>();
    private Set<String> unmodifiableLabels;
    private String type;
    private String revision;
    private String userAccessPolicyURI;

    public UMAResourceSet() {
        createWrappers();
    }

    public UMAResourceSet(UMAResourceSet other) {
        this.id = other.id;
        this.name = other.name;
        this.iconURI = other.iconURI;
        this.scopes = new TreeSet<>(other.scopes);
        this.labels = new TreeSet<>(other.labels);
        this.type = other.type;
        this.revision = other.revision;
        createWrappers();
    }

    private void createWrappers() {
        unmodifiableScopes = Collections.unmodifiableSet(scopes);
        unmodifiableLabels = Collections.unmodifiableSet(labels);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconURI() {
        return iconURI;
    }

    public Set<String> getScopes() {
        return unmodifiableScopes;
    }

    public Set<String> getLabels() {
        return unmodifiableLabels;
    }

    public String getType() {
        return type;
    }

    @Override
    public JsonObject getJsonObject() {
        JsonObject json = new NullSafeJsonObjectBuilder()
                .add("name", name)
                .add("icon_uri", iconURI)
                .add("type", type)
                .add("scopes", scopes.stream().collect(JsonUtil.toJsonArrayOfStrings()))
                .add("labels", labels.stream().collect(JsonUtil.toJsonArrayOfStrings()))
                .add("_id", id)
                .add("_rev", revision)
                .add("user_access_policy_uri", userAccessPolicyURI)
                .build();
        return json;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public void setJsonObject(JsonObject object) {
        id = object.getString("_id", null);
        name = object.getString("name", null);
        iconURI = object.getString("icon_uri", null);
        type = object.getString("type", null);
        scopes = object.getJsonArray("scopes").stream()
                .filter((element) -> element.getValueType() == JsonValue.ValueType.STRING)
                .map((element) -> ((JsonString) element).getString())
                .collect(Collectors.toCollection(() -> new TreeSet<String>()));

        labels = object.getJsonArray("labels").stream()
                .filter((element) -> element.getValueType() == JsonValue.ValueType.STRING)
                .map((element) -> ((JsonString) element).getString())
                .collect(Collectors.toCollection(() -> new TreeSet<String>()));
        
        userAccessPolicyURI = object.getString("user_access_policy_uri", null);
        createWrappers();
    }

    @Override
    public String toString() {
        return getJsonObject().toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UMAResourceSet rs = new UMAResourceSet();

        public Builder resourceSet(UMAResourceSet resourceSet) {
            rs = new UMAResourceSet(resourceSet);
            return this;
        }

        public Builder id(String id) {
            rs.id = id;
            return this;
        }

        public Builder revision(String revision) {
            rs.revision = revision;
            return this;
        }

        public Builder jsonState(JsonObject jsonState) {
            rs.setJsonObject(jsonState);
            return this;
        }

        public Builder addDataFromCreatedResponse(JsonObject jsonState) {
            rs.id = jsonState.getString("_id");
            rs.userAccessPolicyURI = jsonState.getString("user_access_policy_uri", null);
            return this;
        }

        public Builder type(String type) {
            rs.type = type;
            return this;
        }

        public Builder name(String name) {
            rs.name = name;
            return this;
        }

        public Builder iconURI(String iconURI) {
            rs.iconURI = iconURI;
            return this;
        }

        public Builder addScope(String scope) {
            rs.scopes.add(scope);
            return this;
        }

        public Builder addLabel(String label) {
            rs.labels.add(label);
            return this;
        }

        public Builder scopes(String... scopes) {
            rs.scopes.addAll(Arrays.asList(scopes));
            return this;
        }

        public Builder labels(String... labels) {
            rs.labels.addAll(Arrays.asList(labels));
            return this;
        }

        public UMAResourceSet build() {
            return rs;
        }
    }

}
