package com.noisy.mapper;

import com.noisy.entity.AgentIp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liuzhaoce
 * @since 2019-05-05
 */

@Mapper
public interface AgentIpMapper extends BaseMapper<AgentIp> {

    int insertAgentIp(AgentIp agentIp);

}
