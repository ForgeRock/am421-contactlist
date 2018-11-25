package com.forgerock.edu.contactlist.dao;

import com.forgerock.edu.contactlist.ldap.LDAPSettings;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactoryImpl;
import com.forgerock.edu.contactlist.entity.LdapId;
import com.forgerock.edu.contactlist.entity.UserId;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.auth.User;
import com.forgerock.edu.contactlist.rest.security.filter.LocalTokenStoreSessionValidatorFilter;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.forgerock.opendj.ldap.AuthenticationException;
import org.forgerock.opendj.ldap.DN;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.Filter;
import org.forgerock.opendj.ldap.ModificationType;
import org.forgerock.opendj.ldap.ResultCode;
import org.forgerock.opendj.ldap.requests.CompareRequest;
import org.forgerock.opendj.ldap.requests.ModifyRequest;
import org.forgerock.opendj.ldap.requests.Requests;
import org.forgerock.opendj.ldap.responses.BindResult;
import org.forgerock.opendj.ldap.responses.CompareResult;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;

/**
 *
 * @author vrg
 */
public class UserDAO extends GenericDAO<User, LdapId, UserId> {

    public final static Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private final DN USER_ADMIN = LDAPSettings.getInstance().getGroupsContainerDN().child("cn", "User Admin");
    private final DN CONTACT_ADMIN = LDAPSettings.getInstance().getGroupsContainerDN().child("cn", "Contact Admin");
    private final DN CONTACT_READER = LDAPSettings.getInstance().getGroupsContainerDN().child("cn", "Contact Reader");

    public UserDAO(LDAPConnectionFactory connectionFactory) {
        super(connectionFactory, User.class);
    }

    public Stream<User> findAll() {
        return findAllByParentId(LDAPSettings.getInstance().getPeopleContainerId());
    }

    @Override
    protected User mapToEntity(SearchResultEntry entry, LdapId parentID) {
        User user = new User(new UserId(null));
        user.setLdapState(entry);
        return user;
    }

    @Override
    public Filter getTypeFilter() {
        return LDAPSettings.getInstance().getUserSearchFilter();
    }

    public boolean isPasswordMatching(String uid, String passwd) {
        try {
            UserId userId = new UserId(uid);
            BindResult result = connectionFactory.getAnonymousConnection().bind(userId.getDN().toString(), passwd.toCharArray());
            return result.isSuccess();
        } catch (AuthenticationException ex) {
            return false;
        } catch (ErrorResultException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public User findByIdAndAddPrivileges(UserId id) throws ErrorResultException {
        User user = super.findById(id);
        Set<String> privileges = calculatePrivileges(id);
        user.setPrivileges(privileges);
        return user;
    }

    public Set<String> calculatePrivileges(UserId id) throws ErrorResultException {
        Set<String> privileges = new TreeSet<>();
        if (isUserMemberOfGroup(USER_ADMIN, id.getDN())) {
            privileges.add("users/all");
            privileges.add("users/read");
            privileges.add("users/create");
            privileges.add("users/modify");
            privileges.add("users/delete");
        }

        if (isUserMemberOfGroup(CONTACT_ADMIN, id.getDN())) {
            privileges.add("contact-groups/all");
            privileges.add("contact-groups/read");
            privileges.add("contact-groups/create");
            privileges.add("contact-groups/modify");
            privileges.add("contact-groups/delete");
            privileges.add("contacts/all");
            privileges.add("contacts/read");
            privileges.add("contacts/create");
            privileges.add("contacts/modify");
            privileges.add("contacts/delete");
        } else if (isUserMemberOfGroup(CONTACT_READER, id.getDN())) {
            privileges.add("contact-groups/read");
            privileges.add("contacts/read");
        }
        return privileges;
    }

    public boolean updateGroupMembershipsBasedOnPrivilegeSet(User user) throws ErrorResultException {
        DN userDN = user.getId().getDN();
        Set<String> privileges = user.getPrivileges();
        boolean isContentAdmin = false;
        boolean isContentReader = false;
        boolean isUserAdmin = false;
        boolean wasChange = false;
        if (privileges.contains("contact-groups/all")) {
            isContentAdmin = true;
        } else if (privileges.contains("contact-groups/read")) {
            isContentReader = true;
        }
        if (privileges.contains("users/all")) {
            isUserAdmin = true;
        }

        wasChange |= updateGroupMembership(CONTACT_ADMIN, userDN, isContentAdmin);
        wasChange |= updateGroupMembership(CONTACT_READER, userDN, isContentReader);
        wasChange |= updateGroupMembership(USER_ADMIN, userDN, isUserAdmin);

        return wasChange;
    }

    public boolean isUserMemberOfGroup(DN groupDN, DN userDN) throws ErrorResultException {
        try {
            CompareRequest cr = Requests.newCompareRequest(groupDN.toString(), "member", userDN.toString());
            CompareResult result = connectionFactory.getConnection().compare(cr);
            return result.getResultCode() == ResultCode.COMPARE_TRUE;
        } catch (ErrorResultException ex) {
            LOGGER.log(Level.WARNING, "Error during checking group membership: groupDN: {0}", groupDN);
            return false;
        }
    }

    public boolean updateGroupMembership(DN groupDN, DN userDN, boolean shouldBeAMember) throws ErrorResultException {
        boolean currentlyMember = isUserMemberOfGroup(groupDN, userDN);
        if (currentlyMember != shouldBeAMember) {
            ModifyRequest req;
            if (shouldBeAMember) {
                LOGGER.log(Level.INFO, "Adding member to group {0}, new member: {1}", new Object[]{groupDN, userDN});
                req = Requests.newModifyRequest(groupDN).addModification(ModificationType.ADD, "member", userDN.toString());
            } else {
                LOGGER.log(Level.INFO, "Removing member from group {0}, old member: {1}", new Object[]{groupDN, userDN});
                req = Requests.newModifyRequest(groupDN).addModification(ModificationType.DELETE, "member", userDN.toString());
            }
            connectionFactory.getConnection().modify(req);
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws ErrorResultException {
        UserDAO userDAO = new UserDAO(LDAPConnectionFactoryImpl.INSTANCE);
        boolean passwordMatching = userDAO.isPasswordMatching("vrg", "cangetin");
        User john = userDAO.findByIdAndAddPrivileges(new UserId("john"));
        System.out.println("john:" + john);
        System.out.println("passwd matching: " + passwordMatching);
//        User user = User.builder().uid("peter3").active(true).email("peter@acme.com").name("Peter Family").familyName("Family").givenName("Peter").password("cangetin").build();
//        userDAO.connectionFactory.getConnection().add(user.getLdapState());
        LDAPConnectionFactoryImpl.INSTANCE.shutdown();
    }

    public void createUserByUID(String ownerId) throws ErrorResultException {
        UserId userId = new UserId(ownerId);
        User user = new User(userId);
        user.setName(ownerId);
        user.setFamilyName(ownerId);
        user.setGivenName(ownerId);
        user.setActive(Boolean.TRUE);

        connectionFactory.getConnection().add(user.getLdapState());
    }
}
