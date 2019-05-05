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
 * @Date: 2019-05-05 10:48
 * @Description:
 */

@Component
@Configuration
@EnableScheduling
public class UploadKuaidailiTask {

    @Resource
    AgentIpServiceImpl agentIpService;

//    @Scheduled(fixedRate = 5000)
    private void configureTasks() {

        try {
            String s = HttpInvoker.get("https://www.kuaidaili.com/free/");
            System.out.println(s);
            List<String> msg = XpathParser.compile("//css('.con-body')::div/div/table/tbody/tr/td/text()").evaluateToString(Jsoup.parse(s));
            List<AgentIp> agentIps = msgToIP(msg, 7);
            System.out.println(agentIps);
            agentIpService.upload(agentIps);
            System.err.println("爬取快代理 定时任务时间: " + LocalDateTime.now());
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @Description: msg转换为 List<AgentIp>
     */
    public static List<AgentIp> msgToIP(List<String> msg, int size) {

        List<List<String>> listArr = IPutils.createList(msg, size);
        List<AgentIp> agentIPList = Lists.newArrayList();
        for (List<String> item : listArr) {
            String[] bits = item.get(4).split(" ");
            String operator = bits[bits.length - 1];
            String location = item.get(4).substring(0, item.get(4).indexOf(operator));
            AgentIp agentIp = new AgentIp(item.get(0), item.get(1), item.get(2), item.get(3), location, operator);
            agentIPList.add(agentIp);
        }
        return agentIPList;
    }
}
