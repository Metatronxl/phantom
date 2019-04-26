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
public class DetectPorts {
    private final static Configuration config = ConfigUtils.getConfig();
    private final Set<Integer> ports = new HashSet<>();

    private final static DetectPorts _Instance = new DetectPorts();

    private DetectPorts() {
        String detectPortsFilePath = config.getString("detection.ports.file");
        if (StringUtils.isEmpty(detectPortsFilePath)) {
            throw new IllegalArgumentException("The detecting port list file hasn't been configured.");
        }
        File filterIPListFile = FileUtils.getFile(detectPortsFilePath);
        if (filterIPListFile == null) {
            throw new IllegalArgumentException("Cannot load the detecting port list from file: " + detectPortsFilePath);
        }

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filterIPListFile), "utf-8"));
            List<String> detectPortList = IOUtils.readLines(br);
            if (detectPortList != null && !detectPortList.isEmpty()) {
                for (String port : detectPortList) {
                    ports.add(Integer.parseInt(port.trim()));
                }
                br.close();
            } else {
                br.close();
                throw new IllegalArgumentException(
                        "The detecting port list file is empty, file path: " + detectPortsFilePath);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "An exception occurred when loading detection port list from file: " + detectPortsFilePath);
        }
    }

    public static DetectPorts getInstance() {
        return _Instance;
    }

    public Set<Integer> getPorts() {
        return ports;
    }
}

