package com.maxent.proxy.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kevin on 5/25/16.
 */
public class IPPoolUtils {
    private final static Logger log = LoggerFactory.getLogger(IPPoolUtils.class);
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private final static Pattern ipreg = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");

    public static Set<String> retrieveIPFromText(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = ipreg.matcher(text);
        Set<String> ipSet = new HashSet<>();
        while (matcher.find()) {
            ipSet.add(matcher.group());
        }

        return ipSet;
    }

    public static boolean validateIP(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    /**
     * Get IPSegments from a normal IP segment expression list, IP with mask bits, eg. 220.154.0.0/15
     *
     * @param ipWithMaskBitsList
     * @return
     */
    public static List<IPSegment> getIPSegments(Collection<String> ipWithMaskBitsList) {
        if (ipWithMaskBitsList == null) {
            throw new IllegalArgumentException("The given IP segment expression list is null.");
        }
        Random random = new Random(System.currentTimeMillis());

        List<IPSegment> ipSegments = new ArrayList<>();
        for (String ipWithMaskBits : ipWithMaskBitsList) {
            IPSegment ipSegment = getIPSegment(ipWithMaskBits);
            ipSegment.setRandomIndex(random.nextInt());
            //ipSegment.setIpWithMaskBits(ipWithMaskBits);
            if (needAdds(ipSegment, ipSegments)) {
                ipSegments.add(ipSegment);
            }
        }
        Collections.sort(ipSegments);

        return ipSegments;
    }

    private static boolean needAdds(final IPSegment ipSegment, final List<IPSegment> ipSegments) {
        Iterator<IPSegment> iterator = ipSegments.iterator();
        while (iterator.hasNext()) {
            IPSegment segment = iterator.next();
            // if the given IP segment has been include
            if (ipSegment.getStartLongIP() >= segment.getStartLongIP()
                    && ipSegment.getEndLongIP() <= segment.getEndLongIP()) {
                return false;
            }

            // if the given IP segment include an exists IP segment in the list, then remove the exists one.
            if (ipSegment.getStartLongIP() <= segment.getStartLongIP()
                    && ipSegment.getEndLongIP() >= segment.getEndLongIP()) {
                iterator.remove();
            }
        }

        return true;
    }

    /**
     * Get IPSegment from a normal IP segment expression, IP with mask bits, eg. 220.154.0.0/15
     *
     * @param ipWithMaskBits
     * @return
     */
    public static IPSegment getIPSegment(String ipWithMaskBits) {
        if (StringUtils.isEmpty(ipWithMaskBits)) {
            log.error("The given parameter is empty or null.");
            return null;
        }

        String[] ipSegTmp = ipWithMaskBits.trim().split("\\/");
        if (ipSegTmp.length != 2 || !validateIP(ipSegTmp[0])) {
            log.error("The given IP segments expression is invalid.");
            return null;
        }

        return getEndIP(ipSegTmp[0], Integer.valueOf(ipSegTmp[1]));
    }

    /**
     * According to the start IP address and the bits number of net mask to calculate the end IP address.
     *
     * @param startIP
     * @param netmask
     * @return
     */
    public static IPSegment getEndIP(String startIP, int netmask) {
        return getEndIP(startIP, getMask(netmask));
    }

    /**
     * Return the net mask with the given bits of net mask
     *
     * @param masks
     * @return
     */
    public static String getMask(int masks) {
        if (masks == 1)
            return "128.0.0.0";
        else if (masks == 2)
            return "192.0.0.0";
        else if (masks == 3)
            return "224.0.0.0";
        else if (masks == 4)
            return "240.0.0.0";
        else if (masks == 5)
            return "248.0.0.0";
        else if (masks == 6)
            return "252.0.0.0";
        else if (masks == 7)
            return "254.0.0.0";
        else if (masks == 8)
            return "255.0.0.0";
        else if (masks == 9)
            return "255.128.0.0";
        else if (masks == 10)
            return "255.192.0.0";
        else if (masks == 11)
            return "255.224.0.0";
        else if (masks == 12)
            return "255.240.0.0";
        else if (masks == 13)
            return "255.248.0.0";
        else if (masks == 14)
            return "255.252.0.0";
        else if (masks == 15)
            return "255.254.0.0";
        else if (masks == 16)
            return "255.255.0.0";
        else if (masks == 17)
            return "255.255.128.0";
        else if (masks == 18)
            return "255.255.192.0";
        else if (masks == 19)
            return "255.255.224.0";
        else if (masks == 20)
            return "255.255.240.0";
        else if (masks == 21)
            return "255.255.248.0";
        else if (masks == 22)
            return "255.255.252.0";
        else if (masks == 23)
            return "255.255.254.0";
        else if (masks == 24)
            return "255.255.255.0";
        else if (masks == 25)
            return "255.255.255.128";
        else if (masks == 26)
            return "255.255.255.192";
        else if (masks == 27)
            return "255.255.255.224";
        else if (masks == 28)
            return "255.255.255.240";
        else if (masks == 29)
            return "255.255.255.248";
        else if (masks == 30)
            return "255.255.255.252";
        else if (masks == 31)
            return "255.255.255.254";
        else if (masks == 32)
            return "255.255.255.255";
        return "";
    }

    /**
     * According to the start IP address and the net mask to calculate the end IP address.
     *
     * @param startIP
     * @param netmask
     * @return
     */
    public static IPSegment getEndIP(String startIP, String netmask) {
        String[] start = Negation(startIP, netmask).split("\\.");
        String tmpStartIP = start[0] + "." + start[1] + "." + start[2] + "." + (Integer.valueOf(start[3]) + 1);
        String tmpEndIP = TaskOR(Negation(startIP, netmask), netmask);

        return new IPSegment(tmpStartIP, tmpEndIP, netmask);
    }

    /**
     * Return the negation of the given start IP address and net mask.
     *
     * @param startIP
     * @param netmask
     * @return
     */
    private static String Negation(String startIP, String netmask) {
        String[] temp1 = startIP.trim().split("\\.");
        String[] temp2 = netmask.trim().split("\\.");
        int[] rets = new int[4];
        for (int i = 0; i < 4; i++) {
            rets[i] = Integer.parseInt(temp1[i]) & Integer.parseInt(temp2[i]);
        }
        return rets[0] + "." + rets[1] + "." + rets[2] + "." + rets[3];
    }

    private static String TaskOR(String StartIP, String netmask) {
        String[] temp1 = StartIP.trim().split("\\.");
        String[] temp2 = netmask.trim().split("\\.");
        int[] rets = new int[4];
        for (int i = 0; i < 4; i++) {
            rets[i] = 255 - (Integer.parseInt(temp1[i]) ^ Integer.parseInt(temp2[i]));
        }
        return rets[0] + "." + rets[1] + "." + rets[2] + "." + (rets[3] - 1);
    }

    /**
     * Return address pool size by the bits of net mask.
     */
    public static int getPoolMax(int netmask) {
        if (netmask <= 0 || netmask >= 32) {
            return 0;
        }
        int bits = 32 - netmask;
        return (int) Math.pow(2, bits) - 2;
    }

    /**
     * Convert the net mask to bits of net mask.
     */
    public static int getNetMask(String netmarks) {
        StringBuffer sbf;
        String str;
        int inetmask = 0, count = 0;
        String[] ipList = netmarks.split("\\.");
        for (int n = 0; n < ipList.length; n++) {
            sbf = toBin(Integer.parseInt(ipList[n]));
            str = sbf.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf('1', i);
                if (i == -1) {
                    break;
                }
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }

    private static StringBuffer toBin(int x) {
        StringBuffer result = new StringBuffer();
        result.append(x % 2);
        x /= 2;
        while (x > 0) {
            result.append(x % 2);
            x /= 2;
        }
        return result;
    }
}

