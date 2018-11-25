package com.forgerock.edu.contactlist.dao;

import com.forgerock.edu.contactlist.entity.Contact;
import com.forgerock.edu.contactlist.entity.ContactGroupId;
import com.forgerock.edu.contactlist.entity.ContactId;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.Filter;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;

/**
 *
 * @author vrg
 */
public class ContactDAO extends GenericDAO<Contact, ContactGroupId, ContactId> {

    /**
     *
     */
    public final static Filter TYPE_FILTER = new ContactId(null).getTypeFilter();

    public ContactDAO(LDAPConnectionFactory connectionFactory) {
        super(connectionFactory, Contact.class);
    }

    @Override
    public Filter getTypeFilter() {
        return TYPE_FILTER;
    }

    @Override
    protected Contact mapToEntity(SearchResultEntry entry, ContactGroupId parentId) {
        Contact contact = new Contact(new ContactId(parentId));
        contact.setLdapState(entry);
        return contact;
    }

}
