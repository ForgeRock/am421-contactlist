package com.forgerock.edu.contactlist.entity;

import org.forgerock.opendj.ldap.Entry;

/**
 *
 * @author vrg
 */
public interface LDAPEntry {
    public LdapId getId();
    public void setId(LdapId id);
    public Entry getLdapState();
    public void setLdapState(Entry entry);

    String getRevision();

    void setRevision(String revision);
}
