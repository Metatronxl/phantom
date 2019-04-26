package com.maxent.proxy.dao;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.maxent.proxy.util.ConfigUtils;
import org.apache.commons.configuration.Configuration;

/**
 * Created by kevin on 5/31/16.
 */
public class CassandraConnection {
    private final static Configuration config = ConfigUtils.getConfig();
    private static CassandraConnection _instance = new CassandraConnection();
    private Cluster cluster;
    private Session session;

    public static CassandraConnection getInstance() {
        if (_instance == null) {
            _instance = new CassandraConnection();
        }
        return _instance;
    }

    /**
     * Creating Cassandra connection using Datastax API
     */
    private CassandraConnection() {
        try {
            cluster = Cluster.builder().addContactPoint(config.getString("db.cassandra.host")).build();
            session = cluster.connect(config.getString("db.cassandra.keyspace"));
        } catch (NoHostAvailableException e) {
            throw new RuntimeException(e);
        }
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Session getSession() {
        return session;
    }

    public void close() {
        session.close();
        cluster.close();
    }
}
