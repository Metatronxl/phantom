package com.noisy.task;

import com.noisy.service.impl.AgentIpServiceImpl;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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

        String s = HttpInvoker.get("http://www.goubanjia.com/");

        // TODO : 爬取goubanjia的代理ip，页面分析较为繁琐，每个数字放在不同的标签里

    }


}
