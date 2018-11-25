package com.forgerock.edu.contactlist.rest.auth;

import com.forgerock.edu.contactlist.entity.JSONEntity;
import com.forgerock.edu.contactlist.entity.LDAPEntryBase;
import com.forgerock.edu.contactlist.entity.UserId;
import com.forgerock.edu.contactlist.util.EntryBuilder;
import com.forgerock.edu.contactlist.util.JsonUtil;
import com.forgerock.edu.contactlist.util.NullSafeJsonObjectBuilder;
import com.forgerock.edu.contactlist.util.SimplifiedEntry;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import javax.json.JsonObject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author vrg
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class User extends LDAPEntryBase implements JSONEntity {

    private String uid;
    @XmlTransient
    private String password;
    private String name;
    private String givenName;
    private String familyName;
    private String email;
    private Boolean active;
    @XmlTransient
    private Set<String> privileges = new TreeSet<>();

    public User() {
    }

    public User(UserId userId) {
        setId(userId);
    }

    public User(User user) {
        super(user);
        this.uid = user.uid;
        this.password = user.password;
        this.name = user.name;
        this.givenName = user.givenName;
        this.familyName = user.familyName;
        this.email = user.email;
        this.active = user.active;
        this.privileges = new HashSet<>(user.privileges);
    }

    @Override
    public UserId getId() {
        return (UserId) super.getId();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
        getId().setRdnAttributeValue(uid);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Set<String> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Set<String> privileges) {
        this.privileges = privileges;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    protected void buildEntry(EntryBuilder builder) {
        builder
                .addAttribute("uid", uid)
                .addAttribute("userPassword", password)
                .addAttribute("givenName", givenName)
                .addAttribute("sn", familyName)
                .addAttribute("cn", name)
                .addAttribute("mail", email)
                .addAttribute("inetuserstatus", active ? "Active" : "Inactive");
    }

    @Override
    protected void parseEntry(SimplifiedEntry entry) {
        this.uid = entry.getFirstValueOfAttributeAsString("uid");
        this.password = null;
        this.givenName = entry.getFirstValueOfAttributeAsString("givenName");
        this.familyName = entry.getFirstValueOfAttributeAsString("sn");
        this.name = entry.getFirstValueOfAttributeAsString("cn");
        this.email = entry.getFirstValueOfAttributeAsString("mail");
        String userStatus = entry.getFirstValueOfAttributeAsString("inetuserstatus");
        this.active = !"Inactive".equalsIgnoreCase(userStatus);
    }

    @Override
    public JsonObject getJsonObject() {
        return new NullSafeJsonObjectBuilder()
                .add("_id", getId().getRDNAttributeValue())
                .add("_rev", getRevision())
                .add("uid", uid)
                .add("active", active)
                .add("givenName", givenName)
                .add("familyName", familyName)
                .add("name", name)
                .add("email", email)
                .add("privileges", privileges.stream().collect(JsonUtil.toJsonArrayOfStrings()))
                .build();
    }
    
    public JsonObject getMinimalJsonObject() {
        return new NullSafeJsonObjectBuilder()
                .add("_id", getId().getRDNAttributeValue())
                .add("_rev", getRevision())
                .add("uid", uid)
                .add("active", active)
                .build();
    }

    @Override
    public void setJsonObject(JsonObject object) {
        getId().setRdnAttributeValue(object.getString("_id", UUID.randomUUID().toString()));
        this.uid = object.getString("_id", null);
        this.active = object.getBoolean("active", true);
        this.password = object.getString("password", null);
        this.givenName = object.getString("givenName", null);
        this.familyName = object.getString("familyName", null);
        this.name = object.getString("name", null);
        this.email = object.getString("email", null);
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    @Override
    public String toString() {
        return "User{" + "uid=" + uid + ", active=" + active + ", password=" + password + ", name=" + name + ", givenName=" + givenName + ", familyName=" + familyName + ", email=" + email + ", privileges=" + privileges + '}';
    }

    public boolean isPasswordMatching(String passwd) {
        return this.password.equals(passwd);
    }
}
