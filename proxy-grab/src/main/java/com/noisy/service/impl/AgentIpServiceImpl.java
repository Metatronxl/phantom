package com.noisy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.noisy.entity.AgentIp;
import com.noisy.mapper.AgentIpMapper;
import com.noisy.service.IAgentIpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author liuzhaoce
 * @since 2019-04-29
 */

@Service
@Slf4j
public class AgentIpServiceImpl extends ServiceImpl<AgentIpMapper, AgentIp> implements IAgentIpService {

    public AgentIpServiceImpl() {
        System.out.println("AgentIpServiceImpl created");
    }

    @Resource
    private AgentIpMapper agentIpMapper;

    public void upload(List<AgentIp> agentIpList) {
        for (AgentIp agentIp : agentIpList) {
            int code = agentIpMapper.insert(agentIp);
            log.info("return code: " + code);
        }
    }
}
