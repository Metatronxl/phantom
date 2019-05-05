package com.noisy.proxy.detector;

import com.noisy.proxy.TaskScheduler;
import com.noisy.proxy.entity.ProtocolType;

/**
 * Created by kevin on 6/13/16.
 */
public class ProxyDetectorFactory {
    /**
     * Create a new proxy detector with the given protocol type and task scheduler.
     *
     * @param protocolType ProtocolType
     * @param scheduler    TaskSchedule Object
     * @return a ProxyDetector Object
     */
    public static ProxyDetector createProxyDetector(ProtocolType protocolType, TaskScheduler scheduler) {
        switch (protocolType) {
            case HTTP:
                return new HTTPProxyDetector(scheduler);
//            case SOCKS_V4:
//                return new Socks4ProxyDetector(scheduler);
//            case SOCKS_V5:
//                return new Socks5ProxyDetector(scheduler);
        }

        return new HTTPProxyDetector(scheduler);
    }
}
