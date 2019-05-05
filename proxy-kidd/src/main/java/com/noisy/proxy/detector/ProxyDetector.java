package com.noisy.proxy.detector;

/**
 * Created by kevin on 5/24/16.
 */
public interface ProxyDetector {
    /**
     * Detect the given IP and port, will return the proxy info if its a proxy.
     *
     * @param ip
     * @param port
     */
    void detect(String ip, int port);

    /**
     * Reset the proxy detector.
     */
    void reset();
}
