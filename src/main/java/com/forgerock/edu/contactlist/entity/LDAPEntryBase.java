package com.forgerock.edu.contactlist.entity;

import com.forgerock.edu.contactlist.util.EntryBuilder;
import com.forgerock.edu.contactlist.util.SimplifiedEntry;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.forgerock.opendj.ldap.Entry;

/**
 *
 * @author vrg
 */
@XmlType
public abstract class LDAPEntryBase implements LDAPEntry {

    @XmlTransient
    protected LdapId id;
    protected String revision;

    public LDAPEntryBase() {
    }

    public LDAPEntryBase(LDAPEntryBase other) {
        this.id = other.id;
        this.revision = other.revision;
    }

    @Override
    public LdapId getId() {
        return id;
    }

    @Override
    public final void setId(LdapId id) {
        this.id = id;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    @Override
    public void setRevision(String revision) {
        this.revision = revision;
    }
    
    protected abstract void buildEntry(EntryBuilder builder);
    protected abstract void parseEntry(SimplifiedEntry entry);
    @Override
    public Entry getLdapState() {
         EntryBuilder builder = new EntryBuilder()
                .setDN(id.getDN())
                .addAttribute("objectClass", id.getAllObjectClasses())
                .addAttribute(id.getRDNAttributeName(), id.getRDNAttributeValue());
         
         buildEntry(builder);
         return builder.build();
    }

    @Override
    public void setLdapState(Entry entry) {
        getId().setDN(entry.getName());
        SimplifiedEntry e = new SimplifiedEntry(entry);
        this.revision = e.getFirstValueOfAttributeAsString("etag");
        parseEntry(e);
    }

    
}
