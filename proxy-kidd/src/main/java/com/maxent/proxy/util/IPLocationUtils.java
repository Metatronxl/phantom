package com.maxent.proxy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

/**
 * Created by kevin on 5/27/16.
 */
public class IPLocationUtils {
    private final static Logger log = LoggerFactory.getLogger(IPLocationUtils.class);
    private final static Resty restClient = new Resty();
    private final static String geoURL = ConfigUtils.getConfig().getString("geo.service.url");

    public static String getLocation(final String ip) {
        String location = null;
        String url = geoURL + ip;
        try {
            JSONResource response = restClient.json(url);
            location = constructLocation(response);
        } catch (Exception e) {
            log.warn("Failed to retrieve the IP location from GEO service: {}", url);
        }

        return location;
    }

    private static String constructLocation(JSONResource resource) throws Exception {
        if (resource == null) {
            return null;
        }
        String country = resource.get("country").toString();
        if (country == null) {
            country = "";
        }
        String province = resource.get("province").toString();
        if (province == null) {
            province = "";
        }
        String city = resource.get("city").toString();

        StringBuilder location = new StringBuilder(country);
        if (!country.equals(province)) {
            location.append(province);
        }
        if (!province.equals(city)) {
            location.append(city);
        }

        return location.toString();
    }
}
