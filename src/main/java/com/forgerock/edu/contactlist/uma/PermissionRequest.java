package com.forgerock.edu.contactlist.uma;

import com.forgerock.edu.contactlist.entity.JSONEntity;
import com.forgerock.edu.contactlist.util.JsonUtil;
import com.forgerock.edu.contactlist.util.NullSafeJsonObjectBuilder;
import com.forgerock.edu.contactlist.util.StringUtil;
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
public class PermissionRequest implements JSONEntity {

    private String resourceSetId;
    private Set<String> scopes = new TreeSet<>();
    private Set<String> unmodifiableScopes;

    public PermissionRequest() {
        createWrappers();
    }

    private void createWrappers() {
        unmodifiableScopes = Collections.unmodifiableSet(scopes);
    }

    public String getResourceSetId() {
        return resourceSetId;
    }

    public Set<String> getScopes() {
        return unmodifiableScopes;
    }

    @Override
    public JsonObject getJsonObject() {
        JsonObject json = new NullSafeJsonObjectBuilder()
                .add("resource_id", resourceSetId)
                .add("resource_scopes", scopes.stream().collect(JsonUtil.toJsonArrayOfStrings()))
                .build();
        return json;
    }

    @Override
    public void setJsonObject(JsonObject object) {
        resourceSetId = object.getString("access_token");
        scopes = object.getJsonArray("scopes").stream()
                .map(JsonUtil.jsonValueAsString())
                .collect(Collectors.toCollection(() -> new TreeSet<String>()));
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

        private PermissionRequest rs = new PermissionRequest();

        public Builder jsonState(JsonObject jsonState) {
            rs.setJsonObject(jsonState);
            return this;
        }

        public Builder resourceSetId(String tokenId) {
            rs.resourceSetId = tokenId;
            return this;
        }

        public Builder addScope(String scope) {
            rs.scopes.add(scope);
            return this;
        }

        public Builder scopes(String... scopes) {
            rs.scopes.addAll(Arrays.asList(scopes));
            return this;
        }
        
        public Builder resourceSet(UMAResourceSet resourceSet) {
            rs.resourceSetId = resourceSet.getId();
            rs.scopes = new TreeSet<>(resourceSet.getScopes());
            return this;
        }

        public PermissionRequest build() {
            return rs;
        }
    }

}
