package com.noisy.proxy;

import com.noisy.proxy.detector.ProxyDetector;
import com.noisy.proxy.detector.ProxyDetectorFactory;
import com.noisy.proxy.entity.InputType;
import com.noisy.proxy.entity.ProtocolType;
import com.noisy.proxy.util.DetectPorts;
import com.noisy.proxy.util.IPFilterUtils;
import com.noisy.proxy.util.IPPoolUtils;
import com.noisy.proxy.util.IPSegment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by kevin on 5/25/16.
 */
public class TaskScheduler implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(TaskScheduler.class);
    private final static Set<Integer> ports = DetectPorts.getInstance().getPorts();

    private String name;
    private String scanTarget;
    private Map<Integer, String> ipSegments;
    private Set<String> ipList;
    private ProtocolType protocolType;
    private ProxyDetector proxyDetector;
    private long totalTasks;
    private String inputType;

    public TaskScheduler(String name, String scanTarget, ProtocolType protocolType, Map<Integer, String> ipSegments) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(scanTarget)
                || protocolType == null || ipSegments == null) {
            throw new IllegalArgumentException(
                    "Any one of the parameters cannot be null when constructing the TaskScheduler.");
        }
        log.info("Building scheduler: {}", name);
        this.name = name;
        this.scanTarget = scanTarget;
        this.protocolType = protocolType;
        this.ipSegments = ipSegments;

        proxyDetector = ProxyDetectorFactory.createProxyDetector(protocolType, this);
        totalTasks = 0;
        inputType = InputType.IP_SEGMENTS.getType();
    }

    public TaskScheduler(String name, String scanTarget, ProtocolType protocolType, Set<String> ipList) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(scanTarget)
                || protocolType == null || ipList == null) {
            throw new IllegalArgumentException(
                    "Any one of the parameters cannot be null when constructing the TaskScheduler.");
        }
        log.info("Building scheduler: {}", name);
        this.name = name;
        this.scanTarget = scanTarget;
        this.protocolType = protocolType;
        this.ipList = ipList;

        proxyDetector = ProxyDetectorFactory.createProxyDetector(protocolType, this);
        totalTasks = 0;
        inputType = InputType.IP_LIST.getType();
    }

    @Override
    public void run() {
        proxyDetector.reset();
        log.info("The scheduler {} started to execute the new schedule with {} input.", name, inputType);
        if (InputType.IP_LIST.isThisType(inputType)) {
            createTaskFromIpList();
        } else {
            createTaskFromIpSegs();
        }
    }

    private long createTaskFromIpSegs() {
        if (ipSegments == null || ipSegments.isEmpty()) {
            return 0;
        }
        countTotalTasks();

        log.info("{}: creating tasks.", name);
        for (Integer port : ports) {
            List<IPSegment> ipSegmentList = IPPoolUtils.getIPSegments(ipSegments.values());
            while (!ipSegmentList.isEmpty()) {
                Iterator<IPSegment> iterator = ipSegmentList.iterator();
                while (iterator.hasNext()) {
                    IPSegment ipSegment = iterator.next();
                    if (ipSegment.hasNextIP()) {
                        String proxyIP = ipSegment.getNextStringIP();
                        if (!IPFilterUtils.getInstance().needFilter(proxyIP)) {
                            proxyDetector.detect(proxyIP, port);
                        }
                    } else {
                        iterator.remove();
                    }
                }
            }
        }

        long totalIPNum = totalTasks / ports.size();
        log.info("{} has finished to create the tasks, total IP number: {}", name, totalIPNum);

        return totalTasks;
    }

    private long createTaskFromIpList() {
        countTotalTasks();

        log.info("{}: creating tasks.", name);
        for (Integer port : ports) {
            for (String proxyIP : ipList) {

                proxyDetector.detect(proxyIP, port);
            }
        }

        long totalIPNum = totalTasks / ports.size();
        log.info("{} has finished to create the tasks, total IP number: {}", name, totalIPNum);

        return totalTasks;
    }

    public long countTotalTasks() {
        if (totalTasks > 0) {
            return totalTasks;
        }

        log.info("Counting tasks.");
        if (InputType.IP_LIST.isThisType(inputType)) {
            totalTasks = ports.size() * (ipList == null ? 0 : ipList.size());
        } else {
            for (int i = 0; i < ports.size(); i++) {
                List<IPSegment> ipSegmentList = IPPoolUtils.getIPSegments(ipSegments.values());
                while (!ipSegmentList.isEmpty()) {
                    Iterator<IPSegment> iterator = ipSegmentList.iterator();
                    while (iterator.hasNext()) {
                        IPSegment ipSegment = iterator.next();
                        if (ipSegment.hasNextIP()) {
                            String proxyIP = ipSegment.getNextStringIP();
                            if (!IPFilterUtils.getInstance().needFilter(proxyIP)) {
                                totalTasks++;
                            }
                        } else {
                            iterator.remove();
                        }
                    }
                }
            }
        }
        log.info("Total tasks: {}", totalTasks);

        return totalTasks;
    }

    public String getName() {
        return name;
    }

    public String getScanTarget() {
        return scanTarget;
    }

    public void setScanTarget(String scanTarget) {
        this.scanTarget = scanTarget;
    }

    public Map<Integer, String> getIpSegments() {
        return ipSegments;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public int getScanPortNum() {
        return ports.size();
    }

    public ProxyDetector getProxyDetector() {
        return proxyDetector;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }
}
