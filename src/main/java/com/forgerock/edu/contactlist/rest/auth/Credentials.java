package com.forgerock.edu.contactlist.rest.auth;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB mapped entity class that represents credentials (username and password)
 * of a user. Mapped to a JSON object in {@link AuthResource#login(com.forgerock.edu.phonebook.rest.auth.Credentials) }.
 * 
 * @author vrg
 */
@XmlRootElement
public class Credentials {
    private String user;
    private String password;

    public Credentials() {
    }

    public Credentials(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Credentials{" + "user=" + user + ", password=" + password + '}';
    }
    
    
    
}
