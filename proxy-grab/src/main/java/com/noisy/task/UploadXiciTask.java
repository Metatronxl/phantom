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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Auther: liuzhaoce
 * @Date: 2019-05-05 15:38
 * @Description:
 */

@Component
@Configuration
@EnableScheduling
public class UploadXiciTask {

    @Resource
    AgentIpServiceImpl agentIpService;

//    @Scheduled(fixedRate = 5000)
    private void configureTasks() {

        try {
            String s = HttpInvoker.get("https://www.xicidaili.com/nn/1");
            List<String> msg = XpathParser.compile("//css('#ip_list')::tbody/tr[position() > 1]/td/text()").evaluateToString(Jsoup.parse(s));
            List<String> location = XpathParser.compile("//css('#ip_list')::tbody/tr[position() > 1]/td/a/text()").evaluateToString(Jsoup.parse(s));
            System.out.println(msg);
            System.out.println(location);
            List<AgentIp> agentIPs = msgToIP(msg, location);
            System.out.println(agentIPs);
            agentIpService.upload(agentIPs);
            System.err.println("爬取西刺代理 定时任务时间: " + LocalDateTime.now());
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }

    }

    private static List<AgentIp> msgToIP(List<String> msg, List<String> location) {

        List<List<String>> listArr = IPutils.createList(msg, 10);
        List<AgentIp> agentIPList = Lists.newArrayList();
        int i = 0;
        for (List<String> item : listArr) {
            String loc = "";
            if (location.size() > i){
                loc = location.get(i);
            }
            AgentIp agentIp = new AgentIp(item.get(1), item.get(2), item.get(4), item.get(5), loc, "");
            agentIPList.add(agentIp);
            i++;
        }
        return agentIPList;
    }

}
