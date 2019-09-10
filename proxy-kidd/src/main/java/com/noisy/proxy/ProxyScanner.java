package com.noisy.proxy;

import com.noisy.proxy.entity.InputType;
import com.noisy.proxy.entity.ProtocolType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author lei.X
 * @date 2019/5/5
 */

@Component
public class ProxyScanner {


    private final static Logger log = LoggerFactory.getLogger(ProxyScanner.class);

    private static Map<Integer, String> inlandIPSegments;
    private static Map<Integer, String> abroadIPSegments;
    private static Set<String> ipList;
    private static Set<Integer> ports;

    @Autowired
    private Environment env;
    @Value("${data.address}")
    private String dataAddress;
    @Value("${input_type}")
    // 设定初始类型(选择IP_LIST 还是 IP_SEGMENT)
    private String inputType;


    public void start() {


        String ipListFilePath = dataAddress + "IpList.txt";
        String inlandFilePath = dataAddress + "InlandIPSegments.txt";
        String abroadIpFilePath = dataAddress + "AbroadIPSegments.txt";
        String portFilePath = env.getProperty("detection.ports.file");


        // 扫描控制
        long inlandHTTPInterval = Integer.valueOf(env.getProperty("inland.http.scanner.interval")) * 60 * 60 * 1000;
        long inlandSocks4Interval = Integer.valueOf(env.getProperty("inland.socks4.scanner.interval")) * 60 * 60 * 1000;
        long inlandSocks5Interval = Integer.valueOf(env.getProperty("inland.socks5.scanner.interval")) * 60 * 60 * 1000;
        long abroadHTTPInterval = Integer.valueOf(env.getProperty("abroad.http.scanner.interval")) * 60 * 60 * 1000;
        long abroadSocks4Interval = Integer.valueOf(env.getProperty("abroad.socks4.scanner.interval")) * 60 * 60 * 1000;
        long abroadSocks5Interval = Integer.valueOf(env.getProperty("abroad.socks5.scanner.interval")) * 60 * 60 * 1000;


        log.info("Starting the scanner.");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

        //读取IP list文件
        if (InputType.IP_LIST.isThisType(inputType)) {
            ipList = getIpListSet(ipListFilePath);
        } else {
            inlandIPSegments = readIPSegments(inlandFilePath);
            abroadIPSegments = readIPSegments(abroadIpFilePath);
        }
        // 读取port文件
        ports = getPortSet(portFilePath);

        if (inlandHTTPInterval >= 0) {
            scheduleInlandHTTPTask(executor, inlandHTTPInterval);
        }

        if (inlandSocks4Interval >= 0) {
            scheduleInlandSocks4Task(executor,inlandSocks4Interval);
        }

        if (inlandSocks5Interval >= 0) {
            scheduleInlandSocks5Task(executor,inlandSocks5Interval);
        }

        if (InputType.IP_SEGMENTS.isThisType(inputType)) {
            if (abroadHTTPInterval >= 0) {
                scheduleAbroadHTTPTask(executor,abroadHTTPInterval);
            }

            if (abroadSocks4Interval >= 0) {
                scheduleAbroadSocks4Task(executor,abroadSocks4Interval);
            }

            if (abroadSocks5Interval >= 0) {
                scheduleAbroadSocks5Task(executor,abroadSocks5Interval);
            }
        }

        log.info("Scanner started.");

    }


    private void scheduleInlandHTTPTask(ScheduledExecutorService executor, long inlandHTTPInterval) {
        log.info("Scheduling the inland HTTP proxy scheduler.");
        TaskScheduler scheduler = null;

        if (InputType.IP_LIST.isThisType(inputType)) {
            scheduler = new TaskScheduler("InlandHTTPProxy", "国内HTTP代理", ProtocolType.HTTP, ipList,ports);
        } else {
            scheduler = new TaskScheduler("InlandHTTPProxy", "国内HTTP代理", ProtocolType.HTTP, inlandIPSegments,ports);
        }
        //定义启动时间和启动间隔
        executor.scheduleAtFixedRate(scheduler, getInitDelay(inlandHTTPInterval,
                Integer.valueOf(env.getProperty("inland.http.scanner.start"))), inlandHTTPInterval, TimeUnit.MILLISECONDS);
    }


    private void scheduleInlandSocks4Task(ScheduledExecutorService executor,long inlandSocks4Interval){
        TaskScheduler scheduler = null;
        if (InputType.IP_LIST.isThisType(inputType)) {
            scheduler = new TaskScheduler("InlandSocks4Proxy", "国内Socks4代理",
                    ProtocolType.SOCKS_V4, ipList,ports);
        } else {
            scheduler = new TaskScheduler("InlandSocks4Proxy", "国内Socks4代理",
                    ProtocolType.SOCKS_V4, inlandIPSegments,ports);
        }
        executor.scheduleAtFixedRate(scheduler, getInitDelay(inlandSocks4Interval,
                Integer.valueOf(env.getProperty("inland.socks4.scanner.start"))), inlandSocks4Interval, TimeUnit.MILLISECONDS);
    }

    private void scheduleInlandSocks5Task(ScheduledExecutorService executor,long inlandSocks5Interval){
        TaskScheduler scheduler = null;
        if (InputType.IP_LIST.isThisType(inputType)) {
            scheduler = new TaskScheduler("InlandSocks5Proxy", "国内Socks5代理",
                    ProtocolType.SOCKS_V5, ipList,ports);
        } else {
            scheduler = new TaskScheduler("InlandSocks5Proxy", "国内Socks5代理",
                    ProtocolType.SOCKS_V5, inlandIPSegments,ports);
        }
        executor.scheduleAtFixedRate(scheduler, getInitDelay(inlandSocks5Interval,
                Integer.valueOf(env.getProperty("inland.socks5.scanner.start"))), inlandSocks5Interval, TimeUnit.MILLISECONDS);
    }

    private void scheduleAbroadHTTPTask(ScheduledExecutorService executor,long abroadHTTPInterval) {
        TaskScheduler scheduler = new TaskScheduler("AbroadHTTPProxy", "国外HTTP代理",
                ProtocolType.HTTP, abroadIPSegments,ports);
        executor.scheduleAtFixedRate(scheduler, getInitDelay(abroadHTTPInterval,
                Integer.valueOf(env.getProperty("abroad.http.scanner.start"))), abroadHTTPInterval, TimeUnit.MILLISECONDS);
    }

    private void scheduleAbroadSocks4Task(ScheduledExecutorService executor,long abroadSocks4Interval) {
        TaskScheduler scheduler = new TaskScheduler("abroadSocks4Proxy", "国外socks4代理",
                ProtocolType.SOCKS_V4, abroadIPSegments,ports);
        executor.scheduleAtFixedRate(scheduler, getInitDelay(abroadSocks4Interval,
                Integer.valueOf(env.getProperty("abroad.socks4.scanner.start"))), abroadSocks4Interval, TimeUnit.MILLISECONDS);
    }

    private void scheduleAbroadSocks5Task(ScheduledExecutorService executor,long abroadSocks5Interval) {
        TaskScheduler scheduler = new TaskScheduler("abroadSocks5Proxy", "国外socks5代理",
                ProtocolType.SOCKS_V5, abroadIPSegments,ports);
        executor.scheduleAtFixedRate(scheduler, getInitDelay(abroadSocks5Interval,
                Integer.valueOf(env.getProperty("abroad.socks5.scanner.start"))), abroadSocks5Interval, TimeUnit.MILLISECONDS);
    }




    public static Map<Integer, String> readIPSegments(String ipSegFilePath) {
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

    // 读取IP list
    private Set<String> getIpListSet(String ipListFilePath) {

        Set<String> filterIpList;

        File proxyIPFile = new File(ipListFilePath);
        try {
            List<String> proxyIPs = FileUtils.readLines(proxyIPFile, "utf-8");
            if (proxyIPs != null) {
                filterIpList = new HashSet<>();
                filterIpList.addAll(proxyIPs);
                return filterIpList;

            }
        } catch (IOException e) {
            log.error("Failed to load the IP list file: {}", ipListFilePath);
            return null;
        }

        if (ipList == null || ipList.isEmpty()) {
            log.error("The IP list file is empty: {}", ipListFilePath);
            return null;
        }

        return null;
    }

    private Set<Integer> getPortSet(String portListFilePath) {
        Set<Integer> filterPortList = new HashSet<>();

        File portListFile = new File(portListFilePath);
        try {
            List<String> portList = FileUtils.readLines(portListFile, "utf-8");
            if (portList != null && !portList.isEmpty()) {
                for (String port : portList) {
                    filterPortList.add(Integer.valueOf(port));

                }
                return filterPortList;
            }
        } catch (IOException e) {
            log.error("Failed to load the port list file: {}", portListFilePath);
            return null;
        }

        if (ipList == null || ipList.isEmpty()) {
            log.error("The port list file is empty: {}", portListFilePath);
            return null;
        }

        return null;
    }

}
