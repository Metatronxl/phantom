package com.noisy.proxy.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by kevin on 5/25/16.
 */
public class ConfigUtils {

    private static Configuration config;


    static {
        try {
            config = new PropertiesConfiguration(System.getProperty("user.dir")+"/proxy-kidd/src/main/resources-env/dev/config.properties");
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
