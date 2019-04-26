package com.maxent.proxy.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kevin on 6/1/16.
 */
public class IPFilterUtils {
    private final static Configuration config = ConfigUtils.getConfig();
    private final static Set<String> _FilterIPList = new HashSet<>();
    private final static long longMulticastIP = IPSegment.ipToLong("224.0.0.0");
    private final static long classAPrivateIPStart = IPSegment.ipToLong("10.0.0.0");
    private final static long classAPrivateIPEnd = IPSegment.ipToLong("10.255.255.255");
    private final static long classBPrivateIPStart = IPSegment.ipToLong("172.16.0.0");
    private final static long classBPrivateIPEnd = IPSegment.ipToLong("172.31.255.255");
    private final static long classCPrivateIPStart = IPSegment.ipToLong("192.168.0.0");
    private final static long classCPrivateIPEnd = IPSegment.ipToLong("192.168.255.255");

    private final static IPFilterUtils _Instance = new IPFilterUtils();

    private IPFilterUtils() {
        String filterIPListFilePath = config.getString("filter.ip.list.file");
        if (StringUtils.isEmpty(filterIPListFilePath)) {
            throw new IllegalArgumentException("The filter IP list file hasn't been configured.");
        }
        File filterIPListFile = FileUtils.getFile(filterIPListFilePath);
        if (filterIPListFile == null) {
            throw new IllegalArgumentException("Cannot load the filter IP list from file: " + filterIPListFilePath);
        }

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filterIPListFile), "utf-8"));
            List<String> filterIPList = IOUtils.readLines(br);
            if (filterIPList != null && !filterIPList.isEmpty()) {
                for (String filterIP : filterIPList) {
                    _FilterIPList.add(filterIP.trim());
                }
                br.close();
            } else {
                br.close();
                throw new IllegalArgumentException(
                        "The filter IP list file is empty, file path: " + filterIPListFilePath);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "An exception occurred when loading filter IP list from file: " + filterIPListFilePath);
        }
    }

    public static IPFilterUtils getInstance() {
        return _Instance;
    }

    public boolean needFilter(final String ip) {
        if (_FilterIPList.contains(ip)) {
            return true;
        }

        return isPrivateIPOrMulticastIP(ip);
    }

    public boolean isPrivateIPOrMulticastIP(final String ip) {
        long longIp = IPSegment.ipToLong(ip);
        if (longIp >= longMulticastIP) {
            return true;
        }

        if ((longIp >= classAPrivateIPStart && longIp <= classAPrivateIPEnd)
                || (longIp >= classBPrivateIPStart && longIp <= classBPrivateIPEnd)
                || (longIp >= classCPrivateIPStart && longIp <= classCPrivateIPEnd)) {
            return true;
        }

        return false;
    }

}

