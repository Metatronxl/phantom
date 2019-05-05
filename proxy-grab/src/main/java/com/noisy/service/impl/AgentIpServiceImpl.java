package com.noisy.service.impl;

import com.noisy.entity.AgentIp;
import com.noisy.mapper.AgentIpMapper;
import com.noisy.service.IAgentIpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuzhaoce
 * @since 2019-05-05
 */
@Service
public class AgentIpServiceImpl extends ServiceImpl<AgentIpMapper, AgentIp> implements IAgentIpService {

    @Resource
    private AgentIpMapper agentIpMapper;


    public void upload(List<AgentIp> agentIpList) {
        for (AgentIp agentIp : agentIpList) {
            int code = agentIpMapper.insertAgentIp(agentIp);
//            log.info("return code: " + code);
        }
    }
}
