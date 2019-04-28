package com.noisy.proxy.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by kevin on 5/25/16.
 */
public class ConfigUtils {

    private static Configuration config;

    static {
        try {
            config = new PropertiesConfiguration("proxy-kidd/conf/config.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    private ConfigUtils() {
    }

    public static Configuration getConfig() {
        return config;
    }
}
