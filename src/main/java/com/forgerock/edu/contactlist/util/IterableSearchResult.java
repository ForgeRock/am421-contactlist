package com.forgerock.edu.contactlist.util;

import java.io.Closeable;
import java.util.Iterator;
import org.forgerock.opendj.ldap.ErrorResultIOException;
import org.forgerock.opendj.ldap.SearchResultReferenceIOException;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;
import org.forgerock.opendj.ldif.ConnectionEntryReader;

/**
 *
 * @author vrg
 */
public class IterableSearchResult implements Iterable<SearchResultEntry>, Closeable{

    private final ConnectionEntryReader reader;

    public IterableSearchResult(ConnectionEntryReader reader) {
        this.reader = reader;
    }
    
    @Override
    public Iterator<SearchResultEntry> iterator() {
        
        return new Iterator<SearchResultEntry>() {
            @Override
            public boolean hasNext() {
                try {
                    return reader.hasNext();
                } catch (ErrorResultIOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public SearchResultEntry next() {
                try {
                    return reader.readEntry();
                } catch (ErrorResultIOException | SearchResultReferenceIOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    @Override
    public void close() {
        reader.close();
    }
}
