package com.noisy.proxy.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by kevin on 6/24/16.
 */
public class IPSegmentsUnionCombiner {
    private static final Logger log = LoggerFactory.getLogger(IPSegmentsUnionCombiner.class);

    public Set<String> readIPSegments(String ipSegFilePath) {
        Set<String> ipSegments = new HashSet<>();
        File file = FileUtils.getFile(ipSegFilePath);
        if (file == null) {
            log.warn("Cannot load the IP segments from file: {}", ipSegFilePath);
            return null;
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            List<String> ipSegList = IOUtils.readLines(br);
            if (ipSegList != null && !ipSegList.isEmpty()) {
                for (String ipSeg : ipSegList) {
                    ipSegments.add(ipSeg);
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

    public void union(String srcFilePath, String dstFilePath) {
        Set<String> ipSegments = readIPSegments(srcFilePath);
        List<IPSegment> ipSegmentList = IPPoolUtils.getIPSegments(ipSegments);
        if (ipSegmentList != null && !ipSegmentList.isEmpty()) {
            File dstFile = new File(dstFilePath);

            for (IPSegment ipSegment : ipSegmentList) {
                try {
                    FileUtils.write(dstFile, ipSegment.getIpWithMaskBits() + "\n", true);
                } catch (IOException e) {
                    log.warn("Failed to add the IP segment: {}", ipSegment.getIpWithMaskBits());
                }
            }
        }
    }

    public static void main(String[] args) {
        IPSegmentsUnionCombiner combiner = new IPSegmentsUnionCombiner();
        combiner.union("data/test.txt", "output/IPSegUnion.txt");
    }
}
