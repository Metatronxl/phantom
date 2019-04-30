package com.noisy.crawlers;


import com.google.common.collect.Lists;
import com.noisy.entity.AgentIp;
import com.noisy.mapper.AgentIpMapper;
import com.noisy.service.impl.AgentIpServiceImpl;
import com.noisy.utils.IPutils;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.sipsoup.exception.XpathSyntaxErrorException;
import com.virjar.sipsoup.parse.XpathParser;
import org.jsoup.Jsoup;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Auther: liuzhaoce
 * @Date: 2019-04-26 16:09
 * @Description: 爬虫规则
 */

@Component
@MapperScan("com.noisy")
public class GrabData5u {

    @Resource
    private AgentIpServiceImpl agentIpService;

    private static GrabData5u grabData5u;

    @Resource
    private AgentIpMapper agentIpMapper;



    public static void grab(ConfigurableApplicationContext context) {
        try {
            String s = HttpInvoker.get("http://www.data5u.com/free/index.html");
            List<String> msg = XpathParser.compile("//css('.wlist')::ul[position() > 1]/span/li/text()").evaluateToString(Jsoup.parse(s));
            List<AgentIp> agentIPs = msgToIP(msg);
            System.out.println(agentIPs);
            AgentIpServiceImpl agentIpService = context.getBean(AgentIpServiceImpl.class);
//            agentIpMapper.insert(agentIPs.get(0));
//            agentIpService.upload(agentIPs);
            agentIpService.upload(agentIPs);
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
    }

    private static List<AgentIp> msgToIP(List<String> msg) {

        List<List<String>> listArr = IPutils.createList(msg, 9);
        System.out.println(listArr);
        List<AgentIp> agentIPList = Lists.newArrayList();
        for (List<String> item : listArr) {
            AgentIp agentIp = new AgentIp(item.get(0), item.get(1), item.get(2), item.get(3), item.get(4), item.get(5), item.get(6));
            agentIPList.add(agentIp);
        }
        return agentIPList;
    }
}
