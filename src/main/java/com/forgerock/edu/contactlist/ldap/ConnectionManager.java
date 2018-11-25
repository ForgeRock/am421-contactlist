package com.forgerock.edu.contactlist.ldap;

import java.util.HashSet;
import java.util.Set;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.ConnectionFactory;
import org.forgerock.opendj.ldap.ErrorResultException;

/**
 *
 * @author vrg
 */
public class ConnectionManager {

    private final ConnectionFactory connectionFactory;
    
    private final ThreadLocal<Connection> connectionOfThread;
    private final Set<Connection>managedConnections = new HashSet<>();
    private final ConnectionPreparer connectionPreparer;

    public ConnectionManager(ConnectionFactory connectionFactory, ConnectionPreparer connectionPreparer) {
        this.connectionFactory = connectionFactory;
        this.connectionOfThread = new ThreadLocal<>();
        this.connectionPreparer = connectionPreparer;
    }

    public Connection getConnection() throws ErrorResultException {
        Connection connection = connectionOfThread.get();
        if (connection == null || !connection.isValid()) {
            if (connection != null) {
                closeConnection(connection);
            } 
            connection = allocateConnection();
            connectionOfThread.set(connection);
        }
        return connection;
    }
    
    protected void closeConnection(Connection connection) {
        synchronized(managedConnections) {
            if (managedConnections.remove(connection)) {
                connection.close();
            }
        }
    }
    
    protected Connection allocateConnection() throws ErrorResultException {
        synchronized(managedConnections) {
            Connection newConnection = connectionFactory.getConnection();
            connectionPreparer.prepare(newConnection);
            managedConnections.add(newConnection);
            return newConnection;
        }
    }

    public void closeAll() {
        synchronized(managedConnections) {
            managedConnections.parallelStream().forEach((conn) -> {
                conn.close();
            });
            managedConnections.clear();
        }
    }
}
