package com.forgerock.edu.contactlist.uma;

import com.forgerock.edu.contactlist.entity.JSONEntity;
import com.forgerock.edu.contactlist.util.NullSafeJsonObjectBuilder;
import javax.json.JsonObject;

/**
 *
 * @author vrg
 */
public class PermissionTicket implements JSONEntity {

    private String ticket;

    public PermissionTicket() {
    }

    public String getTicket() {
        return ticket;
    }

    @Override
    public JsonObject getJsonObject() {
        JsonObject json = new NullSafeJsonObjectBuilder()
                .add("ticket", ticket)
                .build();
        return json;
    }

    @Override
    public void setJsonObject(JsonObject object) {
        ticket = object.getString("ticket");
    }

    @Override
    public String toString() {
        return getJsonObject().toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private PermissionTicket ticket = new PermissionTicket();

        public Builder jsonState(JsonObject jsonState) {
            ticket.setJsonObject(jsonState);
            return this;
        }

        public Builder ticket(String tokenId) {
            ticket.ticket = tokenId;
            return this;
        }
        public PermissionTicket build() {
            return ticket;
        }
    }

}
