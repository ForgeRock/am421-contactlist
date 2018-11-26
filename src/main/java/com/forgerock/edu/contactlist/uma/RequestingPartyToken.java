package com.forgerock.edu.contactlist.uma;

import com.forgerock.edu.contactlist.entity.JSONEntity;
import com.forgerock.edu.contactlist.util.NullSafeJsonObjectBuilder;
import javax.json.JsonObject;

/**
 *
 * @author vrg
 */
public class RequestingPartyToken implements JSONEntity {

    private String rpt;

    public RequestingPartyToken() {
    }

    public String getRpt() {
        return rpt;
    }

    @Override
    public JsonObject getJsonObject() {
        JsonObject json = new NullSafeJsonObjectBuilder()
                .add("rpt", rpt)
                .build();
        return json;
    }

    @Override
    public void setJsonObject(JsonObject object) {
        rpt = object.getString("rpt");
    }

    @Override
    public String toString() {
        return getJsonObject().toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private RequestingPartyToken ticket = new RequestingPartyToken();

        public Builder jsonState(JsonObject jsonState) {
            ticket.setJsonObject(jsonState);
            return this;
        }

        public Builder rpt(String tokenId) {
            ticket.rpt = tokenId;
            return this;
        }
        public RequestingPartyToken build() {
            return ticket;
        }
    }

}
