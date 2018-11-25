package com.forgerock.edu.contactlist.dao;

import com.forgerock.edu.contactlist.entity.ContactGroup;
import com.forgerock.edu.contactlist.entity.ContactGroupId;
import com.forgerock.edu.contactlist.entity.UserId;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import org.forgerock.opendj.ldap.Filter;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;

/**
 *
 * @author vrg
 */
public class ContactGroupDAO extends GenericDAO<ContactGroup, UserId, ContactGroupId> {

    /**
     *
     */
    public final static Filter TYPE_FILTER = new ContactGroupId(null).getTypeFilter();

    public ContactGroupDAO(LDAPConnectionFactory connectionFactory) {
        super(connectionFactory, ContactGroup.class);
    }

    @Override
    public Filter getTypeFilter() {
        return TYPE_FILTER;
    }

    @Override
    protected ContactGroup mapToEntity(SearchResultEntry entry, UserId parentId) {
        ContactGroup group = new ContactGroup(new ContactGroupId(parentId));
        group.setLdapState(entry);
        return group;
    }

}
