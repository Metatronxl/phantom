package com.noisy.proxy.scanner;

import com.noisy.proxy.task.TaskScheduler;
import com.noisy.proxy.util.ConfigUtils;
import com.noisy.proxy.util.InputType;
import com.noisy.proxy.detector.ProtocolType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin on 5/25/16.
 */
public class ProxyScanner {
    private final static Logger log = LoggerFactory.getLogger(ProxyScanner.class);
    private final static Configuration config = ConfigUtils.getConfig();
    private final static String inputType = config.getString("input.type");
    private final static String inlandIpFilePath = config.getString("inland.ip.file.path");
    private final static String abroadIpFilePath = config.getString("abroad.ip.file.path");
    private final static String ipListFilePath = config.getString("ip.list.file.path");
    private static Map<Integer, String> inlandIPSegments;
    private static Map<Integer, String> abroadIPSegments;
    private static Set<String> ipList;

    private final static long inlandHTTPInterval =
            config.getInt("inland.http.scanner.interval") * 60 * 60 * 1000;
    private final static long inlandSocks4Interval =
            config.getInt("inland.socks4.scanner.interval") * 60 * 60 * 1000;
    private final static long inlandSocks5Interval =
            config.getInt("inland.socks5.scanner.interval") * 60 * 60 * 1000;
    private final static long abroadHTTPInterval =
            config.getInt("abroad.http.scanner.interval") * 60 * 60 * 1000;
    private final static long abroadSocks4Interval =
            config.getInt("abroad.socks4.scanner.interval") * 60 * 60 * 1000;
    private final static long abroadSocks5Interval =
            config.getInt("abroad.socks5.scanner.interval") * 60 * 60 * 1000;


    public static void start() {
        log.info("Starting the scanner.");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

        if (InputType.IP_LIST.isThisType(inputType)) {
            File proxyIPFile = new File(ipListFilePath);
            try {
                List<String> proxyIPs = FileUtils.readLines(proxyIPFile);
                if (proxyIPs != null) {
                    ipList = new HashSet<>();
                    ipList.addAll(proxyIPs);
                }
            } catch (IOException e) {
                log.error("Failed to load the IP list file: {},error:{}", ipListFilePath,e.getMessage());
            }

            if (ipList == null || ipList.isEmpty()) {
                log.error("The IP list file is empty: {}", ipListFilePath);
                return;
            }

        } else {
            inlandIPSegments = readIPSegments(inlandIpFilePath);
            abroadIPSegments = readIPSegments(abroadIpFilePath);
        }

        if (inlandHTTPInterval >= 0) {
            scheduleInlandHTTPTask(executor);
        }

        if (inlandSocks4Interval >= 0) {
            scheduleInlandSocks4Task(executor);
        }

        if (inlandSocks5Interval >= 0) {
            scheduleInlandSocks5Task(executor);
        }

        if (InputType.IP_SEGMENTS.isThisType(inputType)) {
            if (abroadHTTPInterval >= 0) {
                scheduleAbroadHTTPTask(executor);
            }

            if (abroadSocks4Interval >= 0) {
                scheduleAbroadSocks4Task(executor);
            }

            if (abroadSocks5Interval >= 0) {
                scheduleAbroadSocks5Task(executor);
            }
        }

        log.info("Scanner started.");
    }

    private static void scheduleInlandHTTPTask(ScheduledExecutorService executor) {
        log.info("Scheduling the inland HTTP proxy scheduler.");
        TaskScheduler scheduler = null;
        if (InputType.IP_LIST.isThisType(inputType)) {
            scheduler = new TaskScheduler("InlandHTTPProxy", "国内HTTP代理",
                    ProtocolType.HTTP, ipList);
        } else {
            scheduler = new TaskScheduler("InlandHTTPProxy", "国内HTTP代理",
                    ProtocolType.HTTP, inlandIPSegments);
        }
        executor.scheduleAtFixedRate(scheduler, getInitDelay(inlandHTTPInterval,
                config.getInt("inland.http.scanner.start")), inlandHTTPInterval, TimeUnit.MILLISECONDS);
    }

    private static void scheduleInlandSocks4Task(ScheduledExecutorService executor) {
        TaskScheduler scheduler = null;
        if (InputType.IP_LIST.isThisType(inputType)) {
            scheduler = new TaskScheduler("InlandSocks4Proxy", "国内Socks4代理",
                    ProtocolType.SOCKS_V4, ipList);
        } else {
            scheduler = new TaskScheduler("InlandSocks4Proxy", "国内Socks4代理",
                    ProtocolType.SOCKS_V4, inlandIPSegments);
        }
        executor.scheduleAtFixedRate(scheduler, getInitDelay(inlandSocks4Interval,
                config.getInt("inland.socks4.scanner.start")), inlandSocks4Interval, TimeUnit.MILLISECONDS);
    }

    private static void scheduleInlandSocks5Task(ScheduledExecutorService executor) {
        TaskScheduler scheduler = null;
        if (InputType.IP_LIST.isThisType(inputType)) {
            scheduler = new TaskScheduler("InlandSocks5Proxy", "国内Socks5代理",
                    ProtocolType.SOCKS_V5, ipList);
        } else {
            scheduler = new TaskScheduler("InlandSocks5Proxy", "国内Socks5代理",
                    ProtocolType.SOCKS_V5, inlandIPSegments);
        }
        executor.scheduleAtFixedRate(scheduler, getInitDelay(inlandSocks5Interval,
                config.getInt("inland.socks5.scanner.start")), inlandSocks5Interval, TimeUnit.MILLISECONDS);
    }

    private static void scheduleAbroadHTTPTask(ScheduledExecutorService executor) {
        TaskScheduler scheduler = new TaskScheduler("AbroadHTTPProxy", "国外HTTP代理",
                ProtocolType.HTTP, abroadIPSegments);
        executor.scheduleAtFixedRate(scheduler, getInitDelay(abroadHTTPInterval,
                config.getInt("abroad.http.scanner.start")), abroadHTTPInterval, TimeUnit.MILLISECONDS);
    }

    private static void scheduleAbroadSocks4Task(ScheduledExecutorService executor) {
        TaskScheduler scheduler = new TaskScheduler("AbroadSocks4Proxy", "国外Socks4代理",
                ProtocolType.SOCKS_V5, abroadIPSegments);
        executor.scheduleAtFixedRate(scheduler, getInitDelay(abroadSocks4Interval,
                config.getInt("abroad.socks4.scanner.start")), abroadSocks4Interval, TimeUnit.MILLISECONDS);
    }

    private static void scheduleAbroadSocks5Task(ScheduledExecutorService executor) {
        TaskScheduler scheduler = new TaskScheduler("AbroadSocks4Proxy", "国外Socks4代理",
                ProtocolType.SOCKS_V5, abroadIPSegments);
        executor.scheduleAtFixedRate(scheduler, getInitDelay(abroadSocks5Interval,
                config.getInt("abroad.socks5.scanner.start")), abroadSocks5Interval, TimeUnit.MILLISECONDS);
    }

    private static Map<Integer, String> readIPSegments(String ipSegFilePath) {
        log.info("Loading IP segments from file: {}", ipSegFilePath);
        Map<Integer, String> ipSegments = new HashMap<>();
        File file = FileUtils.getFile(ipSegFilePath);
        if (file == null) {
            log.warn("Cannot load the IP segments from file: {}", ipSegFilePath);
            return null;
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            List<String> ipSegList = IOUtils.readLines(br);
            if (ipSegList != null && !ipSegList.isEmpty()) {
                int idx = 1;
                for (String ipSeg : ipSegList) {
                    ipSegments.put(idx, ipSeg);
                    idx += 1;
                }
                br.close();
            } else {
                log.warn("The IP segments file is empty, file path: {}", ipSegFilePath);
                br.close();
                return null;
            }
        } catch (IOException e) {
            log.warn("An exception occurred when loading IP segments from file: {}", ipSegFilePath);
            return null;
        }

        return ipSegments;
    }

    private static long getInitDelay(long interval, int startHour) {
        if (startHour < 0) {
            return 0;
        }

        if (startHour > 24) {
            startHour = startHour % 24;
        }

        long initDelay = getTimeMillis(startHour + ":00:00") - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : interval + initDelay;

        return initDelay;
    }

    private static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return curDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
