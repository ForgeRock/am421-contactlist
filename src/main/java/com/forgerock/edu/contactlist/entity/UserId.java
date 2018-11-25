package com.forgerock.edu.contactlist.entity;

import com.forgerock.edu.contactlist.ldap.LDAPSettings;
import org.forgerock.opendj.ldap.Filter;

/**
 *
 * @author vrg
 */
public class UserId extends LdapId {

    private final static LDAPSettings SETTINGS = LDAPSettings.getInstance();

    public static Filter typeFilter() {
        return SETTINGS.getUserSearchFilter();
    }

    public UserId(String ownerId) {
        super(ownerId);
    }

    @Override
    public String getRDNAttributeName() {
        return SETTINGS.getUserSearchAttribute();
    }

    @Override
    public LdapId getParentId() {
        return SETTINGS.getPeopleContainerId();
    }

    @Override
    public String getObjectClass() {
        return "inetOrgPerson";
    }

    @Override
    public Object[] getAllObjectClasses() {
        return new Object[]{"inetOrgPerson", "inetuser"};
    }

}
