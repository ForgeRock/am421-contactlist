package com.forgerock.edu.contactlist.entity;

import com.forgerock.edu.contactlist.util.EntryBuilder;
import com.forgerock.edu.contactlist.util.SimplifiedEntry;
import com.forgerock.edu.contactlist.util.NullSafeJsonObjectBuilder;
import java.util.UUID;
import javax.json.JsonObject;

/**
 *
 * @author vrg
 */
public class Contact extends LDAPEntryBase implements JSONEntity {

    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String mobile;
    private String homePhone;
    private String workPhone;

    public Contact() {
    }
    
    public Contact(ContactId contactId) {
        setId(contactId);
    }

    public Contact(Contact contact) {
        super(contact);
        this.email = contact.email;
        this.firstName = contact.firstName;
        this.lastName = contact.lastName;
        this.fullName = contact.fullName;
        this.mobile = contact.mobile;
        this.homePhone = contact.homePhone;
        this.workPhone = contact.workPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    @Override
    protected void buildEntry(EntryBuilder builder) {
        builder
                .addAttribute("givenName", firstName)
                .addAttribute("sn", lastName)
                .addAttribute("cn", fullName)
                .addAttribute("mail", email)
                .addAttribute("mobile", mobile)
                .addAttribute("telephoneNumber", workPhone)
                .addAttribute("homePhone", homePhone);
    }

    @Override
    protected void parseEntry(SimplifiedEntry entry) {
        this.firstName = entry.getFirstValueOfAttributeAsString("givenName");
        this.lastName = entry.getFirstValueOfAttributeAsString("sn");
        this.fullName = entry.getFirstValueOfAttributeAsString("cn");
        this.email = entry.getFirstValueOfAttributeAsString("mail");
        this.mobile = entry.getFirstValueOfAttributeAsString("mobile");
        this.workPhone = entry.getFirstValueOfAttributeAsString("telephoneNumber");
        this.homePhone = entry.getFirstValueOfAttributeAsString("homePhone");
        
    }

    @Override
    public JsonObject getJsonObject() {
        return new NullSafeJsonObjectBuilder()
                .add("_id", getId().getRDNAttributeValue())
                .add("_rev", getRevision())
                .add("firstName", firstName)
                .add("lastName", lastName)
                .add("fullName", fullName)
                .add("email", email)
                .add("mobile", mobile)
                .add("homePhone", homePhone)
                .add("workPhone", workPhone)
                .build();
    }

    @Override
    public void setJsonObject(JsonObject object) {
        getId().setRdnAttributeValue(object.getString("_id", UUID.randomUUID().toString()));
        this.firstName = object.getString("firstName", null);
        this.lastName = object.getString("lastName", null);
        this.fullName = object.getString("fullName", null);
        this.email = object.getString("email", null);
        this.mobile = object.getString("mobile", null);
        this.homePhone = object.getString("homePhone", null);
        this.workPhone = object.getString("workPhone", null);
    }

}
