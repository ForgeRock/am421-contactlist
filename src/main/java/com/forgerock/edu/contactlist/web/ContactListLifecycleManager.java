package com.forgerock.edu.contactlist.web;

import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactoryImpl;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 *
 * @author vrg
 */
public class ContactListLifecycleManager implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LDAPConnectionFactoryImpl.INSTANCE.shutdown();
    }
}
