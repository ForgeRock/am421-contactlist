package com.forgerock.edu.contactlist.entity;

import javax.json.JsonObject;

/**
 *
 * @author vrg
 */
public interface JSONEntity {
    public JsonObject getJsonObject();
    public void setJsonObject(JsonObject object);
}
