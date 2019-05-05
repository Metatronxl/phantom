package com.noisy.task;

import com.noisy.service.impl.AgentIpServiceImpl;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.sipsoup.exception.XpathSyntaxErrorException;
import com.virjar.sipsoup.parse.XpathParser;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Auther: liuzhaoce
 * @Date: 2019-05-05 17:13
 * @Description:
 */

@Component
@Configuration
@EnableScheduling
public class UploadGoubanjiaTask {

    @Resource
    AgentIpServiceImpl agentIpService;

//    @Scheduled(fixedRate = 5000)
    private void configureTasks() {

        try {
            String s = HttpInvoker.get("http://www.goubanjia.com/");
            System.out.println(s);
            List<String> msg = XpathParser.compile("//css('.wlist')::ul[position() > 1]/span/li/text()").evaluateToString(Jsoup.parse(s));
//            List<AgentIp> agentIPs = msgToIP(msg);
//            System.out.println(agentIPs);
//            agentIpService.upload(agentIPs);
//            System.err.println("爬取无忧代理 定时任务时间: " + LocalDateTime.now());
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }

    }


}
