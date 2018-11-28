package com.forgerock.edu.contactlist.uma;

import com.forgerock.edu.contactlist.entity.JSONEntity;
import com.forgerock.edu.contactlist.util.NullSafeJsonObjectBuilder;
import com.forgerock.edu.contactlist.util.StringUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import javax.json.JsonObject;

/**
 *
 * @author vrg
 */
public class AccessToken implements JSONEntity {

    private String tokenId;
    private String type;
    private Set<String> scopes = new TreeSet<>();
    private Set<String> unmodifiableScopes;
    private int expiresIn;

    public AccessToken() {
        createWrappers();
    }

    private void createWrappers() {
        unmodifiableScopes = Collections.unmodifiableSet(scopes);
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getType() {
        return type;
    }

    public Set<String> getScopes() {
        return unmodifiableScopes;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    @Override
    public JsonObject getJsonObject() {
        JsonObject json = new NullSafeJsonObjectBuilder()
                .add("access_token", tokenId)
                .add("token_type", type)
                .add("scope", StringUtil.join(scopes, " "))
                .add("expires_in", expiresIn)
                .build();
        return json;
    }

    @Override
    public void setJsonObject(JsonObject object) {
        tokenId = object.getString("access_token");
        type = object.getString("token_type");
        expiresIn = object.getInt("expires_in", 0);
        if(object.getJsonString("scope") != null) {
            String scopeString = object.getString("scope");
            scopes = new TreeSet<>(Arrays.asList(scopeString.split("\\s+")));
        }        
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

        private AccessToken rs = new AccessToken();

        public Builder jsonState(JsonObject jsonState) {
            rs.setJsonObject(jsonState);
            return this;
        }

        public Builder tokenId(String tokenId) {
            rs.tokenId = tokenId;
            return this;
        }

        public Builder type(String type) {
            rs.type = type;
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

        public AccessToken build() {
            return rs;
        }
    }

}
