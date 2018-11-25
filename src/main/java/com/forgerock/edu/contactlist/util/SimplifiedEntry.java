package com.forgerock.edu.contactlist.util;

import org.forgerock.opendj.ldap.Attribute;
import org.forgerock.opendj.ldap.Entry;

/**
 *
 * @author vrg
 */
public class SimplifiedEntry {
    private final Entry entry;

    public SimplifiedEntry(Entry entry) {
        this.entry = entry;
    }
    
    public Attribute getAttribute(String name) {
        return entry.getAttribute(name);
    }
    
    public String getFirstValueOfAttributeAsString(String name) {
        Attribute attribute = entry.getAttribute(name);
        return attribute == null ? null : attribute.firstValueAsString();
    }
}
