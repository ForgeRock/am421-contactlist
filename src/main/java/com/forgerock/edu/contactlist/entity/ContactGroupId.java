package com.forgerock.edu.contactlist.entity;

/**
 *
 * @author vrg
 */
public class ContactGroupId extends LdapId {

    private final UserId owner;

    public ContactGroupId(UserId owner) {
        this.owner = owner;
    }

    public ContactGroupId(UserId owner, String groupId) {
        super(groupId);
        this.owner = owner;
    }

    @Override
    public String getRDNAttributeName() {
        return "cn";
    }

    @Override
    public UserId getParentId() {
        return owner;
    }

    @Override
    public String getObjectClass() {
        return "contactgroup";
    }
}
