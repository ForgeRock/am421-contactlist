package com.forgerock.edu.contactlist.entity;

import org.forgerock.opendj.ldap.DN;
import org.forgerock.opendj.ldap.Filter;
import org.forgerock.opendj.ldap.RDN;

/**
 *
 * @author vrg
 */
public abstract class LdapId {

    private DN cachedDN;
    private RDN cachedRDN;
    private String rdnAttributeValue;

    public LdapId() {
    }

    public LdapId(String rdnAttributeValue) {
        this.rdnAttributeValue = rdnAttributeValue;
    }

    protected void clearCache() {
        cachedDN = null;
        cachedRDN = null;
    }

    public final RDN getRDN() {
        if (cachedRDN == null) {
            cachedRDN = RDN.valueOf(getRDNAttributeName() + "=" + getRDNAttributeValue());
        }
        return cachedRDN;
    }

    public abstract String getRDNAttributeName();

    public String getRDNAttributeValue() {
        return this.rdnAttributeValue;
    }

    public void setRdnAttributeValue(String rdnAttributeValue) {
        this.rdnAttributeValue = rdnAttributeValue;
    }

    public DN getDN() {
        if (cachedDN == null) {
            cachedDN = DN.valueOf(getRDN() + "," + getParentId().getDN());
        }
        return cachedDN;
    }

    public abstract LdapId getParentId();

    public void setDN(DN dn) {
        cachedDN = dn;
        cachedRDN = dn.iterator().next();
        rdnAttributeValue = cachedRDN.getFirstAVA().getAttributeValue().toString();
    }

    public Filter getTypeFilter() {
        return Filter.equality("objectClass", getObjectClass());
    }

    public abstract String getObjectClass();

    public Object[] getAllObjectClasses() {
        return new Object[]{getObjectClass()};
    }
}
