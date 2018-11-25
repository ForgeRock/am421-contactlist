package com.forgerock.edu.contactlist.dao;

import com.forgerock.edu.contactlist.entity.LdapId;
import com.forgerock.edu.contactlist.entity.LDAPEntry;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.util.IterableSearchResult;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.forgerock.opendj.ldap.DN;
import org.forgerock.opendj.ldap.Entry;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.Filter;
import org.forgerock.opendj.ldap.SearchScope;
import org.forgerock.opendj.ldap.requests.ModifyDNRequest;
import org.forgerock.opendj.ldap.requests.ModifyRequest;
import org.forgerock.opendj.ldap.requests.Request;
import org.forgerock.opendj.ldap.requests.Requests;
import org.forgerock.opendj.ldap.requests.SearchRequest;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;
import org.forgerock.opendj.ldif.ConnectionEntryReader;

/**
 *
 * @author vrg
 * @param <E>
 * @param <ParentID>
 * @param <ID>
 */
public abstract class GenericDAO<E extends LDAPEntry, ParentID extends LdapId, ID extends LdapId> {

    protected final LDAPConnectionFactory connectionFactory;
    protected final Class<E> type;

    public GenericDAO(LDAPConnectionFactory connectionFactory, Class<E> type) {
        this.connectionFactory = connectionFactory;
        this.type = type;
    }

    public abstract Filter getTypeFilter();

    public String getRevision(DN dn) throws ErrorResultException {
        return connectionFactory.getConnection()
                .readEntry(dn, "etag")
                .getAttribute("etag")
                .firstValueAsString();
    }

    public String getRevision(ID id) throws ErrorResultException {
        return getRevision(id.getDN());
    }

    public void delete(DN dn) throws ErrorResultException {
        connectionFactory.getConnection().delete(Requests.newDeleteRequest(dn));
    }

    public void delete(E entry) throws ErrorResultException {
        delete(entry.getId().getDN());
    }

    public void executeRequests(Iterable<? extends Request> requests) throws ErrorResultException {
        for (Request request : requests) {
            if (request instanceof ModifyRequest) {
                connectionFactory.getConnection().modify((ModifyRequest) request);
            } else if (request instanceof ModifyDNRequest) {
                connectionFactory.getConnection().modifyDN((ModifyDNRequest) request);
            } else {
                throw new IllegalArgumentException("Unknown request type: " + request);
            }
        }
    }

    public void checkWhetherExists(ID id) throws ErrorResultException {
        SearchRequest searchUserRequest = Requests.newSearchRequest(
                id.getDN(),
                SearchScope.BASE_OBJECT,
                getTypeFilter(),
                "dn");
        connectionFactory.getConnection().searchSingleEntry(searchUserRequest);
    }

    public Entry findEntryById(ID id) throws ErrorResultException {
        SearchRequest searchUserRequest = Requests.newSearchRequest(
                id.getDN(),
                SearchScope.BASE_OBJECT,
                getTypeFilter(),
                "*", "etag");
        return connectionFactory.getConnection().searchSingleEntry(searchUserRequest);
    }

    public E findById(ID id) throws ErrorResultException {
        try {
            findEntryById(id);
            E entity = type.newInstance();
            Entry entry = findEntryById(id);
            entity.setId(id);
            entity.setLdapState(entry);
            return entity;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Error instantiating entity: " + type, ex);
        }
    }

    public Stream<SearchResultEntry> findAllEntriesByParentId(ParentID parentID) {
        SearchRequest searchGroupsRequest = Requests.newSearchRequest(
                parentID.getDN(),
                SearchScope.SINGLE_LEVEL,
                getTypeFilter(),
                "*", "etag");
        ConnectionEntryReader reader = connectionFactory.getConnection().search(searchGroupsRequest);
        return StreamSupport.stream(new IterableSearchResult(reader).spliterator(), false);
    }

    public Stream<E> findAllByParentId(ParentID parentID) {
        return findAllEntriesByParentId(parentID)
                .map(entry -> mapToEntity(entry, parentID));
    }

    protected abstract E mapToEntity(SearchResultEntry entry, ParentID parentID);

}
