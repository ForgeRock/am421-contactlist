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
    private String resourceOwnerPARefreshToken;
    private String resourceOwnerPAToken;

    public ContactGroup() {
    }
    
    public ContactGroup(ContactGroupId id) {
        setId(id);
    }

    public ContactGroup(ContactGroup group) {
        super(group);
        this.displayName = group.displayName;
        this.resourceSetId = group.resourceSetId;
        this.resourceOwnerPARefreshToken = group.resourceOwnerPARefreshToken;
        this.resourceOwnerPAToken = group.resourceOwnerPAToken;
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

    /**
     * Returns the assigned UMA resource_set_id.
     * @return the assigned UMA resource_set_id
     */
    public String getResourceSetId() {
        return resourceSetId;
    }

    /**
     * Assigns the UMA resource_set_id to this contact group.
     * @param resourceSetId 
     */
    public void setResourceSetId(String resourceSetId) {
        this.resourceSetId = resourceSetId;
    }

    public String getResourceOwnerPARefreshToken() {
        return resourceOwnerPARefreshToken;
    }

    public void setResourceOwnerPARefreshToken(String resourceOwnerPARefreshToken) {
        this.resourceOwnerPARefreshToken = resourceOwnerPARefreshToken;
    }

    public String getResourceOwnerPAToken() {
        return resourceOwnerPAToken;
    }

    public void setResourceOwnerPAToken(String resourceOwnerPAToken) {
        this.resourceOwnerPAToken = resourceOwnerPAToken;
    }

    @Override
    protected void parseEntry(SimplifiedEntry entry) {
        this.displayName = entry.getFirstValueOfAttributeAsString("displayName");
        this.resourceSetId = entry.getFirstValueOfAttributeAsString("umaLabelResourceSet");
        this.resourceOwnerPARefreshToken = entry.getFirstValueOfAttributeAsString("umaResourceOwnerPARefreshToken");
        this.resourceOwnerPAToken = entry.getFirstValueOfAttributeAsString("umaResourceOwnerPAToken");
    }

    @Override
    protected void buildEntry(EntryBuilder builder) {
        builder
                .addAttribute("displayName", displayName)
                //The UMA resource_set_id is stored in an LDAP attribute called "umaLabelResourceSet"
                .addAttribute("umaLabelResourceSet", resourceSetId)
                .addAttribute("umaResourceOwnerPARefreshToken", resourceOwnerPARefreshToken)
                .addAttribute("umaResourceOwnerPAToken", resourceOwnerPAToken);
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
