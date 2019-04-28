package com.noisy.proxy.report;

import com.noisy.proxy.util.ConfigUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kevin on 5/31/16.
 */
public class EmailReporter {
    private final static Logger log = LoggerFactory.getLogger(EmailReporter.class);
    private final static Configuration config = ConfigUtils.getConfig();

    private final static EmailReporter _instance = new EmailReporter();
    private final static long dayMilliseconds = 24 * 60 * 60 * 1000;
    private final static long hourMilliseconds = 60 * 60 * 1000;
    private final static long minuteMilliseconds = 60 * 1000;

    private final Email email = new SimpleEmail();

    public static EmailReporter getInstance() {
        return _instance;
    }

    private EmailReporter() {
        try {
            init();
        } catch (EmailException e) {
            log.error("Faild to initialize the email accounts info.");
        }
    }

    private void init() throws EmailException {
        email.setHostName(config.getString("email.server.hostname"));
        email.setSmtpPort(config.getInt("email.server.port"));
        email.setAuthenticator(new DefaultAuthenticator(
                config.getString("email.auth.username"), config.getString("email.auth.password")));
        email.setStartTLSEnabled(true);
        email.setFrom(config.getString("email.auth.username"));
        String[] toList = config.getString("email.to.list").split("\\|");
        for (String to : toList) {
            email.addTo(to);
        }

        email.setSubject("代理服务器IP扫描报告");
    }

    public void report(String scanTarget, Date startTime, long totalIPNum, int portNum, long proxyIPNum, long consumedTime) {
        String startTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime);

        StringBuilder consumedTimeStr = new StringBuilder();
        if (consumedTime >= dayMilliseconds) {
            long days = consumedTime / dayMilliseconds;
            consumedTimeStr.append(days);
            consumedTimeStr.append("天");
        }

        if (consumedTime >= hourMilliseconds) {
            long hours = (consumedTime % dayMilliseconds) / hourMilliseconds;
            consumedTimeStr.append(hours);
            consumedTimeStr.append("小时");
        }

        if (consumedTime >= minuteMilliseconds) {
            long minutes = (consumedTime % hourMilliseconds) / minuteMilliseconds;
            consumedTimeStr.append(minutes);
            consumedTimeStr.append("分钟");
        }

        long seconds = (consumedTime % minuteMilliseconds) / 1000;
        consumedTimeStr.append(seconds);
        consumedTimeStr.append("秒");

        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error("Faild to retrieve the host name of the this server, ex: {}", e);
        }
        long speed = (totalIPNum * portNum * 1000) / consumedTime;

        StringBuilder msgBuilder = new StringBuilder("扫描服务主机：");
        msgBuilder.append(hostname);
        msgBuilder.append("\n扫描目标：");
        msgBuilder.append(scanTarget);
        msgBuilder.append("\n开始时间：");
        msgBuilder.append(startTimeStr);
        msgBuilder.append("\n扫描IP总数：");
        msgBuilder.append(totalIPNum);
        msgBuilder.append("\n扫描端口数：");
        msgBuilder.append(portNum);
        msgBuilder.append("\n代理IP数：");
        msgBuilder.append(proxyIPNum);
        msgBuilder.append("\n探测速度：");
        msgBuilder.append(speed);
        msgBuilder.append(" 次/秒");
        msgBuilder.append("\n共计耗时：");
        msgBuilder.append(consumedTimeStr);

        try {
            email.setMsg(msgBuilder.toString());
            email.send();
        } catch (EmailException e) {
            log.error("Failed to send email for report the scanning results, ex: {}", e);
        }
    }
}
