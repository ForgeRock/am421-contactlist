package com.forgerock.edu.contactlist.ldap;

import com.forgerock.edu.contactlist.entity.LdapId;
import org.forgerock.opendj.ldap.DN;
import org.forgerock.opendj.ldap.Filter;

/**
 *
 * @author vrg
 */
public class LDAPSettings {

    private String ldapHost = "localhost";
    private int ldapPort = 1389;
    private String ldapBindDN = "uid=admin";
    private char[] ldapBindPassword = "cangetinds".toCharArray();
    private DN peopleContainerDN = DN.valueOf("ou=people,dc=contactlist,dc=com");
    private DN groupsContainerDN = DN.valueOf("ou=groups,dc=contactlist,dc=com");
    private String userSearchAttribute = "uid";
    private Filter userSearchFilter = Filter.equality("objectClass", "inetOrgPerson");
    private LdapId peopleContainerId;

    private LDAPSettings() {
    }

    public String getLdapHost() {
        return ldapHost;
    }

    public void setLdapHost(String ldapHost) {
        this.ldapHost = ldapHost;
    }

    public int getLdapPort() {
        return ldapPort;
    }

    public void setLdapPort(int ldapPort) {
        this.ldapPort = ldapPort;
    }

    public String getLdapBindDN() {
        return ldapBindDN;
    }

    public void setLdapBindDN(String ldapBindDN) {
        this.ldapBindDN = ldapBindDN;
    }

    public char[] getLdapBindPassword() {
        return ldapBindPassword;
    }

    public void setLdapBindPasswordAsString(String ldapBindPassword) {
        this.ldapBindPassword = ldapBindPassword.toCharArray();
    }

    public DN getPeopleContainerDN() {
        return peopleContainerDN;
    }

    public void setPeopleContainerDNAsString(String peopleContainerDNAsString) {
        this.peopleContainerDN = DN.valueOf(peopleContainerDNAsString);
    }

    public DN getGroupsContainerDN() {
        return groupsContainerDN;
    }

    public void setGroupsContainerDNAsString(String groupsContainerDNAsString) {
        this.groupsContainerDN = DN.valueOf(groupsContainerDNAsString);
    }

    public String getUserSearchAttribute() {
        return userSearchAttribute;
    }

    public void setUserSearchAttribute(String userSearchAttribute) {
        this.userSearchAttribute = userSearchAttribute;
    }

    public Filter getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilterAsString(String userSearchFilterAsString) {
        this.userSearchFilter = Filter.valueOf(userSearchFilterAsString);
    }

    public LdapId getPeopleContainerId() {
        if (peopleContainerId == null) {
            peopleContainerId = new LdapId() {
                @Override
                public String getRDNAttributeName() {
                    return "ou";
                }

                @Override
                public String getRDNAttributeValue() {
                    return "people";
                }

                @Override
                public DN getDN() {
                    return getPeopleContainerDN();
                }

                @Override
                public LdapId getParentId() {
                    return null;
                }

                @Override
                public String getObjectClass() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
        return peopleContainerId;
    }

    private static class SingletonHolder {

        private static final LDAPSettings INSTANCE = new LDAPSettings();
    }

    public static LDAPSettings getInstance() {
        return SingletonHolder.INSTANCE;
    }

}
