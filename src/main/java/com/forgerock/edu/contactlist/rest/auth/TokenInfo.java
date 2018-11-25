package com.forgerock.edu.contactlist.rest.auth;

import com.forgerock.edu.contactlist.rest.LocalDateTimeXmlAdapter;
import com.forgerock.edu.contactlist.rest.security.AuthToken;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * JAXB mapped entity that represents token info JSON object. Wraps {@link AuthToken}.
 *
 * @author vrg
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class TokenInfo {
    private AuthToken token;

    public TokenInfo(AuthToken token) {
        this.token = token;
    }

    public boolean isValid() {
        return token.isValid();
    }

    @XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
    public LocalDateTime getExpiresOn() {
        return token.getExpiresOn();
    }

    public User getUser() {
        return token.getUser();
    }

    public String getTokenId() {
        return token.getTokenId();
    }
    
    public Set<String> getPrivileges() {
        return token.getUser().getPrivileges();
    }
}
