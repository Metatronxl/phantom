package com.noisy.proxy.dao;

import com.noisy.proxy.detector.ProxyInfo;

/**
 * Created by kevin on 5/31/16.
 */
public class ProxyInfoDaoCassandraImpl extends AbstractProxyInfoDaoImpl {
    private CassandraConnection connection = CassandraConnection.getInstance();

    @Override
    public void insert(ProxyInfo proxyInfo) {
        StringBuilder cql = new StringBuilder(
                "INSERT INTO proxy (ip,port,protocol,type,location,update_time) VALUES (");
        cql.append("'" + proxyInfo.getIp() + "'");
        cql.append(",");
        cql.append(proxyInfo.getPort());
        cql.append(",");
        cql.append(proxyInfo.getProtocol());
        cql.append(",");
        cql.append(proxyInfo.getType());
        cql.append(",");
        cql.append("'" + proxyInfo.getLocation() + "'");
        cql.append(",");
        cql.append(proxyInfo.getUpdateTime());
        cql.append(");");

        connection.getSession().execute(cql.toString());
    }

    @Override
    public void deleteByIPAndPort(String ip, int port) {
        StringBuilder cql = new StringBuilder("DELETE FROM proxy WHERE ip=");
        cql.append("'" + ip + "'");
        cql.append(" AND port=" + port);
        cql.append(";");

        connection.getSession().execute(cql.toString());
    }
}
