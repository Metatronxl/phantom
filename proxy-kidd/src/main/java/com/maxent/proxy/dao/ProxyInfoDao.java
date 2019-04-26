package com.maxent.proxy.dao;

import com.maxent.proxy.detector.ProxyInfo;

import java.io.File;

/**
 * Created by kevin on 5/31/16.
 */
public interface ProxyInfoDao {

    public void insert(ProxyInfo proxyInfo);

    public void deleteByIPAndPort(String ip, int port);

    public void append(ProxyInfo proxyInfo, File distFile);
}
