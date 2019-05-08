package com.noisy.task;

import com.google.common.collect.Lists;
import com.noisy.entity.AgentIp;
import com.noisy.service.impl.AgentIpServiceImpl;
import com.noisy.utils.IPutils;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.sipsoup.exception.XpathSyntaxErrorException;
import com.virjar.sipsoup.parse.XpathParser;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Auther: liuzhaoce
 * @Date: 2019-04-30 20:42
 * @Description:
 */

@Component
@Configuration
@EnableScheduling
public class Upload5UTask {

    @Resource
    AgentIpServiceImpl agentIpService;

    @Scheduled(fixedRate = 5000)
    private void configureTasks() {

        try {
            String s = HttpInvoker.get("http://www.data5u.com/free/index.html");
            List<String> msg = XpathParser.compile("//css('.wlist')::ul[position() > 1]/span/li/text()").evaluateToString(Jsoup.parse(s));
            List<AgentIp> agentIPs = msgToIP(msg);
            System.out.println(agentIPs);
            agentIpService.upload(agentIPs);
            System.err.println("爬取无忧代理 定时任务时间: " + LocalDateTime.now());
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }

    }

    private static List<AgentIp> msgToIP(List<String> msg) {

        List<List<String>> listArr = IPutils.createList(msg, 9);
        List<AgentIp> agentIPList = Lists.newArrayList();
        for (List<String> item : listArr) {
            AgentIp agentIp = new AgentIp(item.get(0), item.get(1), item.get(2), item.get(3), item.get(4) + " " + item.get(5), item.get(6));
            agentIPList.add(agentIp);
        }
        return agentIPList;
    }

}
