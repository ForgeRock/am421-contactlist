package com.forgerock.edu.contactlist.entity;

/**
 *
 * @author vrg
 */
public class ContactId extends LdapId {

    private final ContactGroupId groupId;

    public ContactId(ContactGroupId groupId) {
        this.groupId = groupId;
    }

    public ContactId(ContactGroupId groupId, String contactId) {
        super(contactId);
        this.groupId = groupId;
    }

    @Override
    public String getRDNAttributeName() {
        return "uid";
    }

    @Override
    public ContactGroupId getParentId() {
        return groupId;
    }

    @Override
    public String getObjectClass() {
        return "contact";
    }

}
