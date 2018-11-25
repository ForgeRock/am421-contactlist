package com.forgerock.edu.contactlist.util;

import com.forgerock.edu.contactlist.entity.LdapId;
import com.forgerock.edu.contactlist.entity.LDAPEntry;
import java.util.ArrayList;
import java.util.List;
import org.forgerock.opendj.ldap.ModificationType;
import org.forgerock.opendj.ldap.requests.ModifyDNRequest;
import org.forgerock.opendj.ldap.requests.ModifyRequest;
import org.forgerock.opendj.ldap.requests.Request;
import org.forgerock.opendj.ldap.requests.Requests;

/**
 *
 * @author vrg
 */
public class LDAPRequestUtil {

    public static <LE extends LDAPEntry> boolean isRDNChanging(LE origEntry, LE changedEntry) {
        return !origEntry.getId().equals(changedEntry.getId());
    }

    public static <LE extends LDAPEntry> List<Request> createModifyRequests(LE origEntry, LE changedEntry) {
        List<Request> requests = new ArrayList<>();

        if (isRDNChanging(origEntry, changedEntry)) {
            ModifyDNRequest modifyDNRequest = Requests.newModifyDNRequest(origEntry.getId().getDN(), changedEntry.getId().getRDN());
            requests.add(modifyDNRequest);
            ModifyRequest replaceRDNAttribute = Requests.newModifyRequest(changedEntry.getId().getDN())
                    .addModification(ModificationType.REPLACE,
                            changedEntry.getId().getRDNAttributeName(),
                            changedEntry.getId().getRDNAttributeValue());
            requests.add(replaceRDNAttribute);
        }

        ModifyRequest modifyRequest = createModifyRequest(origEntry, changedEntry);

        if (!modifyRequest.getModifications().isEmpty()) {
            requests.add(modifyRequest);
        }

        return requests;
    }

    public static <LE extends LDAPEntry> ModifyRequest createModifyRequest(LE origEntry, LE changedEntry) {
        return Requests.newModifyRequest(origEntry.getLdapState(), changedEntry.getLdapState());
    }

    public static Request createDeleteRequest(LdapId id) {
        return Requests.newDeleteRequest(id.getDN());
    }

}
