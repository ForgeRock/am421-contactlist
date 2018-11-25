package com.forgerock.edu.contactlist.util;

import org.forgerock.opendj.ldap.DN;
import org.forgerock.opendj.ldap.Entry;
import org.forgerock.opendj.ldap.LinkedHashMapEntry;

/**
 *
 * @author vrg
 */
public class EntryBuilder {

    private final LinkedHashMapEntry entry;

    public EntryBuilder(LinkedHashMapEntry entry) {
        this.entry = entry;
    }

    public EntryBuilder(DN entryDN) {
        this.entry = new LinkedHashMapEntry(entryDN);
    }

    public EntryBuilder() {
        this.entry = new LinkedHashMapEntry();
    }

    public final EntryBuilder setDN(DN dn) {
        entry.setName(dn);
        return this;
    }

    public EntryBuilder addAttribute(String attributeDescription, Object... values) {
        if (!(values.length == 1 && values[0] == null)) {
            entry.addAttribute(attributeDescription, values);
        }
        return this;
    }

    public EntryBuilder removeAttribute(String attributeDescription, Object... values) {
        if (!(values.length == 1 && values[0] == null)) {
            entry.removeAttribute(attributeDescription, values);
        }
        return this;
    }

    public EntryBuilder replaceAttribute(String attributeDescription, Object... values) {
        if (!(values.length == 1 && values[0] == null)) {
            entry.replaceAttribute(attributeDescription, values);
        }
        return this;
    }

    public EntryBuilder setDN(String dn) {
        entry.setName(dn);
        return this;
    }

    public Entry build() {
        return entry;
    }

}
