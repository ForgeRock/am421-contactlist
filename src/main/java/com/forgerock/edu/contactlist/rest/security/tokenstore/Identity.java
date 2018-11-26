package com.forgerock.edu.contactlist.rest.security.tokenstore;

import java.util.Objects;

/**
 *
 * @author vrg
 */
public class Identity {

    public final static Identity NONE = new Identity(Type.NONE, null, null);

    public enum Type {
        NONE,
        USER,
        OAUTH2_CLIENT
    }

    private Type type;
    private String userId;
    private String realm;

    private Identity() {
    }

    private Identity(Type type, String userId, String realm) {
        this.type = type;
        this.userId = userId;
        this.realm = realm;
    }

    public Type getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getRealm() {
        return realm;
    }

    @Override
    public String toString() {
        return "Identity{" + "type=" + type + ", userId=" + userId + ", realm=" + realm + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.type);
        hash = 67 * hash + Objects.hashCode(this.userId);
        hash = 67 * hash + Objects.hashCode(this.realm);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Identity other = (Identity) obj;
        if (!Objects.equals(this.userId, other.userId)) {
            return false;
        }
        if (!Objects.equals(this.realm, other.realm)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Identity identity = new Identity();

        public Builder user() {
            identity.type = Type.USER;
            return this;
        }

        public Builder oauth2Client() {
            identity.type = Type.OAUTH2_CLIENT;
            return this;
        }

        public Builder userId(String userId) {
            identity.userId = userId;
            return this;
        }

        public Builder realm(String realm) {
            identity.realm = realm;
            return this;
        }

        public Identity build() {
            if (identity.userId == null) {
                throw new IllegalStateException("Identity's userId MUST be set.");
            }
            if (identity.realm == null) {
                throw new IllegalStateException("Identity's realm MUST be set.");
            }
            return identity;
        }
    }

}
