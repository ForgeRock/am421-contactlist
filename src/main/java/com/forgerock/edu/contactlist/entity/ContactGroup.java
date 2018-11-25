package com.forgerock.edu.contactlist.entity;

import com.forgerock.edu.contactlist.util.EntryBuilder;
import com.forgerock.edu.contactlist.util.SimplifiedEntry;
import com.forgerock.edu.contactlist.util.NullSafeJsonObjectBuilder;
import java.util.UUID;
import javax.json.JsonObject;
import org.forgerock.opendj.ldap.Filter;

/**
 *
 * @author vrg
 */
public class ContactGroup extends LDAPEntryBase implements JSONEntity {

    private final static Filter OBJECT_CLASS_FILTER = Filter.equality("objectClass", "contactgroup");

    private String displayName;
    private String resourceSetId;

    public ContactGroup() {
    }
    
    public ContactGroup(ContactGroupId id) {
        setId(id);
    }

    public ContactGroup(ContactGroup group) {
        super(group);
        this.displayName = group.displayName;
        this.resourceSetId = group.resourceSetId;
    }

    @Override
    public ContactGroupId getId() {
        return (ContactGroupId) super.getId();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getResourceSetId() {
        return resourceSetId;
    }

    public void setResourceSetId(String resourceSetId) {
        this.resourceSetId = resourceSetId;
    }

    @Override
    protected void parseEntry(SimplifiedEntry entry) {
        this.displayName = entry.getFirstValueOfAttributeAsString("displayName");
        this.resourceSetId = entry.getFirstValueOfAttributeAsString("umaLabelResourceSet");
    }

    @Override
    protected void buildEntry(EntryBuilder builder) {
        builder
                .addAttribute("displayName", displayName)
                .addAttribute("umaLabelResourceSet", resourceSetId);
    }

    @Override
    public JsonObject getJsonObject() {
        return new NullSafeJsonObjectBuilder()
                .add("displayName", displayName)
                .add("_id", getId().getRDNAttributeValue())
                .add("_rev", revision)
                .add("resourceSetId", resourceSetId)
                .add("owner", getId().getParentId().getRDNAttributeValue())
                .build();
    }

    @Override
    public void setJsonObject(JsonObject object) {
        getId().setRdnAttributeValue(object.getString("_id", UUID.randomUUID().toString()));
        this.displayName = object.getString("displayName", null);
        this.revision = object.getString("_rev", null);
    }

    public static Filter typeFilter() {
        return OBJECT_CLASS_FILTER;
    }

}
